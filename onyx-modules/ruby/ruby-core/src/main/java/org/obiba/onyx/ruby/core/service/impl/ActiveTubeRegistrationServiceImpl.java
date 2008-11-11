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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.contraindication.Contraindication.Type;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 */
@Transactional
public class ActiveTubeRegistrationServiceImpl extends PersistenceManagerAwareService implements ActiveTubeRegistrationService {

  private TubeRegistrationConfiguration tubeRegistrationConfig;

  private ActiveInterviewService activeInterviewService;

  // private BarcodeStructure barcodeStructure;

  // private IContraindicatable iContraindicatable;

  public int getExpectedTubeCount() {
    return tubeRegistrationConfig.getExpectedTubeCount();
  }

  public int getRegisteredTubeCount() {
    return getCurrentParticipantTubeRegistration().getRegisteredParticipantTubes().size();
  }

  /**
   * Finds the current ParticipantTubeRegistration and return it, it will create a new one if there is no
   * TubeRegistration for current Interview.
   * 
   * @return
   */
  private ParticipantTubeRegistration getCurrentParticipantTubeRegistration() {
    ParticipantTubeRegistration template = new ParticipantTubeRegistration(tubeRegistrationConfig);
    template.setInterview(activeInterviewService.getInterview());

    ParticipantTubeRegistration registration = getPersistenceManager().matchOne(template);
    if(registration == null) {
      registration = createParticipantTubeRegistration();
    }
    return registration;
  }

  private ParticipantTubeRegistration createParticipantTubeRegistration() {
    ParticipantTubeRegistration registration = new ParticipantTubeRegistration(tubeRegistrationConfig);
    registration.setInterview(activeInterviewService.getInterview());
    // Contraindication contraindication = iContraindicatable.getContraindication();
    // registration.setContraindicationCode(contraindication == null ? null : contraindication.getCode());
    // registration.setOtherContraindication(iContraindicatable.getOtherContraindication());
    registration.setStartTime(new Date());
    getPersistenceManager().save(registration);
    return registration;
  }

  public List<MessageSourceResolvable> registerTube(String barcode) {
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    // barcodeStructure.parseBarcode(barcode, errors);

    if(errors.isEmpty()) {
      RegisteredParticipantTube tube = new RegisteredParticipantTube();
      tube.setRegistrationTime(new Date());
      tube.setBarcode(barcode);
      ParticipantTubeRegistration registration = getCurrentParticipantTubeRegistration();
      registration.addRegisteredParticipantTube(tube);
      registration.setEndTime(new Date());
      getPersistenceManager().save(tube);
      getPersistenceManager().save(registration);
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
    ParticipantTubeRegistration registration = getCurrentParticipantTubeRegistration();
    registration.removeRegisteredParticipantTube(tube);
    getPersistenceManager().save(registration);
    getPersistenceManager().delete(tube);
  }

  /**
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

  public void setTubeRegistrationConfig(TubeRegistrationConfiguration config) {
    this.tubeRegistrationConfig = config;
  }

  public TubeRegistrationConfiguration getTubeRegistrationConfig() {
    return tubeRegistrationConfig;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public ActiveInterviewService getActiveInterviewService() {
    return activeInterviewService;
  }

  /*
   * public void setBarcodeStructure(BarcodeStructure barcodeStructure) { this.barcodeStructure = barcodeStructure; }
   * 
   * public BarcodeStructure getBarcodeStructure() { return barcodeStructure; }
   */

  public boolean hasContraindications(Type type) {
    return getCurrentParticipantTubeRegistration().hasContraindications(type);
  }

}
