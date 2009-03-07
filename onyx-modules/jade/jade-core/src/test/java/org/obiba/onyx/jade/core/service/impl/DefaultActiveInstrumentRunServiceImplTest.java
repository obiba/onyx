/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentOutputParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameterCaptureMethod;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantInteractionType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultActiveInstrumentRunServiceImplTest extends BaseDefaultSpringContextTestCase {

  private DefaultActiveInstrumentRunServiceImpl activeInstrumentRunService;

  @Autowired
  private PersistenceManager persistenceManager;

  @Autowired
  private InstrumentTypeFactoryBean instrumentTypeFactoryBean;

  private Map<String, InstrumentType> instrumentTypes;

  private UserSessionService userSessionService;

  private InstrumentService instrumentService;

  private User user;

  private Participant participant;

  //
  // Fixture Methods
  //

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    // Initialize mocks.
    userSessionService = createMock(UserSessionService.class);
    instrumentService = createMock(InstrumentService.class);

    // Initialize activeInstrumentRunService (class being tested).
    activeInstrumentRunService = new DefaultActiveInstrumentRunServiceImpl();
    activeInstrumentRunService.setPersistenceManager(persistenceManager);
    activeInstrumentRunService.setUserSessionService(userSessionService);
    activeInstrumentRunService.setInstrumentService(instrumentService);

    // Initialize instrumentTypes.
    instrumentTypes = (Map<String, InstrumentType>) instrumentTypeFactoryBean.getObject();
  }

  //
  // Test Methods
  //

  @Test
  @Dataset
  public void testStart() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);
    InstrumentType instrumentType = instrumentTypes.get("ArtStiffness");

    // Start an InstrumentRun.
    startInstrumentRun(participant, instrumentType);

    // Verify that a new InstrumentRun was started.
    // TODO: Check the instrumentRun's instrumentType name.
    InstrumentRun instrumentRun = activeInstrumentRunService.getInstrumentRun();
    Assert.assertNotNull(instrumentRun);
    Assert.assertEquals(user.getId(), instrumentRun.getUser().getId());
    Assert.assertEquals(participant.getId(), activeInstrumentRunService.getParticipant().getId());
    Assert.assertEquals(InstrumentRunStatus.IN_PROGRESS, instrumentRun.getStatus());
    Assert.assertNotNull(instrumentRun.getTimeStart());
    Assert.assertNull(instrumentRun.getTimeEnd());

    // Verify that the new InstrumentRun was persisted.
    Assert.assertNotNull(persistenceManager.get(InstrumentRun.class, instrumentRun.getId()));
  }

  @Test
  @Dataset
  public void testEnd() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);
    InstrumentType instrumentType = instrumentTypes.get("ArtStiffness");

    // Start an InstrumentRun.
    InstrumentRun instrumentRun = startInstrumentRun(participant, instrumentType);

    // Now end the InstrumentRun.
    endInstrumentRun();

    // Verify that the InstrumentRun's end time has been saved.
    instrumentRun = persistenceManager.get(InstrumentRun.class, instrumentRun.getId());
    Assert.assertNotNull(instrumentRun);
    Assert.assertNotNull(instrumentRun.getTimeEnd());
  }

  @Test
  @Dataset
  public void testGetParameterByCode() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);
    InstrumentType instrumentType = instrumentTypes.get("ArtStiffness");

    // Start an InstrumentRun.
    startInstrumentRun(participant, instrumentType);

    // Record common instrumentService expectations.
    initInstrumentServiceMock(instrumentType, false);

    // Record specific instrumentService expectations.
    InstrumentParameter parameter = null;

    String parameterCode1 = "OUTPUT_SYSTOLIC_PRESSURE";
    String vendorName1 = "SP";
    parameter = new InstrumentOutputParameter();
    parameter.setCode(parameterCode1);
    parameter.setVendorName(vendorName1);
    expect(instrumentService.getParameterByCode(instrumentType, parameterCode1)).andReturn(parameter);

    String parameterCode2 = "OUTPUT_DIASTOLIC_PRESSURE";
    String vendorName2 = "DP";
    parameter = new InstrumentOutputParameter();
    parameter.setCode(parameterCode2);
    parameter.setVendorName(vendorName2);
    expect(instrumentService.getParameterByCode(instrumentType, parameterCode2)).andReturn(parameter);

    String parameterCode3 = "bogus";
    parameter = new InstrumentOutputParameter();
    parameter.setCode(parameterCode3);
    expect(instrumentService.getParameterByCode(instrumentType, parameterCode3)).andReturn(null);

    replay(instrumentService);

    // Look up a parameter by its code.
    parameter = activeInstrumentRunService.getParameterByCode(parameterCode1);
    Assert.assertEquals(parameterCode1, parameter.getCode());
    Assert.assertEquals(vendorName1, parameter.getVendorName()); // cross-check vendor name

    // Look up another parameter. (In case query always returns the same parameter...)
    parameter = activeInstrumentRunService.getParameterByCode(parameterCode2);
    Assert.assertEquals(parameterCode2, parameter.getCode());
    Assert.assertEquals(vendorName2, parameter.getVendorName()); // cross-check vendor name

    // Now look up a bogus parameter.
    parameter = activeInstrumentRunService.getParameterByCode(parameterCode3);
    Assert.assertNull(parameter);
  }

  @Test
  @Dataset
  public void testGetParameterByVendorName() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);
    InstrumentType instrumentType = instrumentTypes.get("ArtStiffness");

    // Start an InstrumentRun.
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Look up a parameter by its code.
    String vendorName = "SP";
    InstrumentParameter parameter = activeInstrumentRunService.getParameterByVendorName(vendorName);
    Assert.assertEquals(vendorName, parameter.getVendorName());
    Assert.assertEquals("OUTPUT_SYSTOLIC_PRESSURE", parameter.getCode()); // cross-check code

    // Look up another parameter. (In case query always returns the same parameter...)
    vendorName = "DP";
    parameter = activeInstrumentRunService.getParameterByVendorName(vendorName);
    Assert.assertEquals(vendorName, parameter.getVendorName());
    Assert.assertEquals("OUTPUT_DIASTOLIC_PRESSURE", parameter.getCode()); // cross-check code

    // Now look up a bogus parameter.
    vendorName = "bogus";
    parameter = activeInstrumentRunService.getParameterByVendorName(vendorName);
    Assert.assertNull(parameter);
  }

  @Test
  @Dataset
  public void testHasInterpretativeParameter() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType StandingHeight.
    // This InstrumentType has 1 OBSERVED InterpretativeParameter.
    InstrumentType instrumentType = instrumentTypes.get("StandingHeight");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify that the OBSERVED InterpretativeParameter is detected.
    Assert.assertFalse(activeInstrumentRunService.hasInterpretativeParameter(ParticipantInteractionType.ASKED));
    Assert.assertTrue(activeInstrumentRunService.hasInterpretativeParameter(ParticipantInteractionType.OBSERVED));

    resetMocks();
    activeInstrumentRunService.reset();

    // Start an InstrumentRun with InstrumentType ArtStiffness.
    // This InstrumentType has no InterpretativeParameters.
    instrumentType = instrumentTypes.get("ArtStiffness");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify that no InterpretativeParameters are detected.
    Assert.assertFalse(activeInstrumentRunService.hasInterpretativeParameter(ParticipantInteractionType.ASKED));
    Assert.assertFalse(activeInstrumentRunService.hasInterpretativeParameter(ParticipantInteractionType.OBSERVED));
  }

  @Test
  @Dataset
  public void testGetInterpretativeParameters() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType SittingHeight.
    // This InstrumentType has 1 OBSERVED InterpretativeParameter.
    InstrumentType instrumentType = instrumentTypes.get("StandingHeight");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify the number of ASKED and OBSERVED InterpretativeParameters.
    Assert.assertEquals(0, activeInstrumentRunService.getInterpretativeParameters(ParticipantInteractionType.ASKED).size());
    Assert.assertEquals(1, activeInstrumentRunService.getInterpretativeParameters(ParticipantInteractionType.OBSERVED).size());
  }

  @Test
  @Dataset
  public void testHasInputParameterWithReadonlyArg() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType ArtStiffness.
    // This InstrumentType has only READONLY InstrumentInputParameters.
    InstrumentType instrumentType = instrumentTypes.get("ArtStiffness");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMockForInputParameterTests(instrumentType, 7, 0);

    // Verify that the READONLY InstrumentInputParameters are detected.
    Assert.assertTrue(activeInstrumentRunService.hasInputParameter(true));
    Assert.assertFalse(activeInstrumentRunService.hasInputParameter(false));

    resetMocks();
    activeInstrumentRunService.reset();

    // Start an InstrumentRun with InstrumentType StandingHeight.
    // This Instrument has no InstrumentInputParameters.
    instrumentType = instrumentTypes.get("StandingHeight");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMockForInputParameterTests(instrumentType, 0, 0);

    // Verify that no InstrumentInputParameters are detected.
    Assert.assertFalse(activeInstrumentRunService.hasInputParameter(true));
    Assert.assertFalse(activeInstrumentRunService.hasInputParameter(false));
  }

  @Test
  @Dataset
  public void testGetInputParametersWithReadonlyArg() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType ArtStiffness.
    // This InstrumentType has only READONLY InstrumentInputParameters (7).
    InstrumentType instrumentType = instrumentTypes.get("ArtStiffness");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMockForInputParameterTests(instrumentType, 7, 0);

    // Verify the number of READONLY and NON-READONLY InputParameters.
    Assert.assertEquals(7, activeInstrumentRunService.getInputParameters(true).size());
    Assert.assertEquals(0, activeInstrumentRunService.getInputParameters(false).size());
  }

  @Test
  @Dataset
  public void testHasInputParameterWithCaptureMethodArg() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType ArtStiffness.
    // This InstrumentType has 5 AUTOMATIC and 2 MANUAL InstrumentInputParameters.
    InstrumentType instrumentType = instrumentTypes.get("ArtStiffness");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify that the AUTOMATIC and MANUAL InstrumentInputParameters are detected.
    Assert.assertTrue(activeInstrumentRunService.hasInputParameter(InstrumentParameterCaptureMethod.AUTOMATIC));
    Assert.assertTrue(activeInstrumentRunService.hasInputParameter(InstrumentParameterCaptureMethod.MANUAL));
    Assert.assertFalse(activeInstrumentRunService.hasInputParameter(InstrumentParameterCaptureMethod.COMPUTED));
  }

  @Test
  @Dataset
  public void testGetInputParametersWithCaptureMethodArg() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType ArtStiffness.
    // This InstrumentType has 5 AUTOMATIC and 2 MANUAL InstrumentInputParameters.
    InstrumentType instrumentType = instrumentTypes.get("ArtStiffness");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify the number of InstrumentInputParameters by capture method.
    Assert.assertEquals(5, activeInstrumentRunService.getInputParameters(InstrumentParameterCaptureMethod.AUTOMATIC).size());
    Assert.assertEquals(2, activeInstrumentRunService.getInputParameters(InstrumentParameterCaptureMethod.MANUAL).size());
    Assert.assertEquals(0, activeInstrumentRunService.getInputParameters(InstrumentParameterCaptureMethod.COMPUTED).size());
  }

  @Test
  @Dataset
  public void testHasInputParameter() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType ArtStiffness.
    // This InstrumentType has 5 AUTOMATIC and 2 MANUAL InstrumentInputParameters.
    InstrumentType instrumentType = instrumentTypes.get("ArtStiffness");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify that the InstrumentInputParameters are detected.
    Assert.assertTrue(activeInstrumentRunService.hasInputParameter());
  }

  @Test
  @Dataset
  public void testGetInputParameters() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType ArtStiffness.
    // This InstrumentType has 5 AUTOMATIC and 2 MANUAL InstrumentInputParameters.
    InstrumentType instrumentType = instrumentTypes.get("ArtStiffness");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify the number of InstrumentInputParameters.
    Assert.assertEquals(7, activeInstrumentRunService.getInputParameters().size());
  }

  @Test
  @Dataset
  public void testHasOutputParameterWithCaptureMethodArg() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType StandingHeight.
    // This InstrumentType has 0 AUTOMATIC, 2 MANUAL and 1 COMPUTED InstrumentOutputParameters.
    InstrumentType instrumentType = instrumentTypes.get("StandingHeight");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMockForOutputParameterTests(instrumentType);

    // Verify that the MANUAL and COMPUTED InstrumentOutputParameters are detected.
    Assert.assertFalse(activeInstrumentRunService.hasOutputParameter(InstrumentParameterCaptureMethod.AUTOMATIC));
    Assert.assertTrue(activeInstrumentRunService.hasOutputParameter(InstrumentParameterCaptureMethod.MANUAL));
    Assert.assertTrue(activeInstrumentRunService.hasOutputParameter(InstrumentParameterCaptureMethod.COMPUTED));
  }

  @Test
  @Dataset
  public void testGetOutputParametersWithCaptureMethodArg() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType StandingHeight.
    // This InstrumentType has 0 AUTOMATIC, 2 MANUAL and 1 COMPUTED InstrumentOutputParameters.
    InstrumentType instrumentType = instrumentTypes.get("StandingHeight");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMockForOutputParameterTests(instrumentType);

    // Verify the number InstrumentOutputParameters by capture method.
    Assert.assertEquals(0, activeInstrumentRunService.getOutputParameters(InstrumentParameterCaptureMethod.AUTOMATIC).size());
    Assert.assertEquals(2, activeInstrumentRunService.getOutputParameters(InstrumentParameterCaptureMethod.MANUAL).size());
    Assert.assertEquals(1, activeInstrumentRunService.getOutputParameters(InstrumentParameterCaptureMethod.COMPUTED).size());
  }

  @Test
  @Dataset
  public void testHasOutputParameterWithAutomaticArg() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType StandingHeight.
    // This InstrumentType has 0 AUTOMATIC, 2 MANUAL and 1 COMPUTED InstrumentOutputParameters.
    InstrumentType instrumentType = instrumentTypes.get("StandingHeight");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMockForOutputParameterTests(instrumentType);

    // Verify that the InstrumentOutputParameters are detected.
    Assert.assertFalse(activeInstrumentRunService.hasOutputParameter(true));
    Assert.assertTrue(activeInstrumentRunService.hasOutputParameter(false));
  }

  @Test
  @Dataset
  public void testGetOutputParametersWithAutomaticArg() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType StandingHeight.
    // This InstrumentType has 0 AUTOMATIC, 2 MANUAL and 1 COMPUTED InstrumentOutputParameters.
    InstrumentType instrumentType = instrumentTypes.get("StandingHeight");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMockForOutputParameterTests(instrumentType);

    // Verify the number InstrumentOutputParameters by capture method.
    Assert.assertEquals(0, activeInstrumentRunService.getOutputParameters(true).size());
    Assert.assertEquals(3, activeInstrumentRunService.getOutputParameters(false).size());
  }

  @Test
  @Dataset
  public void testHasOutputParameter() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType StandingHeight.
    // This InstrumentType has 0 AUTOMATIC, 2 MANUAL and 1 COMPUTED InstrumentOutputParameters.
    InstrumentType instrumentType = instrumentTypes.get("StandingHeight");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify that the InstrumentOutputParameters are detected.
    Assert.assertTrue(activeInstrumentRunService.hasOutputParameter());
  }

  // @Test
  @Dataset
  public void testHasParameterWithWarning() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType StandingHeight.
    // This InstrumentType has 2 manual InstrumentOutputParameters, each with an ERROR-type check
    // and a WARNING-type checks.
    InstrumentType instrumentType = instrumentTypes.get("StandingHeight");
    InstrumentRun instrumentRun = startInstrumentRun(participant, instrumentType);

    // Set the 2 manual InstrumentOutputParameters to a value in the "warnable" range. This will
    // result in 2 warnings.
    setParameterValue(instrumentRun, getInstrumentParameter(instrumentType, "RES_FIRST_HEIGHT"), DataBuilder.buildDecimal(75.0));
    setParameterValue(instrumentRun, getInstrumentParameter(instrumentType, "RES_SEC_HEIGHT"), DataBuilder.buildDecimal(75.0));

    initInstrumentServiceMock(instrumentType);

    // Verify that the InstrumentOutputParameters warnings are detected.
    Assert.assertTrue(activeInstrumentRunService.hasParameterWithWarning());

    resetMocks();

    // Now set the 2 manual InstrumentOutputParameters to a value within range so that no
    // warnings are generated.
    setParameterValue(instrumentRun, getInstrumentParameter(instrumentType, "RES_FIRST_HEIGHT"), DataBuilder.buildDecimal(180.0));
    setParameterValue(instrumentRun, getInstrumentParameter(instrumentType, "RES_SEC_HEIGHT"), DataBuilder.buildDecimal(180.0));

    initInstrumentServiceMock(instrumentType);

    // Verify that no InstrumentOutputParameters with warnings are detected.
    Assert.assertFalse(activeInstrumentRunService.hasParameterWithWarning());
  }

  // @Test
  @Dataset
  public void testGetParametersWithWarnings() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType StandingHeight.
    // This InstrumentType has 2 manual InstrumentOutputParameters, each with an ERROR-type check
    // and a WARNING-type checks.
    InstrumentType instrumentType = instrumentTypes.get("StandingHeight");
    InstrumentRun instrumentRun = startInstrumentRun(participant, instrumentType);

    // Set the 2 manual InstrumentOutputParameters to a value in the "warnable" range. This will
    // result in 2 warnings.
    setParameterValue(instrumentRun, getInstrumentParameter(instrumentType, "RES_FIRST_HEIGHT"), DataBuilder.buildDecimal(75.0));
    setParameterValue(instrumentRun, getInstrumentParameter(instrumentType, "RES_SEC_HEIGHT"), DataBuilder.buildDecimal(75.0));

    initInstrumentServiceMock(instrumentType);

    // Verify the number of InstrumentOutputParameters with warnings.
    Assert.assertEquals(2, activeInstrumentRunService.getParametersWithWarning().size());
  }

  @Test
  @Dataset
  public void testGetContraindications() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with InstrumentType StandingHeight.
    // This InstrumentType has 2 OBSERVED Contra-indications..
    InstrumentType instrumentType = instrumentTypes.get("StandingHeight");
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify the number of Contra-indications.
    Assert.assertEquals(0, activeInstrumentRunService.getContraindications(Contraindication.Type.ASKED).size());
    Assert.assertEquals(2, activeInstrumentRunService.getContraindications(Contraindication.Type.OBSERVED).size());
  }

  //
  // Helper Methods
  //

  private void resetMocks() {
    reset(userSessionService);
    reset(instrumentService);
  }

  private void initInstrumentServiceMock(InstrumentType instrumentType, boolean replay) {
    expect(instrumentService.getInstrumentType(instrumentType.getName())).andReturn(instrumentType).anyTimes();

    if(replay) {
      replay(instrumentService);
    }
  }

  private void initInstrumentServiceMock(InstrumentType instrumentType) {
    initInstrumentServiceMock(instrumentType, true);
  }

  private void initInstrumentServiceMockForInputParameterTests(InstrumentType instrumentType, int readOnlyParamCount, int nonReadOnlyParamCount) {
    // Record common instrumentService expectations.
    initInstrumentServiceMock(instrumentType, false);

    // Record specific instrumentService expectations.
    List<InstrumentInputParameter> readOnlyInputParams = new ArrayList<InstrumentInputParameter>();
    for(int i = 0; i < readOnlyParamCount; i++) {
      readOnlyInputParams.add(new InstrumentInputParameter());
    }
    expect(instrumentService.getInstrumentInputParameter(instrumentType, true)).andReturn(readOnlyInputParams);

    List<InstrumentInputParameter> nonReadOnlyInputParams = new ArrayList<InstrumentInputParameter>();
    for(int i = 0; i < nonReadOnlyParamCount; i++) {
      nonReadOnlyInputParams.add(new InstrumentInputParameter());
    }
    expect(instrumentService.getInstrumentInputParameter(instrumentType, false)).andReturn(nonReadOnlyInputParams);

    replay(instrumentService);
  }

  private void initInstrumentServiceMockForOutputParameterTests(InstrumentType instrumentType) {
    // Record common instrumentService expectations.
    initInstrumentServiceMock(instrumentType, false);

    // Record/replay specific instrumentService expectations.
    List<InstrumentOutputParameter> autoOutputParams = new ArrayList<InstrumentOutputParameter>();
    expect(instrumentService.getOutputParameters(instrumentType, InstrumentParameterCaptureMethod.AUTOMATIC)).andReturn(autoOutputParams);

    List<InstrumentOutputParameter> manualOutputParams = new ArrayList<InstrumentOutputParameter>();
    manualOutputParams.add(new InstrumentOutputParameter());
    manualOutputParams.add(new InstrumentOutputParameter());
    expect(instrumentService.getOutputParameters(instrumentType, InstrumentParameterCaptureMethod.MANUAL)).andReturn(manualOutputParams);

    List<InstrumentOutputParameter> computedOutputParams = new ArrayList<InstrumentOutputParameter>();
    computedOutputParams.add(new InstrumentOutputParameter());
    expect(instrumentService.getOutputParameters(instrumentType, InstrumentParameterCaptureMethod.COMPUTED)).andReturn(computedOutputParams);

    replay(instrumentService);
  }

  private InstrumentParameter getInstrumentParameter(InstrumentType instrumentType, String parameterCode) {
    InstrumentParameter instrumentParameter = null;

    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      if(parameter.getCode().equals(parameterCode)) {
        instrumentParameter = parameter;
      }
    }

    return instrumentParameter;
  }

  private InstrumentRun startInstrumentRun(Participant participant, InstrumentType instrumentType) {
    // Record mock expectations.
    expect(userSessionService.getUser()).andReturn(user);

    // Stop recording mock expectations.
    replay(userSessionService);

    // Verify that there is no current InstrumentRun.
    InstrumentRun instrumentRun = activeInstrumentRunService.getInstrumentRun();
    Assert.assertNull(instrumentRun);

    // Start a new InstrumentRun.
    instrumentRun = activeInstrumentRunService.start(participant, instrumentType);

    // Verify mock expectations.
    verify(userSessionService);

    return instrumentRun;
  }

  private void endInstrumentRun() {
    activeInstrumentRunService.end();
  }

  private void setParameterValue(InstrumentRun instrumentRun, InstrumentParameter parameter, Data value) {
    InstrumentRunValue template = new InstrumentRunValue();
    template.setInstrumentRun(instrumentRun);
    template.setInstrumentParameter(parameter.getCode());

    InstrumentRunValue runValue = persistenceManager.matchOne(template);
    if(runValue == null) {
      runValue = template;
    }

    runValue.setData(value);
    persistenceManager.save(runValue);
  }
}
