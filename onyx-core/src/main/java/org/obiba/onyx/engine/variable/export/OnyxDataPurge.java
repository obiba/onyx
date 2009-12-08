/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.filter.FilteredValueTable;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applies filters (from purge.xml) to the current {@link ValueTable} of {@link Participant}s and returns a List of
 * Participants to be purged.
 */
public class OnyxDataPurge {

  private static final Logger log = LoggerFactory.getLogger(OnyxDataPurge.class);

  private List<OnyxDataExportDestination> purgeConfiguration;

  private ParticipantService participantService;

  public void setPurgeConfiguration(List<OnyxDataExportDestination> purgeConfiguration) {
    this.purgeConfiguration = purgeConfiguration;
  }

  public void setParticipantService(ParticipantService participantService) {
    this.participantService = participantService;
  }

  public List<Participant> getParticipantsToPurge() {
    List<Participant> result = new ArrayList<Participant>();

    long purgeListStartTime = new Date().getTime();
    log.info("Starting to determine which Participants need to be purged.");

    for(Datasource datasource : MagmaEngine.get().getDatasources()) {
      for(OnyxDataExportDestination purge : purgeConfiguration) {
        for(ValueTable table : datasource.getValueTables()) {
          if(table.getEntityType().equalsIgnoreCase("Participant")) {
            ValueTable filteredCollection = new FilteredValueTable(table, purge.getVariableFilterChainForEntityName(table.getEntityType()), purge.getEntityFilterChainForEntityName(table.getEntityType()));
            for(ValueSet valueSet : filteredCollection.getValueSets()) {
              Participant template = new Participant();
              template.setBarcode(valueSet.getVariableEntity().getIdentifier());
              Participant participant = participantService.getParticipant(template);
              if(participant != null) {
                result.add(participant);
              }
            }
          }
        }
      }
    }

    long purgeListEndTime = new Date().getTime();
    log.info("Determined that [{}] Participants need to be purged in [{}ms].", new Object[] { result.size(), purgeListEndTime - purgeListStartTime });

    return result;
  }
}
