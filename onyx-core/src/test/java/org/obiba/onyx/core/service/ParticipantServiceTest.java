package org.obiba.onyx.core.service;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
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
    
    log.info("from=" + from + " to="+to);
    Assert.assertEquals(2, participantService.countParticipants(from, to));
  }
}
