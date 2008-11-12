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

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.contraindication.Contraindication.Type;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.transaction.annotation.Transactional;

/**
 *Implementation for a tube registration service, used to register tubes for the current participant.
 */
@Transactional
public class ActiveTubeRegistrationServiceImpl extends PersistenceManagerAwareService implements ActiveTubeRegistrationService {

  private static final Logger log = LoggerFactory.getLogger(ActiveTubeRegistrationServiceImpl.class);

  private TubeRegistrationConfiguration tubeRegistrationConfig;

  private Serializable currentTubeRegistrationId = null;

  // private BarcodeStructure barcodeStructure;

  // private IContraindicatable iContraindicatable;

  public ParticipantTubeRegistration start(Participant participant) {
    if(participant == null) {
      throw new IllegalArgumentException("participant cannot be null.");
    }

    Interview interview = participant.getInterview();
    if(interview == null) {
      throw new IllegalArgumentException("no interview found.");
    }

    // stop existing Registration if there is one
    end();

    // create and persist a new TubeRegistration
    ParticipantTubeRegistration currentRegistration = createParticipantTubeRegistration(interview);
    log.info("New ParticipantTubeRegistration id={} is created.", currentRegistration.getId());

    return currentRegistration;
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
    return getParticipantTubeRegistration().getRegisteredParticipantTubes().size();
  }

  /**
   * Finds the current ParticipantTubeRegistration and return it, it will create a new one if there is no
   * TubeRegistration for current Interview.
   * 
   * @return
   */
  public ParticipantTubeRegistration getParticipantTubeRegistration() {
    if(currentTubeRegistrationId == null) {
      return null;
    }
    return getPersistenceManager().get(ParticipantTubeRegistration.class, currentTubeRegistrationId);
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

  public List<MessageSourceResolvable> registerTube(String barcode) {
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    // barcodeStructure.parseBarcode(barcode, errors);

    if(errors.isEmpty()) {
      RegisteredParticipantTube tube = new RegisteredParticipantTube();
      tube.setRegistrationTime(new Date());
      tube.setBarcode(barcode);
      ParticipantTubeRegistration registration = getParticipantTubeRegistration();
      registration.addRegisteredParticipantTube(tube);
      registration.setEndTime(new Date());
      getPersistenceManager().save(tube);
      getPersistenceManager().save(registration);
      log.info("A tube with code '{}' is registered.", barcode);
    }
    return errors;
  }

  public void setTubeComment(String barcode, String comment) {
    RegisteredParticipantTube tube = findTubeByBarcode(barcode);
    tube.setComment(comment);
    getPersistenceManager().save(tube);
  }

  public void setTubeRemark(String barcode, Remark remark) {
    RegisteredParticipantTube tube = findTubeByBarcode(barcode);
    tube.setRemarkCode(remark.getCode());
    getPersistenceManager().save(tube);
  }

  public void unregisterTube(String barcode) {
    RegisteredParticipantTube tube = findTubeByBarcode(barcode);
    ParticipantTubeRegistration registration = getParticipantTubeRegistration();
    if(registration == null) {
      throw new IllegalArgumentException("The current ParticipantTubeRegistration does not exist.");
    }
    registration.removeRegisteredParticipantTube(tube);
    getPersistenceManager().save(registration);
    getPersistenceManager().delete(tube);
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
    if(tube == null) {
      throw new IllegalArgumentException("Couldn't find the the tube with code '" + barcode + "'.");
    }
    return tube;
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

  /*
   * setter and getter methods
   */
  public void setTubeRegistrationConfig(TubeRegistrationConfiguration config) {
    this.tubeRegistrationConfig = config;
  }

  public TubeRegistrationConfiguration getTubeRegistrationConfig() {
    return tubeRegistrationConfig;
  }

  /*
   * public void setBarcodeStructure(BarcodeStructure barcodeStructure) { this.barcodeStructure = barcodeStructure; }
   * 
   * public BarcodeStructure getBarcodeStructure() { return barcodeStructure; }
   */

}
