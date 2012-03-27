/*******************************************************************************
 * Copyright 2011(c) OBiBa. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import java.util.Set;

import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueTable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.magma.support.VariableEntityBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultMagmaInstanceProvider implements MagmaInstanceProvider {

  private MagmaEngine magmaEngine;

  @Autowired
  public DefaultMagmaInstanceProvider(MagmaEngine magmaEngine) {
    this.magmaEngine = magmaEngine;
  }

  @Override
  public Set<Datasource> getDatasources() {
    return magmaEngine.getDatasources();
  }

  @Override
  public Datasource getDatasource(String name) {
    return magmaEngine.getDatasource(name);
  }

  @Override
  public Datasource getOnyxDatasource() {
    return magmaEngine.getDatasource(ONYX_DATASOURCE);
  }

  @Override
  public ValueTable getParticipantsTable() {
    return getValueTable(PARTICIPANTS_TABLE_NAME);
  }

  @Override
  public ValueTable getValueTable(String name) {
    return getOnyxDatasource().getValueTable(name);
  }

  @Override
  public ValueTable resolveTableFromVariablePath(String variablePath) {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(variablePath);
    return resolver.resolveTable(getParticipantsTable());
  }

  @Override
  public VariableValueSource resolveVariablePath(String variablePath) {
    MagmaEngineVariableResolver resolver = MagmaEngineVariableResolver.valueOf(variablePath);
    return resolver.resolveSource(getParticipantsTable());
  }

  @Override
  public VariableEntity newParticipantEntity(Participant participant) {
    return newParticipantEntity(participant.getBarcode());
  }

  @Override
  public VariableEntity newParticipantEntity(String identifier) {
    return new VariableEntityBean(PARTICIPANT_ENTITY_TYPE, identifier);
  }
}
