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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.Status;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.UserService;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.panel.ConfirmLinkPanel;
import org.obiba.wicket.markup.html.panel.LinkPanel;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;

public class UserSearchPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private UserService userService;

  private ModalWindow userDetailsModalWindow;

  private OnyxEntityList<User> userList;

  private User template = new User();

  public UserSearchPanel(String id) {
    super(id);

    setModalWindow();
    add(userDetailsModalWindow);

    add(new AjaxLink("addUser") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        userDetailsModalWindow.setContent(new UserPanel("content", new Model(new User()), userDetailsModalWindow));
        userDetailsModalWindow.show(target);
      }

    });

    template.setDeleted(false);

    userList = new OnyxEntityList<User>("user-list", new UserProvider(template), new UserListColumnProvider(), new StringResourceModel("UserList", UserSearchPanel.this, null));
    add(userList);

  }

  private void setModalWindow() {
    userDetailsModalWindow = new ModalWindow("userDetailsModalWindow");
    userDetailsModalWindow.setCssClassName("onyx");
    userDetailsModalWindow.setTitle(new StringResourceModel("EditUser", this, null));

    userDetailsModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        // same as cancel
        return true;
      }
    });

    userDetailsModalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      public void onClose(AjaxRequestTarget target) {
        target.addComponent(userList);
      }
    });
    userDetailsModalWindow.setHeightUnit("em");
    userDetailsModalWindow.setWidthUnit("em");
    userDetailsModalWindow.setInitialWidth(32);
    userDetailsModalWindow.setInitialHeight(21);
    userDetailsModalWindow.setResizable(false);

  }

  @SuppressWarnings("serial")
  private class UserProvider extends SortableDataProviderEntityServiceImpl<User> {

    private User template;

    public UserProvider(User template) {
      super(queryService, User.class);
      this.template = template;
      setSort(new SortParam("lastName", true));
    }

    @Override
    protected List<User> getList(PagingClause paging, SortingClause... clauses) {
      return userService.getUsers(template, paging, clauses);
    }

    @Override
    public int size() {
      return userService.getUserCount(template);
    }
  }

  @SuppressWarnings("unchecked")
  private class UserListColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = 1141339694945247910L;

    private List<IColumn> columns = new ArrayList<IColumn>();

    private List<IColumn> additional = new ArrayList<IColumn>();

    @SuppressWarnings("serial")
    public UserListColumnProvider() {
      columns.add(new PropertyColumn(new StringResourceModel("LastName", UserSearchPanel.this, null), "lastName", "lastName"));
      columns.add(new PropertyColumn(new StringResourceModel("FirstName", UserSearchPanel.this, null), "firstName", "firstName"));
      columns.add(new PropertyColumn(new StringResourceModel("login", UserSearchPanel.this, null), "login", "login"));
      columns.add(new PropertyColumn(new StringResourceModel("Email", UserSearchPanel.this, null), "email", "email"));

      columns.add(new AbstractColumn(new StringResourceModel("Role(s)", UserSearchPanel.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          StringBuilder roleList = new StringBuilder();

          for(Role r : getUser(rowModel).getRoles()) {
            if(roleList.length() != 0) roleList.append(", ");
            roleList.append(new StringResourceModel("Role." + ((Role) r).getName(), UserSearchPanel.this, null).getString());
          }

          cellItem.add(new Label(componentId, roleList.toString()));
        }

      });

      columns.add(new AbstractColumn(new StringResourceModel("Status", UserSearchPanel.this, null)) {

        public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
          LinkPanel statusLink = new LinkPanel(componentId, new ILinkListener() {

            public void onLinkClicked() {
              User user = getUser(rowModel);
              Status newStatus;
              if(user.getStatus() == Status.ACTIVE) {
                newStatus = Status.INACTIVE;
              } else {
                newStatus = Status.ACTIVE;
              }

              userService.updateStatus(user, newStatus);
            }

          }, new StringResourceModel("Status." + getUser(rowModel).getStatus(), UserSearchPanel.this, null));

          User currentUser = OnyxAuthenticatedSession.get().getUser();
          // If this line is the current user, display a label instead of a toggle link
          if(currentUser.getLogin().equals(getUser(rowModel).getLogin())) {
            cellItem.add(new Label(componentId, new StringResourceModel("Status." + currentUser.getStatus(), UserSearchPanel.this, null)));
          } else {
            cellItem.add(statusLink);
          }
        }
      });

      columns.add(new HeaderlessColumn() {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          cellItem.add(new LinkFragment(componentId, rowModel));
        }
      });

      columns.add(new HeaderlessColumn() {

        public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
          ConfirmLinkPanel deleteLink = new ConfirmLinkPanel(componentId, new StringResourceModel("Delete", UserSearchPanel.this, null), new StringResourceModel("ConfirmDeleteUser", UserSearchPanel.this, null, new Object[] { getUser(rowModel).getFullName() })) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
              userService.deleteUser(getUser(rowModel));
            }
          };
          if(OnyxAuthenticatedSession.get().getUser().getLogin().equals(getUser(rowModel).getLogin())) deleteLink.setVisible(false);
          cellItem.add(deleteLink);
        }
      });
    }

    public List<IColumn> getAdditionalColumns() {
      return additional;
    }

    public List<String> getColumnHeaderNames() {
      return null;
    }

    public List<IColumn> getDefaultColumns() {
      return columns;
    }

    public List<IColumn> getRequiredColumns() {
      return columns;
    }

    protected User getUser(IModel rowModel) {
      return (User) rowModel.getObject();
    }

  }

  public class LinkFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public LinkFragment(String id, final IModel rowModel) {
      super(id, "linkFragment", UserSearchPanel.this);
      add(new AjaxLink("editLink") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          userDetailsModalWindow.setContent(new UserPanel("content", rowModel, userDetailsModalWindow));
          userDetailsModalWindow.show(target);
        }

      });

    }
  }

  /**
   * Generate a unique user name, only for non existing user.
   * @param user
   */
  private void generateLogin(User user) {
    if(user.getId() != null) return;

    String baseLogin = "";

    if(user.getFirstName() != null && user.getFirstName().length() > 0) baseLogin = user.getFirstName().substring(0, 1).toLowerCase();
    if(user.getLastName() != null && user.getLastName().length() > 0) baseLogin += user.getLastName().toLowerCase();

    String login = (baseLogin.length() > 12) ? baseLogin.substring(0, 12) : baseLogin;
    int i = 1;
    while(userService.getUserWithLogin(login) != null) {
      login = ((baseLogin.length() > 12) ? baseLogin.substring(0, (12 - (String.valueOf(i)).length())) : baseLogin) + i;
      i++;
    }

    user.setLogin(login);
  }

}
