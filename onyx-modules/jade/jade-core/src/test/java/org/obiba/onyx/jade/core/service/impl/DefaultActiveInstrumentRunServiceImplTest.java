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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
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

  private UserSessionService userSessionService;

  private InstrumentService instrumentService;

  private User user;

  private Participant participant;

  //
  // Fixture Methods
  //

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
  }

  //
  // Test Methods
  //

  @Test
  @Dataset
  public void testStart() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);
    InstrumentType instrumentType = persistenceManager.get(InstrumentType.class, 1l);

    // Start an InstrumentRun.
    startInstrumentRun(participant, instrumentType);

    // Verify that a new InstrumentRun was started.
    // TODO: Check the instrumentRun's instrumentType name.
    InstrumentRun instrumentRun = activeInstrumentRunService.getInstrumentRun();
    Assert.assertNotNull(instrumentRun);
    Assert.assertEquals(user.getId(), instrumentRun.getUser().getId());
    Assert.assertEquals(participant.getId(), activeInstrumentRunService.getParticipant().getId());
    Assert.assertEquals(InstrumentRunStatus.IN_PROGRESS, instrumentRun.getStatus());

    // Verify that the new InstrumentRun was persisted.
    Assert.assertNotNull(persistenceManager.get(InstrumentRun.class, instrumentRun.getId()));
  }

  @Test
  @Dataset
  public void testHasInterpretativeParameter() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // First, test the positive case.
    // Start an InstrumentRun with an InstrumentType that has 2 ASKED and 3 OBSERVED InterpretativeParameters.
    InstrumentType instrumentType = persistenceManager.get(InstrumentType.class, 1l);
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify that the ASKED and OBSERVED InterpretativeParameters are detected.
    Assert.assertTrue(activeInstrumentRunService.hasInterpretativeParameter(ParticipantInteractionType.ASKED));
    Assert.assertTrue(activeInstrumentRunService.hasInterpretativeParameter(ParticipantInteractionType.OBSERVED));

    resetMocks();
    activeInstrumentRunService.reset();

    // Next, test the negative case.
    // Start an InstrumentRun with an InstrumentType that does not have any InterpretativeParameters.
    instrumentType = persistenceManager.get(InstrumentType.class, 2l);
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

    // Start an InstrumentRun with an InstrumentType that has 2 ASKED and 3 OBSERVED InterpretativeParameters.
    InstrumentType instrumentType = persistenceManager.get(InstrumentType.class, 1l);
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify the number of ASKED and OBSERVED InterpretativeParameters.
    Assert.assertEquals(2, activeInstrumentRunService.getInterpretativeParameters(ParticipantInteractionType.ASKED).size());
    Assert.assertEquals(3, activeInstrumentRunService.getInterpretativeParameters(ParticipantInteractionType.OBSERVED).size());
  }

  @Test
  @Dataset
  public void testHasInputParameter() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // First, test the positive case.
    // Start an InstrumentRun with an InstrumentType that has 2 READONLY and 3 NON-READONLY InstumentInputParameters.
    InstrumentType instrumentType = persistenceManager.get(InstrumentType.class, 3l);
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify that the READONLY and NON-READONLY InstrumentInputParameters are detected.
    Assert.assertTrue(activeInstrumentRunService.hasInputParameter(true));
    Assert.assertTrue(activeInstrumentRunService.hasInputParameter(false));

    resetMocks();
    activeInstrumentRunService.reset();

    // Next, test the negative case.
    // Start an InstrumentRun with an InstrumentType that does not have any InstrumentInputParameters.
    instrumentType = persistenceManager.get(InstrumentType.class, 2l);
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify that no InstrumentInputParameters are detected.
    Assert.assertFalse(activeInstrumentRunService.hasInputParameter(true));
    Assert.assertFalse(activeInstrumentRunService.hasInputParameter(false));
  }

  @Test
  @Dataset
  public void testGetInputParameters() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with an InstrumentType that has 2 READONLY and 3 NON-READONLY InstumentInputParameters.
    InstrumentType instrumentType = persistenceManager.get(InstrumentType.class, 3l);
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify the number of READONLY and NON-READONLY InputParameters.
    Assert.assertEquals(2, activeInstrumentRunService.getInputParameters(true).size());
    Assert.assertEquals(3, activeInstrumentRunService.getInputParameters(false).size());
  }

  public void testHasOutputParameter() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // First, test the positive case.
    // Start an InstrumentRun with an InstrumentType that has InstrumentOutputParameters with 2 AUTOMATIC
    // 3 NON-AUTOMATIC capture methods.
    InstrumentType instrumentType = persistenceManager.get(InstrumentType.class, 5l);
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify that the AUTOMATIC and NON-AUTOMATIC InstrumentOutputParameters are detected.
    Assert.assertTrue(activeInstrumentRunService.hasOutputParameter(true));
    Assert.assertTrue(activeInstrumentRunService.hasOutputParameter(false));

    resetMocks();
    activeInstrumentRunService.reset();

    // Next, test the negative case.
    // Start an InstrumentRun with an InstrumentType that does not have any InstrumentOutputParameters.
    instrumentType = persistenceManager.get(InstrumentType.class, 6l);
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify that no InstrumentOutputParameters are detected.
    Assert.assertFalse(activeInstrumentRunService.hasOutputParameter(true));
    Assert.assertFalse(activeInstrumentRunService.hasOutputParameter(false));
  }

  @Test
  @Dataset
  public void testGetOutputParameters() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun with an InstrumentType that has InstrumentOutputParameters with 2 AUTOMATIC
    // 3 NON-AUTOMATIC capture methods.
    InstrumentType instrumentType = persistenceManager.get(InstrumentType.class, 5l);
    startInstrumentRun(participant, instrumentType);

    initInstrumentServiceMock(instrumentType);

    // Verify the number of AUTOMATIC and NON-AUTOMATIC InstrumentOutputParameters.
    Assert.assertEquals(2, activeInstrumentRunService.getOutputParameters(true).size());
    Assert.assertEquals(3, activeInstrumentRunService.getOutputParameters(false).size());
  }

  @Test
  @Dataset
  public void testHasParameterWithWarning() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // First, test the positive case.
    // Start an InstrumentRun with an InstrumentType that has two InstrumentOutputParameters: 1 with
    // a WARNING-type RangeCheck and 1 with an ERROR-type RangeCheck (same range).
    InstrumentType instrumentType = persistenceManager.get(InstrumentType.class, 7l);
    InstrumentRun instrumentRun = startInstrumentRun(participant, instrumentType);

    // Set all parameters to an out-of-range value. This will result in a WARNING for the first parameter,
    // and an ERROR for the second one.
    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      setParameterValue(instrumentRun, parameter, DataBuilder.buildInteger(0l));
    }

    initInstrumentServiceMock(instrumentType);

    // Verify that the the InstrumentOutputParameter with the WARNING is detected.
    Assert.assertTrue(activeInstrumentRunService.hasParameterWithWarning());

    resetMocks();
    activeInstrumentRunService.reset();

    // Next, test the negative case.
    // Start a InstrumentRun with an InstrumentType that has two InstrumentOutputParameters, each
    // having an ERROR-type RangeCheck (i.e. no warnings!).
    instrumentType = persistenceManager.get(InstrumentType.class, 8l);
    instrumentRun = startInstrumentRun(participant, instrumentType);

    // Set all parameters to an out-of-range value. This will result in ERRORS for both parameters
    // (but no WARNINGS).
    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      setParameterValue(instrumentRun, parameter, DataBuilder.buildInteger(0l));
    }

    initInstrumentServiceMock(instrumentType);

    // Verify that no InstrumentOutputParameters with WARNINGS are detected.
    Assert.assertFalse(activeInstrumentRunService.hasParameterWithWarning());
  }

  @Test
  @Dataset
  public void testGetParametersWithWarnings() {
    user = persistenceManager.get(User.class, 1l);
    participant = persistenceManager.get(Participant.class, 1l);

    // Start an InstrumentRun.
    InstrumentType instrumentType = persistenceManager.get(InstrumentType.class, 7l);
    InstrumentRun instrumentRun = startInstrumentRun(participant, instrumentType);

    // Set all parameters to an out-of-range value.
    for(InstrumentParameter parameter : instrumentType.getInstrumentParameters()) {
      setParameterValue(instrumentRun, parameter, DataBuilder.buildInteger(0l));
    }

    initInstrumentServiceMock(instrumentType);

    // Verify the number of InstrumentOutputParameters with warnings.
    Assert.assertEquals(1, activeInstrumentRunService.getParametersWithWarning().size());
  }

  //
  // Helper Methods
  //

  private void resetMocks() {
    reset(userSessionService);
    reset(instrumentService);
  }

  private void initInstrumentServiceMock(InstrumentType instrumentType) {
    expect(instrumentService.getInstrumentType(instrumentType.getName())).andReturn(instrumentType).anyTimes();
    replay(instrumentService);
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

  private void setParameterValue(InstrumentRun instrumentRun, InstrumentParameter parameter, Data value) {
    InstrumentRunValue runValue = new InstrumentRunValue();
    runValue.setInstrumentRun(instrumentRun);
    runValue.setInstrumentParameter(parameter);
    runValue.setData(value);
    persistenceManager.save(runValue);
  }
}
