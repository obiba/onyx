/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.stage.StageInstance;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.ExportLogService;
import org.obiba.onyx.core.service.InterviewService;
import org.obiba.onyx.engine.Action;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ValueSetBeanResolver for resolving Onyx "admin" beans.
 */
public class OnyxAdminValueSetBeanResolver extends AbstractOnyxBeanResolver {

  @Autowired
  private ApplicationConfigurationService applicationConfigService;

  @Autowired
  private InterviewService interviewService;

  @Autowired
  private ExportLogService exportLogService;

  public void setApplicationConfigService(ApplicationConfigurationService applicationConfigService) {
    this.applicationConfigService = applicationConfigService;
  }

  public void setInterviewService(InterviewService interviewService) {
    this.interviewService = interviewService;
  }

  public void setExportLogService(ExportLogService exportLogService) {
    this.exportLogService = exportLogService;
  }

  public boolean resolves(Class<?> type) {
    return Interview.class.equals(type) || Participant.class.equals(type) || Action.class.equals(type) || ApplicationConfiguration.class.equals(type) || StageInstance.class.equals(type) || ExportLog.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable variable) {
    if(type.equals(Interview.class)) {
      return getParticipant(valueSet).getInterview();
    }
    if(type.equals(Participant.class)) {
      return getParticipant(valueSet);
    }
    if(type.equals(Action.class)) {
      return getParticipantService().getActions(getParticipant(valueSet));
    }
    if(type.equals(ApplicationConfiguration.class)) {
      return applicationConfigService.getApplicationConfiguration();
    }
    if(type.equals(StageInstance.class)) {
      Participant participant = getParticipant(valueSet);
      return interviewService.getStageInstances(participant.getInterview());
    }
    if(type.equals(ExportLog.class)) {
      return exportLogService.getExportLogs("Participant", valueSet.getVariableEntity().getIdentifier(), null, true);
    }
    return null;
  }

}
