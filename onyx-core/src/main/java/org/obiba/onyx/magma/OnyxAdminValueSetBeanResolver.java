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

import java.util.Set;

import org.obiba.magma.Occurrence;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.beans.OccurrenceProvider;
import org.obiba.magma.support.OccurrenceBean;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.stage.StageInstance;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.InterviewService;
import org.obiba.onyx.engine.Action;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableSet;

/**
 * 
 */
public class OnyxAdminValueSetBeanResolver extends AbstractOnyxBeanResolver implements OccurrenceProvider {

  private ApplicationConfigurationService applicationConfigService;

  private InterviewService interviewService;

  @Autowired
  public void setApplicationConfigService(ApplicationConfigurationService applicationConfigService) {
    this.applicationConfigService = applicationConfigService;
  }

  @Autowired
  public void setInterviewService(InterviewService interviewService) {
    this.interviewService = interviewService;
  }

  public boolean resolves(Class<?> type) {
    return Interview.class.equals(type) || Participant.class.equals(type) || Action.class.equals(type) || ApplicationConfiguration.class.equals(type) || StageInstance.class.equals(type);
  }

  public Object resolve(Class<?> type, ValueSet valueSet, Variable Variable) {
    if(type.equals(Interview.class)) {
      return getParticipant(valueSet).getInterview();
    }
    if(type.equals(Participant.class)) {
      return getParticipant(valueSet);
    }
    if(type.equals(Action.class)) {
      Occurrence occurrence = (Occurrence) valueSet;
      return getParticipantService().getActions(getParticipant(valueSet)).get(occurrence.getOrder());
    }
    if(type.equals(ApplicationConfiguration.class)) {
      return applicationConfigService.getApplicationConfiguration();
    }
    if(type.equals(StageInstance.class)) {
      Participant participant = getParticipant(valueSet);
      Occurrence occurrence = (Occurrence) valueSet;
      return interviewService.getStageInstances(participant.getInterview()).get(occurrence.getOrder());
    }
    return null;
  }

  public boolean providesOccurrencesOf(Variable variable) {
    return ("Action".equals(variable.getOccurrenceGroup()) || "StageInstance".equals(variable.getOccurrenceGroup()));
  }

  public Set<Occurrence> loadOccurrences(ValueSet valueSet, Variable variable) {
    Participant participant = getParticipant(valueSet);
    ImmutableSet.Builder<Occurrence> builder = ImmutableSet.builder();

    String occurrenceGroup = variable.getOccurrenceGroup();

    if("Action".equals(occurrenceGroup)) {
      int occurrenceCount = getParticipantService().getActions(participant).size();
      for(int order = 0; order < occurrenceCount; order++) {
        builder.add(new OccurrenceBean(valueSet, variable.getOccurrenceGroup(), order++));
      }
    } else if("StageInstance".equals(occurrenceGroup)) {
      int occurrenceCount = interviewService.getStageInstances(participant.getInterview()).size();
      for(int order = 0; order < occurrenceCount; order++) {
        builder.add(new OccurrenceBean(valueSet, variable.getOccurrenceGroup(), order++));
      }
    }

    return builder.build();
  }
}
