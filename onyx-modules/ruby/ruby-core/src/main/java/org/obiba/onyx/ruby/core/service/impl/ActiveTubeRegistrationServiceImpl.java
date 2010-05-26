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
import java.util.Map;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.contraindication.Contraindication.Type;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.Stage;
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

  private Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigMap;

  private ActiveInterviewService activeInterviewService;

  public void setTubeRegistrationConfigurationMap(Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigMap) {
    this.tubeRegistrationConfigMap = tubeRegistrationConfigMap;
  }

  public Map<String, TubeRegistrationConfiguration> getTubeRegistrationConfigurationMap() {
    return tubeRegistrationConfigMap;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  //
  // ActiveTubeRegistrationService Methods
  //

  public int getExpectedTubeCount() {
    return getParticipantTubeRegistration().getTubeRegistrationConfig().getExpectedTubeCount();
  }

  public int getRegisteredTubeCount() {
    int registeredTubeCount = 0;

    ParticipantTubeRegistration participantTubeRegistration = getParticipantTubeRegistration();

    if(participantTubeRegistration != null) {
      registeredTubeCount = participantTubeRegistration.getRegisteredParticipantTubes().size();
    }

    return registeredTubeCount;
  }

  public ParticipantTubeRegistration getParticipantTubeRegistration() {
    return getCurrentTubeRegistration();
  }

  public List<MessageSourceResolvable> registerTube(String barcode) {
    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();

    ParticipantTubeRegistration participantTubeRegistration = getParticipantTubeRegistration();
    TubeRegistrationConfiguration tubeRegistrationConfig = tubeRegistrationConfigMap.get(participantTubeRegistration.getTubeSetName());

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

    ParticipantTubeRegistration participantTubeRegistration = getParticipantTubeRegistration();
    TubeRegistrationConfiguration tubeRegistrationConfig = tubeRegistrationConfigMap.get(participantTubeRegistration.getTubeSetName());

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

  /**
   * Returns the currently active ParticipantTubeRegistration instance
   */
  private ParticipantTubeRegistration getCurrentTubeRegistration() {
    Stage currentStage = activeInterviewService.getInteractiveStage();
    if(currentStage == null || this.tubeRegistrationConfigMap.containsKey(currentStage.getName()) == false) {
      throw new IllegalStateException("invalid active stage");
    }

    ParticipantTubeRegistration template = new ParticipantTubeRegistration();
    template.setInterview(activeInterviewService.getInterview());
    template.setTubeSetName(activeInterviewService.getInteractiveStage().getName());
    ParticipantTubeRegistration participantTubeRegistration = getPersistenceManager().matchOne(template);
    if(participantTubeRegistration != null) {
      TubeRegistrationConfiguration tubeRegistrationConfig = tubeRegistrationConfigMap.get(currentStage.getName());
      participantTubeRegistration.setTubeRegistrationConfig(tubeRegistrationConfig);
    }
    return participantTubeRegistration;
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
