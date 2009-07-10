/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.etl.participant.IParticipantReadListener;
import org.obiba.onyx.core.etl.participant.IParticipantReader;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.UserSessionService;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * 
 */
public class DefaultAppointmentManagementServiceImplTest {

  private ApplicationConfiguration config;

  private ApplicationConfigurationService configServiceMock;

  private UserSessionService userSessionServiceMock;

  private ParticipantService participantServiceMock;

  private IParticipantReader participantReaderMock;

  private DefaultAppointmentManagementServiceImpl appointmentServiceImpl;

  @Before
  public void setUp() {
    configServiceMock = createMock(ApplicationConfigurationService.class);
    userSessionServiceMock = createMock(UserSessionService.class);
    participantServiceMock = createMock(ParticipantService.class);
    participantReaderMock = createMock(IParticipantReader.class);

    appointmentServiceImpl = new DefaultAppointmentManagementServiceImpl();
    appointmentServiceImpl.setResourceLoader(new PathMatchingResourcePatternResolver());
    appointmentServiceImpl.setParticipantService(participantServiceMock);
    appointmentServiceImpl.setUserSessionService(userSessionServiceMock);
    appointmentServiceImpl.setApplicationConfigurationService(configServiceMock);
    appointmentServiceImpl.setParticipantReader(participantReaderMock);

    config = new ApplicationConfiguration();
    config.setSiteNo("cag001");
  }

  @Test
  public void testInitializeNoInputDirectoryString() {
    try {
      appointmentServiceImpl.initialize();
      fail("Should get IllegalArgumentException.");
    } catch(IllegalArgumentException e) {
      Assert.assertEquals("DefaultAppointmentManagementServiceImpl: InputDirectory should not be null", e.getMessage());
    }
  }

  @Test
  public void testInitializeEmptyInputDirectoryString() {
    appointmentServiceImpl.setInputDirectory("");

    try {
      appointmentServiceImpl.initialize();
      fail("Should get IllegalArgumentException.");
    } catch(IllegalArgumentException e) {
      Assert.assertEquals("DefaultAppointmentManagementServiceImpl: InputDirectory should not be null", e.getMessage());
    }
  }

  @Test
  public void testInitializeInputDirNotFound() {
    appointmentServiceImpl.setInputDirectory("TESTInputDir");

    try {
      appointmentServiceImpl.initialize();
      fail("Should get RuntimeException.");
    } catch(RuntimeException e) {
      Assert.assertEquals("DefaultAppointmentManagementServiceImpl: Failed to access directory - java.io.FileNotFoundException: class path resource [TESTInputDir] cannot be resolved to URL because it does not exist", e.getMessage());
    }
  }

  @Test
  public void testInitializeInputDir() {
    appointmentServiceImpl.setInputDirectory("file:./src/test/resources/appointments/in");
    appointmentServiceImpl.initialize();

    Assert.assertNotNull(appointmentServiceImpl.getInputDir());
    Assert.assertNull(appointmentServiceImpl.getOutputDir());
  }

  @Test
  public void testIsUpdateAvailableFalse() {
    expect(participantReaderMock.accept((File) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(false).anyTimes();
    replay(participantReaderMock);
    appointmentServiceImpl.setInputDirectory("file:./src/test/resources/appointments/inNoData");
    appointmentServiceImpl.initialize();
    Assert.assertEquals(false, appointmentServiceImpl.isUpdateAvailable());
    verify(participantReaderMock);
  }

  @Test
  public void testIsUpdateAvailableTrue() {
    expect(participantReaderMock.accept((File) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(true).anyTimes();
    replay(participantReaderMock);
    appointmentServiceImpl.setInputDirectory("file:./src/test/resources/appointments/in");
    appointmentServiceImpl.initialize();
    Assert.assertEquals(true, appointmentServiceImpl.isUpdateAvailable());
    verify(participantReaderMock);
  }

  @Test
  public void testSortFilesOnDateAsc() {
    expect(participantReaderMock.accept((File) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(true).anyTimes();
    replay(participantReaderMock);

    appointmentServiceImpl.setInputDirectory("file:./src/test/resources/appointments/in");
    appointmentServiceImpl.initialize();

    File[] appointmentFiles = appointmentServiceImpl.getInputDir().listFiles(appointmentServiceImpl.getFilter());
    long now = System.currentTimeMillis();

    // Set the file timestamps of the appointment files, spaced at one-minute intervals.
    // Explicitly setting the timestamps prevents the test from failing if the files'
    // timestamps are modified when checked into source control.
    for(int i = 0; i < appointmentFiles.length; i++) {
      appointmentFiles[i].setLastModified(now - 1000 * 60 * i);
    }

    File[] sortedAppointmentFiles = new File[appointmentFiles.length];
    System.arraycopy(appointmentFiles, 0, sortedAppointmentFiles, 0, appointmentFiles.length);
    appointmentServiceImpl.sortFilesOnDateAsc(sortedAppointmentFiles);

    // Verify that the appointment files are sorted by date, in ascending order (earliest first).
    for(int i = 0; i < appointmentFiles.length; i++) {
      Assert.assertEquals(appointmentFiles[appointmentFiles.length - i - 1].getName(), sortedAppointmentFiles[i].getName());
    }
    verify(participantReaderMock);
  }

  @Test
  // Needs to be fixed
  public void testUpdateAppointments() throws IllegalArgumentException, IOException {
    setDirectories();

    expect(userSessionServiceMock.getUser()).andReturn(getUser());
    expect(configServiceMock.getApplicationConfiguration()).andReturn(config);
    participantServiceMock.cleanUpAppointment();
    participantReaderMock.process((FileInputStream) EasyMock.anyObject(), (List<IParticipantReadListener>) EasyMock.anyObject());
    expectLastCall().times(1);
    expect(participantReaderMock.accept((File) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(true).times(10);
    expect(participantReaderMock.accept((File) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(false).times(2);

    replay(userSessionServiceMock);
    replay(configServiceMock);
    replay(participantServiceMock);
    replay(participantReaderMock);

    appointmentServiceImpl.updateAppointments();
    Assert.assertTrue(appointmentServiceImpl.getInputDir().list(appointmentServiceImpl.getFilter()).length == 0);
    Assert.assertTrue(appointmentServiceImpl.getOutputDir().list(appointmentServiceImpl.getFilter()).length == 2);

    verify(userSessionServiceMock);
    verify(configServiceMock);
    verify(participantServiceMock);
    verify(participantReaderMock);

  }

  private void setDirectories() {
    File targetRootDirectory = new File("target", "appointments");
    File targetInputDirectory = new File(targetRootDirectory, "in");
    targetInputDirectory.mkdirs();

    File targetOutputDirectory = new File(targetRootDirectory, "out");
    targetOutputDirectory.mkdirs();

    try {
      FileUtil.copyDirectory(new File("src/test/resources/appointments/in"), targetInputDirectory);
    } catch(IOException ex) {
      System.out.println(ex.getMessage());
    }

    appointmentServiceImpl.setInputDirectory("file:./target/appointments/in");
    appointmentServiceImpl.setOutputDirectory("file:./target/appointments/out");
    appointmentServiceImpl.initialize();
  }

  private User getUser() {
    User u = new User();
    u.setLastName("Onyx");
    u.setFirstName("Admin");
    return u;
  }

}
