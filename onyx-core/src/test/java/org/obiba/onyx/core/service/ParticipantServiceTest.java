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

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
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
  ParticipantService participantService;

  @Test
  @Dataset
  public void testParticipantByCode() {
    Assert.assertEquals(1, participantService.countParticipantsByCode("1"));
    Assert.assertEquals(2, participantService.countParticipantsByCode("100002"));
  }

  @Test
  @Dataset
  public void testParticipantByInterviewStatus() {
    Assert.assertEquals(1, participantService.countParticipants(InterviewStatus.COMPLETED));
    Assert.assertEquals(1, participantService.countParticipants(InterviewStatus.IN_PROGRESS));
  }

  @Test
  @Dataset
  public void testParticipantByName() {
    Assert.assertEquals(2, participantService.countParticipantsByName("Hudson"));
    Assert.assertEquals(1, participantService.countParticipantsByName("John Hudson"));
    Assert.assertEquals(2, participantService.countParticipantsByName("ohn"));
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

    // Verify that the bar code was persisted.
    participant = persistenceManager.get(Participant.class, 4l);
    Assert.assertEquals(barcode, participant.getBarcode());
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

    // Verify that the bar code was persisted.
    participant = persistenceManager.get(Participant.class, participantId);
    Assert.assertEquals(barcode, participant.getBarcode());

    // Verify that the comment was persisted.
    Action commentTemplate = new Action();
    commentTemplate.setActionType(ActionType.COMMENT);
    commentTemplate.setComment(receptionComment);
    commentTemplate.setUser(user);
    commentTemplate.setInterview(participant.getInterview());
    List<Action> comments = persistenceManager.match(commentTemplate, new PagingClause(0));
    Assert.assertTrue(comments.size() == 1);
    Assert.assertEquals(comments.get(0).getComment(), receptionComment);
  }

  @Test
  @Dataset(filenames = { "AppConfigurationForParticipantServiceTest.xml" })
  public void testParticipantReader() {
    try {
      participantService.updateParticipants(getClass().getResourceAsStream("rendez-vous.xls"));
      Assert.assertEquals(3l, persistenceManager.match(new Participant()).size());

      // test we can run same file multiple times without breaking the db
      participantService.updateParticipants(getClass().getResourceAsStream("rendez-vous.xls"));
      Assert.assertEquals(3l, persistenceManager.match(new Participant()).size());
      Participant p = new Participant();
      p.setEnrollmentId("100001");
      p = persistenceManager.matchOne(p);
      Assert.assertNotNull("Cannot find participant", p);
      Assert.assertNotNull("Cannot find participant appointment", p.getAppointment());
      Assert.assertNotNull("Cannot find participant appointment date", p.getAppointment().getDate());

      // add a completed interview
      p.setBarcode("1");
      persistenceManager.save(p);
      Interview interview = new Interview();
      interview.setStatus(InterviewStatus.COMPLETED);
      interview.setParticipant(p);
      persistenceManager.save(interview);
      p.setInterview(interview);
      persistenceManager.save(p);
      p = new Participant();
      p.setEnrollmentId("100001");
      p = persistenceManager.matchOne(p);
      Assert.assertNotNull("Cannot find participant", p);
      Assert.assertNotNull("Cannot find participant interview", p.getInterview());
      Assert.assertEquals("Cannot find participant completed interview", InterviewStatus.COMPLETED, p.getInterview().getStatus());

      participantService.updateParticipants(getClass().getResourceAsStream("rendez-vous.xls"));
      Assert.assertEquals(3l, persistenceManager.match(new Participant()).size());
      p = new Participant();
      p.setEnrollmentId("100001");
      p = persistenceManager.matchOne(p);
      Assert.assertNotNull("Cannot find participant", p);
      Assert.assertNotNull("Cannot find participant appointment", p.getAppointment());
      Assert.assertNotNull("Cannot find participant appointment date", p.getAppointment().getDate());
      Assert.assertNotNull("Cannot find participant interview", p.getInterview());
      Assert.assertEquals("Cannot find participant completed interview", InterviewStatus.COMPLETED, p.getInterview().getStatus());

    } catch(ValidationRuntimeException e) {
      Assert.fail(e.getMessage());
      e.printStackTrace();
    }

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
}
