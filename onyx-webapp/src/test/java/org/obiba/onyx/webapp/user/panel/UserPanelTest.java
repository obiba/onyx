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
    Assert.assertEquals(Locale.ENGLISH, formTester.getForm().get("languageSelect:localeSelect").getModelObject());
    Assert.assertArrayEquals(getUserRoles().toArray(), ((Set<Role>) formTester.getForm().get("roles").getModelObject()).toArray());
  }

  @Test
  public void testEditUser() {
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

    formTester.setValue("email", "ndupont@obiba.com");

    // formTester.select n'a pas l'air de fonctionner
    // formTester.select("languageSelect:localeSelect", 0);

    formTester.submit();
    tester.assertNoErrorMessage();

    verify(userServiceMock);

    Assert.assertEquals("ndupont@obiba.com", u.getEmail());
  }

  @Test
  public void testAddUserError() {
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
    formTester.submit();

    StringResourceModel strModel = new StringResourceModel("Required", userPanel, new Model(new ValueMap("label=password")));
    StringResourceModel strModel1 = new StringResourceModel("Required", userPanel, new Model(new ValueMap("label=roles")));
    tester.assertErrorMessages(new String[] { strModel.getString(), strModel1.getString() });

    verify(userServiceMock);
  }

  @Test
  public void testAddUser() {
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
    formTester.setValue("password", "password01");
    formTester.selectMultiple("roles", new int[] { 0, 1 });
    formTester.submit();

    tester.assertNoErrorMessage();
    verify(userServiceMock);
    Assert.assertEquals("Tremblay", u.getLastName());
    Assert.assertArrayEquals(getUserRoles().toArray(), u.getRoles().toArray());
  }

  private User newUserTest() {
    User u = new User();

    u.setId(1);
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
    roleInstance.setId(1);
    roleList.add(roleInstance);

    roleInstance = Role.PARTICIPANT_MANAGER;
    roleInstance.setId(2);
    roleList.add(roleInstance);

    roleInstance = Role.SYSTEM_ADMINISTRATOR;
    roleInstance.setId(3);
    roleList.add(roleInstance);

    return (roleList);
  }

}
