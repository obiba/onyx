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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.onyx.core.domain.user.User;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * 
 */
public class DefaultAppointmentManagementServiceImplTest {

  // private ApplicationConfiguration config;

  // private ApplicationConfigurationService configServiceMock;
  //
  // private UserSessionService userSessionServiceMock;
  //
  // private ParticipantService participantServiceMock;
  //
  // private ParticipantReader participantReaderMock;

  private DefaultAppointmentManagementServiceImpl appointmentServiceImpl;

  @Before
  public void setUp() {
    // configServiceMock = createMock(ApplicationConfigurationService.class);
    // userSessionServiceMock = createMock(UserSessionService.class);
    // participantServiceMock = createMock(ParticipantService.class);
    // participantReaderMock = createMock(ParticipantReader.class);

    appointmentServiceImpl = new DefaultAppointmentManagementServiceImpl();
    appointmentServiceImpl.setResourceLoader(new PathMatchingResourcePatternResolver());

    // config = new ApplicationConfiguration();
    // config.setSiteNo("cag001");
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

  // @Test
  public void testUpdateAppointments() throws IllegalArgumentException, IOException {
    // TODO: Implement this test
    // setDirectories();
    //
    // expect(userSessionServiceMock.getUser()).andReturn(getUser());
    // expect(configServiceMock.getApplicationConfiguration()).andReturn(config);
    // // participantServiceMock.cleanUpAppointment();
    // participantReaderMock.process((FileInputStream) EasyMock.anyObject(), (List<IParticipantReadListener>)
    // EasyMock.anyObject());
    // expectLastCall().times(1);
    // expect(participantReaderMock.accept((File) EasyMock.anyObject(), (String)
    // EasyMock.anyObject())).andReturn(true).times(6);
    //
    // replay(userSessionServiceMock);
    // replay(configServiceMock);
    // replay(participantServiceMock);
    // replay(participantReaderMock);
    //
    // appointmentServiceImpl.updateAppointments();
    // Assert.assertTrue(appointmentServiceImpl.getInputDir().list(appointmentServiceImpl.getFilter()).length == 0);
    // Assert.assertTrue(appointmentServiceImpl.getOutputDir().list(appointmentServiceImpl.getFilter()).length == 2);
    //
    // verify(userSessionServiceMock);
    // verify(configServiceMock);
    // verify(participantServiceMock);
    // verify(participantReaderMock);

  }

  // @Test
  // @Dataset
  public void testSaveAppointmentUpdateStats() {
    // TODO: Implement this test
  }

  private void setDirectories() {
    File targetRootDirectory = new File("target", "appointments");
    File targetInputDirectory = new File(targetRootDirectory, "in");
    targetInputDirectory.mkdirs();

    File targetOutputDirectory = new File(targetRootDirectory, "out");
    targetOutputDirectory.mkdirs();

    File sourceDirectory = new File("src/test/resources/appointments/in");
    try {
      for(File file : sourceDirectory.listFiles()) {
        if(file.getName().toLowerCase().endsWith(".xls")) FileUtil.copyFile(file, targetInputDirectory);
      }
    } catch(IOException ex) {
      System.out.println(ex.getMessage());
    }

    appointmentServiceImpl.setInputDirectory("file:" + targetInputDirectory.getAbsolutePath().replace('\\', '/'));
    appointmentServiceImpl.setOutputDirectory("file:" + targetOutputDirectory.getAbsolutePath().replace('\\', '/'));
    appointmentServiceImpl.initialize();
  }

  private User getUser() {
    User u = new User();
    u.setLastName("Onyx");
    u.setFirstName("Admin");
    return u;
  }

}
