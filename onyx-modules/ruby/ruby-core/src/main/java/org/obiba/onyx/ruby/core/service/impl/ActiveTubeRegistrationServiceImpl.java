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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.contraindication.Contraindication.Type;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation for a tube registration service, used to register tubes for the current participant.
 */
@Transactional
public class ActiveTubeRegistrationServiceImpl extends PersistenceManagerAwareService implements ActiveTubeRegistrationService {
  //
  // Constants
  //

  private static final Logger log = LoggerFactory.getLogger(ActiveTubeRegistrationServiceImpl.class);

  private static final String INVALIDSIZE_BARCODE_ERROR = "Ruby.Error.InvalidSizeBarcode";

  private static final String DUPLICATE_BARCODE_ERROR = "Ruby.Error.DuplicateBarcode";

  //
  // Instance Variables
  //

  private ActiveInterviewService activeInterviewService;

  private TubeRegistrationConfiguration tubeRegistrationConfig;

  private Serializable currentTubeRegistrationId = null;

  //
  // ActiveTubeRegistrationService Methods
  //

  public ParticipantTubeRegistration start(Participant participant) {
    if(participant == null) {
      throw new IllegalArgumentException("Null participant");
    }

    Interview interview = participant.getInterview();
    if(interview == null) {
      throw new IllegalArgumentException("Null participant interview");
    }

    // stop existing Registration if there is one
    end();

    // create and persist a new TubeRegistration
    ParticipantTubeRegistration currentRegistration = createParticipantTubeRegistration(interview);
    log.info("New ParticipantTubeRegistration id={} is created.", currentRegistration.getId());

    return currentRegistration;
  }

  public void resume(Participant participant) {
    if(participant == null) {
      throw new IllegalArgumentException("Null participant");
    }

    Interview interview = participant.getInterview();
    if(interview == null) {
      throw new IllegalArgumentException("Null participant interview");
    }

    List<ParticipantTubeRegistration> participantTubeRegistrations = getParticipantTubeRegistrations(interview);

    if(participantTubeRegistrations == null || participantTubeRegistrations.isEmpty()) {
      throw new IllegalStateException("No participant tube registration to resume");
    }

    // Resume with the latest one (i.e., the one with the latest start time).
    ParticipantTubeRegistration participantTubeRegistration = participantTubeRegistrations.get(0);
    participantTubeRegistration.setTubeRegistrationConfig(tubeRegistrationConfig);

    currentTubeRegistrationId = participantTubeRegistration.getId();
  }

  public void end() {
    if(currentTubeRegistrationId == null) return;

    ParticipantTubeRegistration currentRegistration = getParticipantTubeRegistration();
    currentRegistration.setEndTime(new Date());

    log.debug("ParticipantTubeRegistration id={} is ending.", currentRegistration.getId());
    getPersistenceManager().save(currentRegistration);

    currentTubeRegistrationId = null;
  }

  
  public int getExpectedTubeCount() {
    return tubeRegistrationConfig.getExpectedTubeCount();
  }

  
  public int getRegisteredTubeCount() {
    int registeredTubeCount = 0;

    ParticipantTubeRegistration participantTubeRegistration = getParticipantTubeRegistration();

    if(participantTubeRegistration != null) {
      registeredTubeCount = participantTubeRegistration.getRegisteredParticipantTubes().size();
    }

    return registeredTubeCount;
  }

  /**
   * Finds the current ParticipantTubeRegistration and return it.
   * 
   * @return participant tube registration of the participant currently being interviewed (if there are many, returns
   * the latest one)
   */
  
  public ParticipantTubeRegistration getParticipantTubeRegistration() {
    ParticipantTubeRegistration participantTubeRegistration = null;

    if(currentTubeRegistrationId != null) {
      participantTubeRegistration = getPersistenceManager().get(ParticipantTubeRegistration.class, currentTubeRegistrationId);
      participantTubeRegistration.setTubeRegistrationConfig(tubeRegistrationConfig);
    } else {
      Interview interview = activeInterviewService.getInterview();
      List<ParticipantTubeRegistration> participantTubeRegistrations = getParticipantTubeRegistrations(interview);

      if(participantTubeRegistrations != null && !participantTubeRegistrations.isEmpty()) {
        // Take the first on the list -- this is the latest one.
        participantTubeRegistration = participantTubeRegistrations.get(0);
        participantTubeRegistration.setTubeRegistrationConfig(tubeRegistrationConfig);
      }
    }

    return participantTubeRegistration;
  }

  public List<MessageSourceResolvable> registerTube(String barcode) {
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();

    int expectedSize = tubeRegistrationConfig.getBarcodeStructure().getExpectedSize();

    if(barcode.length() != expectedSize) {
      DefaultMessageSourceResolvable error = new DefaultMessageSourceResolvable(new String[] { INVALIDSIZE_BARCODE_ERROR }, new Object[] { barcode, expectedSize, barcode.length() });
      errors.add(error);
    } else if(isDuplicateBarcode(barcode)) {
      DefaultMessageSourceResolvable error = new DefaultMessageSourceResolvable(new String[] { DUPLICATE_BARCODE_ERROR }, new Object[] { barcode });
      errors.add(error);
    } else {
      BarcodeStructure barcodeStructure = tubeRegistrationConfig.getBarcodeStructure();
      barcodeStructure.parseBarcode(barcode, errors);

      if(errors.isEmpty()) {
        RegisteredParticipantTube tube = new RegisteredParticipantTube();
        tube.setRegistrationTime(new Date());
        tube.setBarcode(barcode);
        ParticipantTubeRegistration registration = getParticipantTubeRegistration();
        registration.addRegisteredParticipantTube(tube);
        getPersistenceManager().save(tube);
        getPersistenceManager().save(registration);

        log.info("Registered a participant tube with barcode '{}'", barcode);
      }
    }

    return errors;
  }

  public void unregisterTube(String barcode) {
    RegisteredParticipantTube tube = findTubeByBarcode(barcode);

    checkBarcodeExists(barcode, tube);

    ParticipantTubeRegistration registration = getParticipantTubeRegistration();
    if(registration == null) {
      throw new IllegalArgumentException("The current ParticipantTubeRegistration does not exist.");
    }

    registration.removeRegisteredParticipantTube(tube);
    getPersistenceManager().save(registration);
    getPersistenceManager().delete(tube);
  }

  public void setTubeComment(String barcode, String comment) {
    RegisteredParticipantTube tube = findTubeByBarcode(barcode);

    checkBarcodeExists(barcode, tube);

    tube.setComment(comment);
    getPersistenceManager().save(tube);
  }

  public void setTubeRemark(String barcode, List<Remark> remarks) {
    RegisteredParticipantTube tube = findTubeByBarcode(barcode);
    checkBarcodeExists(barcode, tube);

    tube.getRemarks().clear();
    for(Remark remark : remarks) {
      tube.addRemark(remark.getCode());
    }

    getPersistenceManager().save(tube);
  }

  
  public List<Remark> getTubeRemarks(String barcode) {
    RegisteredParticipantTube tube = findTubeByBarcode(barcode);
    checkBarcodeExists(barcode, tube);

    List<Remark> remarks = tubeRegistrationConfig.getAvailableRemarks();
    List<Remark> tubeRemarks = new ArrayList<Remark>();
    for(Remark remark : remarks) {
      if(tube.getRemarks().contains(remark.getCode())) {
        tubeRemarks.add(remark);
      }
    }
    return tubeRemarks;
  }

  
  public boolean hasContraindications(Type type) {
    return getParticipantTubeRegistration().hasContraindications(type);
  }

  
  public Contraindication getContraindication() {
    return getParticipantTubeRegistration().getContraindication();
  }

  public void persistParticipantTubeRegistration() {
    getPersistenceManager().save(getParticipantTubeRegistration());
  }

  public void deleteParticipantTubeRegistration() {
    ParticipantTubeRegistration participantTubeRegistration = getParticipantTubeRegistration();

    if(participantTubeRegistration != null) {
      getPersistenceManager().delete(participantTubeRegistration);
      currentTubeRegistrationId = null;
    }
  }

  //
  // Methods
  //

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public void setTubeRegistrationConfiguration(TubeRegistrationConfiguration config) {
    this.tubeRegistrationConfig = config;
  }

  
  public TubeRegistrationConfiguration getTubeRegistrationConfiguration() {
    return tubeRegistrationConfig;
  }

  /**
   * Creates a tube registration for the current interview and setup the flag
   * @param interview
   * @return
   * 
   */
  private ParticipantTubeRegistration createParticipantTubeRegistration(Interview interview) {
    ParticipantTubeRegistration registration = new ParticipantTubeRegistration();
    registration.setTubeRegistrationConfig(tubeRegistrationConfig);
    registration.setInterview(interview);
    registration.setStartTime(new Date());
    registration = getPersistenceManager().save(registration);
    currentTubeRegistrationId = registration.getId();
    return registration;
  }

  /**
   * Returns a list of all saved participant tube registrations for the participant currently being interviewed.
   * 
   * Note: There could be more than one participant tube registration for a given participant if, for example, one was
   * started and contra-indicated and then another one was started.
   * 
   * @param interview current interview
   * @return list of participant tube registrations for the participant currently being interviewed
   */
  private List<ParticipantTubeRegistration> getParticipantTubeRegistrations(Interview interview) {
    ParticipantTubeRegistration template = new ParticipantTubeRegistration();
    template.setInterview(interview);
    List<ParticipantTubeRegistration> participantTubeRegistrations = getPersistenceManager().match(template, new SortingClause("startTime", false));

    return participantTubeRegistrations;
  }

  /**
   * Finds a tube by its barcode
   * 
   * @param barcode
   * @return
   */
  private RegisteredParticipantTube findTubeByBarcode(String barcode) {
    RegisteredParticipantTube tube = new RegisteredParticipantTube();
    tube.setBarcode(barcode);
    tube = getPersistenceManager().matchOne(tube);

    return tube;
  }

  private boolean isDuplicateBarcode(String barcode) {
    return (findTubeByBarcode(barcode) != null);
  }

  private void checkBarcodeExists(String barcode, RegisteredParticipantTube tube) {
    if(tube == null) {
      throw new IllegalArgumentException("No tube with barcode '" + barcode + "' has been registered");
    }
  }
}
