/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.core.test.spring.DatasetOperationType;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttributeValue;
import org.obiba.onyx.core.domain.stage.StageExecutionMemento;
import org.obiba.onyx.core.domain.stage.StageTransition;
import org.obiba.onyx.core.domain.statistics.InterviewDeletionLog;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.impl.DefaultParticipantServiceImpl;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ParticipantServiceTest extends BaseDefaultSpringContextTestCase {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ParticipantServiceTest.class);

  @Autowired(required = true)
  PersistenceManager persistenceManager;

  @Autowired(required = true)
  DefaultParticipantServiceImpl participantService;

  UserSessionService mockUserSessionService;

  @Before
  public void setUp() {
    mockUserSessionService = EasyMock.createMock(UserSessionService.class);
    participantService.setUserSessionService(mockUserSessionService);
  }

  @Test
  @Dataset
  public void testParticipantByInputField() {
    Assert.assertEquals(1, participantService.countParticipantsByInputField("1"));
    Assert.assertEquals(2, participantService.countParticipantsByInputField("100002"));
    Assert.assertEquals(2, participantService.countParticipantsByInputField("Hudson"));
    Assert.assertEquals(1, participantService.countParticipantsByInputField("John Hudson"));
    Assert.assertEquals(2, participantService.countParticipantsByInputField("ohn"));
  }

  @Test
  @Dataset
  public void testParticipantByInterviewStatus() {
    Assert.assertEquals(1, participantService.countParticipants(InterviewStatus.COMPLETED));
    Assert.assertEquals(4, participantService.countParticipants(InterviewStatus.IN_PROGRESS));
  }

  @Test
  @Dataset
  public void testParticipantByAppointmentDate() {
    Calendar cal = Calendar.getInstance();
    cal.set(2008, 8, 1, 0, 0, 0);
    Date from = cal.getTime();
    cal.add(Calendar.DAY_OF_MONTH, 1);
    Date to = cal.getTime();

    log.info("from=" + from + " to=" + to);
    Assert.assertEquals(2, participantService.countParticipants(from, to));
  }

  @Test
  @Dataset
  public void testAssignCodeToParticipantWithNoComment() {
    long participantId = 4l;
    long userId = 1l; // administrator
    String barcode = "100004";

    Participant participant = persistenceManager.get(Participant.class, participantId);
    Assert.assertNotNull(participant);

    User user = persistenceManager.get(User.class, userId);
    Assert.assertNotNull(user);

    participantService.assignCodeToParticipant(participant, barcode, null, user);

    // Verify that the barcode was persisted.
    participant = persistenceManager.get(Participant.class, 4l);
    Assert.assertEquals(barcode, participant.getBarcode());

    // Verify that the action was persisted.
    Action template = new Action();
    template.setActionType(ActionType.START);
    template.setUser(user);
    template.setInterview(participant.getInterview());
    List<Action> actions = persistenceManager.match(template, new PagingClause(0));
    Assert.assertTrue(actions.size() == 1);
    Assert.assertNull(actions.get(0).getComment());
  }

  @Test
  @Dataset
  public void testAssignCodeToParticipantWithComment() {
    long participantId = 4l;
    long userId = 1l; // administrator
    String barcode = "100004";
    String receptionComment = "test comment";

    Participant participant = persistenceManager.get(Participant.class, participantId);
    Assert.assertNotNull(participant);

    User user = persistenceManager.get(User.class, userId);
    Assert.assertNotNull(user);

    participantService.assignCodeToParticipant(participant, barcode, receptionComment, user);

    // Verify that the barcode was persisted.
    participant = persistenceManager.get(Participant.class, participantId);
    Assert.assertEquals(barcode, participant.getBarcode());

    // Verify that the comment was persisted.
    Action commentTemplate = new Action();
    commentTemplate.setActionType(ActionType.START);
    commentTemplate.setComment(receptionComment);
    commentTemplate.setUser(user);
    commentTemplate.setInterview(participant.getInterview());
    List<Action> comments = persistenceManager.match(commentTemplate, new PagingClause(0));
    Assert.assertTrue(comments.size() == 1);
    Assert.assertEquals(comments.get(0).getComment(), receptionComment);
  }

  @Test
  @Dataset
  public void testUpdateConfiguredAttribute() {
    long participantId = 4l;
    String attributeName = "phone";
    String attributeValue1 = "450-466-6666";
    String attributeValue2 = "514-488-8888";

    // Fetch the test participant.
    Participant participant = persistenceManager.get(Participant.class, participantId);
    Assert.assertNotNull(participant);

    // Update the value of the configured attribute to attributeValue1.
    participant.setConfiguredAttributeValue(attributeName, DataBuilder.buildText(attributeValue1));
    participantService.updateParticipant(participant);

    // Re-fetch the test participant.
    participant = persistenceManager.get(Participant.class, participantId);

    // Verify that the configured attribute has been updated to the new value.
    Assert.assertNotNull(participant.getConfiguredAttributeValue(attributeName));
    Assert.assertEquals(attributeValue1, participant.getConfiguredAttributeValue(attributeName).getValue());

    // Now update the value to null.
    participant.setConfiguredAttributeValue(attributeName, null);
    participantService.updateParticipant(participant);

    // Re-fetch the test participant.
    participant = persistenceManager.get(Participant.class, participantId);

    // Verify that the configured attribute has been updated to null.
    Assert.assertNull(participant.getConfiguredAttributeValue(attributeName));

    // Finally update the value to attributeValue2.
    participant.setConfiguredAttributeValue(attributeName, DataBuilder.buildText(attributeValue2));
    participantService.updateParticipant(participant);

    // Re-fetch the test participant.
    participant = persistenceManager.get(Participant.class, participantId);

    // Verify that the configured attribute has been updated to the new value.
    Assert.assertNotNull(participant.getConfiguredAttributeValue(attributeName));
    Assert.assertEquals(attributeValue2, participant.getConfiguredAttributeValue(attributeName).getValue());
  }

  @Test
  @Dataset
  public void testGetParticipant() {
    Participant template = new Participant();
    template.setLastName("Dupont");
    template.setEnrollmentId("100003");

    Participant participant = participantService.getParticipant(template);
    Assert.assertNull(participant);

    template.setLastName("Hudson");
    participant = participantService.getParticipant(template);
    Assert.assertEquals("Participant[100002]", participant.toString());

  }

  @Test
  @Dataset
  public void testCleanUpAppointment() {
    Assert.assertEquals(9, persistenceManager.count(Participant.class));
    participantService.cleanUpAppointment();
    // Participant with ids 1, 2 and 4 are kept
    Assert.assertEquals(3, persistenceManager.count(Participant.class));
  }

  @Test
  @Dataset(beforeOperation = DatasetOperationType.CLEAN_INSERT)
  public void testDeleteParticipant() {

    // Check data before delete
    Assert.assertNotNull(persistenceManager.get(Participant.class, 1l));
    Assert.assertNotNull(persistenceManager.get(Participant.class, 2l));
    Assert.assertNotNull(persistenceManager.get(Participant.class, 3l));
    Assert.assertNotNull(persistenceManager.get(Participant.class, 4l));

    Assert.assertNotNull(persistenceManager.get(Appointment.class, 1l));
    Assert.assertNotNull(persistenceManager.get(Appointment.class, 2l));
    Assert.assertNotNull(persistenceManager.get(Appointment.class, 3l));
    Assert.assertNotNull(persistenceManager.get(Appointment.class, 4l));

    Assert.assertNotNull(persistenceManager.get(Interview.class, 1l));
    Assert.assertNotNull(persistenceManager.get(Interview.class, 2l));
    Assert.assertNull(persistenceManager.get(Interview.class, 3l));
    Assert.assertNull(persistenceManager.get(Interview.class, 4l));

    Assert.assertNotNull(persistenceManager.get(ParticipantAttributeValue.class, 1l));
    Assert.assertNotNull(persistenceManager.get(ParticipantAttributeValue.class, 2l));

    Assert.assertNotNull(persistenceManager.get(Action.class, 1l));
    Assert.assertNotNull(persistenceManager.get(Action.class, 2l));

    Assert.assertNotNull(persistenceManager.get(StageTransition.class, 1l));
    Assert.assertNotNull(persistenceManager.get(StageTransition.class, 2l));
    Assert.assertNotNull(persistenceManager.get(StageTransition.class, 3l));
    Assert.assertNotNull(persistenceManager.get(StageTransition.class, 4l));

    Assert.assertNotNull(persistenceManager.get(StageExecutionMemento.class, 1l));
    Assert.assertNotNull(persistenceManager.get(StageExecutionMemento.class, 2l));

    Assert.assertNull(persistenceManager.get(InterviewDeletionLog.class, 1l));

    User user = new User();
    user.setFirstName("firstName");
    user.setLastName("lastName");
    user.setLogin("login");

    EasyMock.expect(mockUserSessionService.getUser()).andReturn(user).times(2);
    EasyMock.replay(mockUserSessionService);

    // Delete the participant and his data
    participantService.deleteParticipant(persistenceManager.get(Participant.class, 1l));

    EasyMock.verify(mockUserSessionService);

    // Check data after delete
    Assert.assertNull(persistenceManager.get(Participant.class, 1l));
    Assert.assertNotNull(persistenceManager.get(Participant.class, 2l));
    Assert.assertNotNull(persistenceManager.get(Participant.class, 3l));
    Assert.assertNotNull(persistenceManager.get(Participant.class, 4l));

    Assert.assertNull(persistenceManager.get(Appointment.class, 1l));
    Assert.assertNotNull(persistenceManager.get(Appointment.class, 2l));
    Assert.assertNotNull(persistenceManager.get(Appointment.class, 3l));
    Assert.assertNotNull(persistenceManager.get(Appointment.class, 4l));

    Assert.assertNull(persistenceManager.get(Interview.class, 1l));
    Assert.assertNotNull(persistenceManager.get(Interview.class, 2l));
    Assert.assertNull(persistenceManager.get(Interview.class, 3l));
    Assert.assertNull(persistenceManager.get(Interview.class, 4l));

    Assert.assertNull(persistenceManager.get(ParticipantAttributeValue.class, 1l));
    Assert.assertNull(persistenceManager.get(ParticipantAttributeValue.class, 2l));

    Assert.assertNull(persistenceManager.get(Action.class, 1l));
    Assert.assertNull(persistenceManager.get(Action.class, 2l));

    Assert.assertNull(persistenceManager.get(StageTransition.class, 1l));
    Assert.assertNull(persistenceManager.get(StageTransition.class, 2l));
    Assert.assertNull(persistenceManager.get(StageTransition.class, 3l));
    Assert.assertNull(persistenceManager.get(StageTransition.class, 4l));

    Assert.assertNull(persistenceManager.get(StageExecutionMemento.class, 1l));
    Assert.assertNull(persistenceManager.get(StageExecutionMemento.class, 2l));

    Assert.assertNotNull(persistenceManager.get(InterviewDeletionLog.class, 1l));

  }

}
