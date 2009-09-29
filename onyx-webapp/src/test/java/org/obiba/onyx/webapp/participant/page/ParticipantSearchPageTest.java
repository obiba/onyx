/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.page;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.ITestPageSource;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.etl.participant.impl.ParticipantReader;
import org.obiba.onyx.core.service.AppointmentManagementService;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.variable.export.OnyxDataExport;
import org.obiba.onyx.print.IPrintableReport;
import org.obiba.onyx.print.PrintableReportsRegistry;
import org.obiba.onyx.wicket.test.ExtendedApplicationContextMock;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 */
public class ParticipantSearchPageTest {

  private transient WicketTester tester;

  private ParticipantService mockParticipantService;

  private EntityQueryService mockEntityQueryService;

  private UserSessionService mockUserSessionService;

  private InterviewManager mockInterviewManager;

  private ParticipantMetadata mockParticipantMetadata;

  private OnyxDataExportMock mockOnyxDataExport;

  private AppointmentManagementService mockAppointmentManagementService;

  private PrintableReportsRegistry mockPrintableReportsRegistery;

  private ParticipantReader mockParticipantReader;

  private List<Participant> participants;

  @Before
  public void setup() {
    ExtendedApplicationContextMock mockCtx = new ExtendedApplicationContextMock();

    mockParticipantService = createMock(ParticipantService.class);
    mockEntityQueryService = createMock(EntityQueryService.class);
    mockUserSessionService = createMock(UserSessionService.class);
    mockInterviewManager = createMock(InterviewManager.class);
    mockAppointmentManagementService = createMock(AppointmentManagementService.class);
    mockOnyxDataExport = new OnyxDataExportMock();
    mockPrintableReportsRegistery = createMock(PrintableReportsRegistry.class);

    mockCtx.putBean("participantService", mockParticipantService);
    mockCtx.putBean("entityQueryService", mockEntityQueryService);
    mockCtx.putBean("userSessionService", mockUserSessionService);
    mockCtx.putBean("interviewManager", mockInterviewManager);
    mockCtx.putBean("appointmentManagementService", mockAppointmentManagementService);
    mockCtx.putBean("onyxDataExport", mockOnyxDataExport);
    mockCtx.putBean("printableReportsRegistry", mockPrintableReportsRegistery);

    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("test-spring-context.xml");
    mockParticipantMetadata = (ParticipantMetadata) context.getBean("participantMetadata");
    mockCtx.putBean("participantMetadata", mockParticipantMetadata);

    mockParticipantReader = (ParticipantReader) context.getBean("participantReader");
    mockCtx.putBean("participantReader", mockParticipantReader);

    MockSpringApplication application = new MockSpringApplication();
    application.setApplicationContext(mockCtx);
    application.setHomePage(Page.class);

    tester = new WicketTester(application);
    participants = newParticipantList();
  }

  @Test
  public void testSearchByInputFieldParticipant() {

    // methods expected on the page initialization for the first participant list
    expect(mockParticipantService.getParticipants((Date) EasyMock.anyObject(), (Date) EasyMock.anyObject(), (PagingClause) EasyMock.anyObject(), (SortingClause) EasyMock.anyObject())).andReturn(new ArrayList<Participant>());
    expect(mockParticipantService.countParticipants((Date) EasyMock.anyObject(), (Date) EasyMock.anyObject())).andReturn(0);

    // methods expected after pressing the search button
    expect(mockParticipantService.countParticipantsByInputField("Tremblay")).andReturn(2);
    expect(mockParticipantService.getParticipantsByInputField((String) EasyMock.anyObject(), (PagingClause) EasyMock.anyObject(), (SortingClause) EasyMock.anyObject())).andReturn(participants);

    expect(mockEntityQueryService.refresh(EasyMock.anyObject())).andReturn(participants.get(0));
    expect(mockEntityQueryService.refresh(EasyMock.anyObject())).andReturn(participants.get(1));
    expect(mockUserSessionService.getDateFormat()).andReturn(new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)).anyTimes();
    expect(mockUserSessionService.getDateTimeFormat()).andReturn(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)).anyTimes();

    expect(mockInterviewManager.isInterviewAvailable((Participant) EasyMock.anyObject())).andReturn(true).anyTimes();
    expect(mockPrintableReportsRegistery.availableReports()).andReturn(Collections.<IPrintableReport> emptySet()).anyTimes();

    replay(mockParticipantService);
    replay(mockEntityQueryService);
    replay(mockUserSessionService);
    replay(mockInterviewManager);
    replay(mockPrintableReportsRegistery);

    tester.startPage(new ITestPageSource() {
      private static final long serialVersionUID = 1L;

      public Page getTestPage() {
        return new ParticipantSearchPage();
      }
    });

    FormTester formTester = tester.newFormTester("searchForm");
    formTester.setValue("inputField", "Tremblay");

    tester.assertComponent("searchForm:inputField", TextField.class);
    tester.executeAjaxEvent("searchForm:searchByInputField", "onclick");
    tester.assertNoErrorMessage();

    verify(mockParticipantService);
    verify(mockEntityQueryService);
    verify(mockUserSessionService);
    verify(mockInterviewManager);
  }

  private List<Participant> newParticipantList() {
    List<Participant> participants = new ArrayList<Participant>();

    Participant p = new Participant();
    p.setBarcode("1000001");
    p.setLastName("Tremblay");
    p.setFirstName("Chantal");
    p.setGender(Gender.FEMALE);
    p.setBirthDate(getDate(1966, 5, 7, 0, 0));
    p.setEnrollmentId("1000001");
    p.setId(1);
    p.setRecruitmentType(RecruitmentType.ENROLLED);
    p.setExported(false);

    Appointment a = new Appointment(p, getDate(2009, 9, 1, 9, 0));
    a.setAppointmentCode("1000001");
    p.setAppointment(a);

    Interview i = new Interview();
    i.setId(1);
    i.setStatus(InterviewStatus.IN_PROGRESS);
    p.setInterview(i);
    participants.add(p);

    Participant p2 = new Participant();
    p2.setBarcode("1000002");
    p2.setLastName("Tremblay");
    p2.setFirstName("Suzan");
    p2.setGender(Gender.FEMALE);
    p2.setBirthDate(getDate(1968, 10, 8, 0, 0));
    p2.setEnrollmentId("1000002");
    p2.setId(1);
    p2.setRecruitmentType(RecruitmentType.ENROLLED);
    p2.setExported(false);

    Appointment a2 = new Appointment(p, getDate(2009, 9, 1, 10, 0));
    a2.setAppointmentCode("1000002");
    p2.setAppointment(a2);

    Interview i2 = new Interview();
    i2.setId(2);
    i2.setStatus(InterviewStatus.IN_PROGRESS);
    p2.setInterview(i2);
    participants.add(p2);

    return participants;
  }

  private Date getDate(int year, int month, int day, int hour, int minute) {
    Calendar c = Calendar.getInstance();

    c.set(Calendar.YEAR, year);
    c.set(Calendar.MONTH, month);
    c.set(Calendar.DAY_OF_MONTH, day);
    c.set(Calendar.HOUR_OF_DAY, hour);
    c.set(Calendar.MINUTE, minute);

    return c.getTime();
  }

  private class OnyxDataExportMock extends OnyxDataExport {
    public OnyxDataExportMock() {
      super();
    }
  }
}
