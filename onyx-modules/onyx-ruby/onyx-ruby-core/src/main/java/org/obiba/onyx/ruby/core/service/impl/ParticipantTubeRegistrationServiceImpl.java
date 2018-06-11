/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.service.ParticipantTubeRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ParticipantTubeRegistrationServiceImpl extends PersistenceManagerAwareService implements ParticipantTubeRegistrationService {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(ParticipantTubeRegistrationServiceImpl.class);

  //
  // Instance Variables
  //

  private Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigMap;

  public ParticipantTubeRegistration getParticipantTubeRegistration(Participant participant, String tubeSetName) {
    if(participant == null) throw new IllegalArgumentException("participant cannot be null");
    if(tubeSetName == null) throw new IllegalArgumentException("tubeSetName cannot be null");

    TubeRegistrationConfiguration tubeRegistrationConfig = tubeRegistrationConfigMap.get(tubeSetName);

    if(tubeRegistrationConfig == null) {
      throw new IllegalArgumentException("Invalid tubeRegistrationConfiguration (tubeSetName = " + tubeSetName + ")");
    }

    ParticipantTubeRegistration participantTubeRegistration = getParticipantTubeRegistration(participant.getInterview(), tubeSetName);

    if(participantTubeRegistration != null) {
      if(!tubeSetName.equals(participantTubeRegistration.getTubeSetName())) {
        throw new IllegalArgumentException("tubeSetName does not match participantTubeRegistration's tubeSetName");
      }

      participantTubeRegistration.setTubeRegistrationConfig(tubeRegistrationConfig);
    }

    return participantTubeRegistration;
  }

  public ParticipantTubeRegistration start(Participant participant, String tubeSetName) {
    if(participant == null) throw new IllegalArgumentException("Null participant");

    Interview interview = participant.getInterview();
    if(interview == null) {
      throw new IllegalArgumentException("Null participant interview");
    }

    // create and persist a new TubeRegistration
    ParticipantTubeRegistration currentRegistration = createParticipantTubeRegistration(interview, tubeSetName);
    log.info("New ParticipantTubeRegistration id={} is created.", currentRegistration.getId());

    return currentRegistration;
  }

  public void resume(Participant participant, String tubeSetName) {
    if(participant == null) throw new IllegalArgumentException("Null participant");
    if(tubeSetName == null) throw new IllegalArgumentException("Null tubeSetName");

    Interview interview = participant.getInterview();
    if(interview == null) {
      throw new IllegalArgumentException("Null participant interview");
    }

    TubeRegistrationConfiguration tubeRegistrationConfig = tubeRegistrationConfigMap.get(tubeSetName);

    if(tubeRegistrationConfig == null) {
      throw new IllegalArgumentException("Null tubeRegistrationConfiguration (tubeSetName = " + tubeSetName + ")");
    }

    ParticipantTubeRegistration participantTubeRegistration = getParticipantTubeRegistration(interview, tubeSetName);

    if(participantTubeRegistration == null) {
      throw new IllegalStateException("No participant tube registration to resume");
    }

    participantTubeRegistration.setTubeRegistrationConfig(tubeRegistrationConfig);
  }

  public void end(ParticipantTubeRegistration registration) {
    registration.setEndTime(new Date());
    log.debug("ParticipantTubeRegistration id={} is ending.", registration.getId());
    getPersistenceManager().save(registration);
  }

  public void deleteParticipantTubeRegistration(Participant participant, String stageName) {
    ParticipantTubeRegistration participantTubeRegistration = getParticipantTubeRegistration(participant, stageName);

    if(participantTubeRegistration != null) {
      getPersistenceManager().delete(participantTubeRegistration);
    }
  }

  public void deleteAllParticipantTubeRegistrations(Participant participant) {
    ParticipantTubeRegistration template = new ParticipantTubeRegistration();
    template.setInterview(participant.getInterview());
    List<ParticipantTubeRegistration> tubeRegistrations = getPersistenceManager().match(template);
    for(ParticipantTubeRegistration tubeRegistration : tubeRegistrations) {
      getPersistenceManager().delete(tubeRegistration);
    }
  }

  //
  // Methods
  //

  public void setTubeRegistrationConfigurationMap(Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigMap) {
    this.tubeRegistrationConfigMap = tubeRegistrationConfigMap;
  }

  public Map<String, TubeRegistrationConfiguration> getTubeRegistrationConfigurationMap() {
    return tubeRegistrationConfigMap;
  }

  /**
   * Creates a tube registration for the current interview
   * @param interview
   * @param tubeSetName the associated tube set (i.e., Ruby stage name)
   * @return
   * 
   */
  private ParticipantTubeRegistration createParticipantTubeRegistration(Interview interview, String tubeSetName) {
    if(interview == null) throw new IllegalArgumentException("Null interview");
    if(tubeSetName == null) throw new IllegalArgumentException("Null tubeSetName");

    TubeRegistrationConfiguration tubeRegistrationConfig = tubeRegistrationConfigMap.get(tubeSetName);

    if(tubeRegistrationConfig == null) {
      throw new IllegalArgumentException("Null tubeRegistrationConfiguration (tubeSetName = " + tubeSetName + ")");
    }

    ParticipantTubeRegistration registration = new ParticipantTubeRegistration();
    registration.setTubeRegistrationConfig(tubeRegistrationConfig);
    registration.setInterview(interview);
    registration.setTubeSetName(tubeSetName);
    registration.setStartTime(new Date());
    registration = getPersistenceManager().save(registration);
    return registration;
  }

  private ParticipantTubeRegistration getParticipantTubeRegistration(Interview interview, String tubeSetName) {
    if(interview == null) throw new IllegalArgumentException("Null interview");
    if(tubeSetName == null) throw new IllegalArgumentException("Null tubeSetName");

    ParticipantTubeRegistration template = new ParticipantTubeRegistration();
    template.setInterview(interview);
    template.setTubeSetName(tubeSetName);
    return getPersistenceManager().matchOne(template);
  }

}
