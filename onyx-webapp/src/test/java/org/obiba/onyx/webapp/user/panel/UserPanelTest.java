/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.user.panel;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.value.ValueMap;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserService;
import org.obiba.wicket.test.MockSpringApplication;

public class UserPanelTest {

  private WicketTester tester;

  private UserService userServiceMock;

  private UserPanel userPanel;

  @Before
  public void setUp() {
    ApplicationContextMock mockCtx = new ApplicationContextMock();
    userServiceMock = createMock(UserService.class);

    mockCtx.putBean("userService", userServiceMock);

    MockSpringApplication application = new MockSpringApplication();
    application.setHomePage(Page.class);
    application.setApplicationContext(mockCtx);
    tester = new WicketTester(application);
  }

  @Test
  public void testRetrieveUser() {
    final User u = newUserTest();

    expect(userServiceMock.getRoles((SortingClause) EasyMock.anyObject())).andReturn(newRoleListTest());

    replay(userServiceMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return (new UserPanel(panelId, new Model(u), new ModalWindow("windowMock")));
      }
    });

    FormTester formTester = tester.newFormTester("panel:userPanelForm");

    verify(userServiceMock);

    Assert.assertEquals("ndupont", formTester.getTextComponentValue("login"));
    Assert.assertEquals("Dupont", formTester.getTextComponentValue("lastName"));
    Assert.assertEquals("Nathalie", formTester.getTextComponentValue("firstName"));
    Assert.assertEquals("ndupont@obiba.org", formTester.getTextComponentValue("email"));
    Assert.assertEquals(Locale.ENGLISH, formTester.getForm().get("language").getModelObject());
    assertSetsEquals(getUserRoles(), (Set<Role>) formTester.getForm().get("roles").getModelObject());
  }

  @Test
  public void testEditUser() {
    final User u = newUserTest();

    expect(userServiceMock.getRoles(SortingClause.create("name"))).andReturn(newRoleListTest());
    userServiceMock.createOrUpdateUser(u);

    replay(userServiceMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return (new UserPanel(panelId, new Model(u), new ModalWindow("windowMock")));
      }
    });

    tester.dumpPage();

    FormTester formTester = tester.newFormTester("panel:userPanelForm");

    formTester.setValue("firstName", "newFirstName");
    formTester.setValue("lastName", "newLastName");
    formTester.setValue("email", "ndupont@obiba.com");
    formTester.select("language", 1);

    submitForm();
    tester.assertNoErrorMessage();

    verify(userServiceMock);

    Assert.assertEquals("newFirstName", u.getFirstName());
    Assert.assertEquals("newLastName", u.getLastName());
    Assert.assertEquals("ndupont@obiba.com", u.getEmail());
    Assert.assertEquals(Locale.ENGLISH, u.getLanguage());
  }

  @Test
  public void testAddUserMissingRequiredFields() {
    final User u = new User();
    expect(userServiceMock.getRoles((SortingClause) EasyMock.anyObject())).andReturn(newRoleListTest());

    replay(userServiceMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return (userPanel = new UserPanel(panelId, new Model(u), new ModalWindow("windowMock")));
      }
    });

    FormTester formTester = tester.newFormTester("panel:userPanelForm");
    formTester.setValue("lastName", "Tremblay");
    formTester.setValue("firstName", "Patrick");
    formTester.setValue("email", "ptremblay@obiba.org");

    submitForm();

    StringResourceModel strModel = new StringResourceModel("Required", userPanel, new Model(new ValueMap("label=password")));
    StringResourceModel strModel1 = new StringResourceModel("Required", userPanel, new Model(new ValueMap("label=roles")));
    StringResourceModel strModel2 = new StringResourceModel("Required", userPanel, new Model(new ValueMap("label=language")));
    tester.assertErrorMessages(new String[] { strModel.getString(), strModel1.getString(), strModel2.getString() });

    verify(userServiceMock);
  }

  @Test
  public void testAddUser() {
    final User u = new User();
    expect(userServiceMock.getRoles(SortingClause.create("name"))).andReturn(newRoleListTest());
    expect(userServiceMock.getUserWithLogin("ptremblay")).andReturn(null);
    userServiceMock.createOrUpdateUser(u);

    replay(userServiceMock);

    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {
        return (userPanel = new UserPanel(panelId, new Model(u), new ModalWindow("windowMock")));
      }
    });

    FormTester formTester = tester.newFormTester("panel:userPanelForm");
    formTester.setValue("lastName", "Tremblay");
    formTester.setValue("firstName", "Patrick");
    formTester.setValue("email", "ptremblay@obiba.org");
    formTester.setValue("password", "password01");
    formTester.select("language", 1);
    formTester.selectMultiple("roles", new int[] { 0, 1 });

    submitForm();

    tester.assertNoErrorMessage();
    verify(userServiceMock);
    Assert.assertEquals("Tremblay", u.getLastName());
    Assert.assertEquals(Locale.ENGLISH, u.getLanguage());
    // Assert that two Sets are identical
    assertSetsEquals(getUserRoles(), u.getRoles());
  }

  private void submitForm() {
    tester.executeAjaxEvent("panel:userPanelForm:save", "onclick");
  }

  private User newUserTest() {
    User u = new User();

    u.setId(1l);
    u.setLogin("ndupont");
    u.setLastName("Dupont");
    u.setFirstName("Nathalie");
    u.setEmail("ndupont@obiba.org");
    u.setLanguage(Locale.ENGLISH);
    u.setPassword("password01");
    u.setRoles(getUserRoles());

    return (u);
  }

  private Set<Role> getUserRoles() {
    Set<Role> roles = new HashSet<Role>();
    roles.add(Role.DATA_COLLECTION_OPERATOR);
    roles.add(Role.PARTICIPANT_MANAGER);
    return roles;
  }

  private List<Role> newRoleListTest() {
    List<Role> roleList = new ArrayList<Role>();

    Role roleInstance = Role.DATA_COLLECTION_OPERATOR;
    roleInstance.setId(1l);
    roleList.add(roleInstance);

    roleInstance = Role.PARTICIPANT_MANAGER;
    roleInstance.setId(2l);
    roleList.add(roleInstance);

    roleInstance = Role.SYSTEM_ADMINISTRATOR;
    roleInstance.setId(3l);
    roleList.add(roleInstance);

    return (roleList);
  }

  private void assertSetsEquals(Set<?> lhs, Set<?> rhs) {
    // Assert that lhs contains all the items in rhs AND that rhs contains all the items in lhs
    // Testing both sides assures us that one set is not simply a subset of the other
    Assert.assertTrue(lhs.containsAll(rhs) && rhs.containsAll(lhs));
  }

}
