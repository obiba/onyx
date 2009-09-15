/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.impl;

import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.statistics.AppointmentUpdateStats;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;

/**
 * 
 */
public class AppointmentListUpdateListenerTest {

  private AppointmentManagementService appointmentManagementServiceMock;

  private AppointmentListUpdateListener appointmentListUpdateListener = new AppointmentListUpdateListener();

  @Before
  public void setup() {
    appointmentManagementServiceMock = EasyMock.createMock(AppointmentManagementService.class);
    appointmentListUpdateListener.setAppointmentManagementService(appointmentManagementServiceMock);
  }

  @Test
  public void testOnParticipantReadError() {
    try {
      appointmentListUpdateListener.onParticipantReadError();
      appointmentListUpdateListener.onParticipantReadError();
      appointmentListUpdateListener.onParticipantReadError();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    Assert.assertEquals(0, appointmentListUpdateListener.getAddedParticipants());
    Assert.assertEquals(0, appointmentListUpdateListener.getUpdatedParticipants());
    Assert.assertEquals(0, appointmentListUpdateListener.getIgnoredParticipants());
    Assert.assertEquals(3, appointmentListUpdateListener.getUnreadableParticipants());
  }

  @Test
  public void testAfterParticipantProcessed() {
    try {
      appointmentListUpdateListener.afterParticipantProcessed(getNewParticipant(), null);
      appointmentListUpdateListener.afterParticipantProcessed(getNewParticipant(), null);
      appointmentListUpdateListener.afterParticipantProcessed(getNewParticipant(), null);
      appointmentListUpdateListener.afterParticipantProcessed(getExistParticipant("1"), getExistParticipant("1"));
      appointmentListUpdateListener.afterParticipantProcessed(getExistParticipant("2"), getExistParticipant("2"));
      appointmentListUpdateListener.afterParticipantProcessed(null, null);
      appointmentListUpdateListener.afterParticipantProcessed(getNewParticipant(), getExistParticipant("3"));
      appointmentListUpdateListener.afterParticipantProcessed(getNewParticipant(), getExistParticipant("4"));
      appointmentListUpdateListener.afterParticipantProcessed(getNewParticipant(), getExistParticipant("5"));
      appointmentListUpdateListener.afterParticipantProcessed(getNewParticipant(), getExistParticipant("6"));
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

    Assert.assertEquals(4, appointmentListUpdateListener.getAddedParticipants());
    Assert.assertEquals(2, appointmentListUpdateListener.getUpdatedParticipants());
    Assert.assertEquals(3, appointmentListUpdateListener.getIgnoredParticipants());
    Assert.assertEquals(0, appointmentListUpdateListener.getUnreadableParticipants());
  }

  @Test
  public void testAfterUpdateCompleted() {
    Map<String, JobParameter> jobParameterMap = new HashMap<String, JobParameter>();
    jobParameterMap.put("date", new JobParameter(new Date()));
    JobInstance job = new JobInstance(1l, new JobParameters(jobParameterMap), "jobTest");
    StepExecution stepExecution = new StepExecution("complétion", new JobExecution(job));
    stepExecution.setExitStatus(ExitStatus.COMPLETED);
    ExecutionContext context = new ExecutionContext();
    context.put("fileName", "fileName.xls");
    stepExecution.setExecutionContext(context);

    appointmentManagementServiceMock.saveAppointmentUpdateStats((AppointmentUpdateStats) EasyMock.anyObject());

    replay(appointmentManagementServiceMock);
    appointmentListUpdateListener.afterUpdateCompleted(stepExecution);
    verify(appointmentManagementServiceMock);

  }

  private Participant getNewParticipant() {
    Participant p = new Participant();

    p.setBirthDate(getDate(1964, 10, 1, 0, 0));
    p.setEnrollmentId("100001");
    p.setFirstName("Chantal");
    p.setGender(Gender.FEMALE);
    p.setLastName("Tremblay");
    p.setSiteNo("cag001");

    Appointment a = new Appointment(p, getDate(2009, 9, 1, 9, 0));
    a.setAppointmentCode("100001");
    p.setAppointment(a);

    return p;
  }

  private Participant getExistParticipant(String id) {
    Participant p = getNewParticipant();
    p.setId(id);
    return p;
  }

  private Date getDate(int year, int month, int day, int hour, int minute) {
    Calendar c = Calendar.getInstance();

    c.set(Calendar.YEAR, year);
    c.set(Calendar.MONTH, month - 1);
    c.set(Calendar.DAY_OF_MONTH, day);
    c.set(Calendar.HOUR_OF_DAY, hour);
    c.set(Calendar.MINUTE, minute);

    return c.getTime();
  }
}
