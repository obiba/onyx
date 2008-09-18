package org.obiba.onyx.webapp.user.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.html.panel.Fragment;
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
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.panel.OnyxEntityList;
import org.obiba.onyx.webapp.user.panel.UserPanel;
import org.obiba.wicket.markup.html.panel.ConfirmLinkPanel;
import org.obiba.wicket.markup.html.panel.LinkPanel;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;

/**
 * Displays the list of users Contains a link to edit user info, to add a new user and to delete an existing user
 * Contains a toggle to change user status
 * @author acarey
 * 
 */
@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR" })
public class UserSearchPage extends BasePage {

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private UserService userService;

  private OnyxEntityList<User> userList;

  private User template = new User();

  private ModalWindow userDetailsModalWindow;

  @SuppressWarnings("serial")
  public UserSearchPage() {
    super();

    userDetailsModalWindow = new ModalWindow("userDetailsModalWindow");
    userDetailsModalWindow.setTitle(new StringResourceModel("UserManagement", this, null));

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
    userDetailsModalWindow.setInitialWidth(400);
    userDetailsModalWindow.setInitialHeight(450);
    add(userDetailsModalWindow);

    add(new AjaxLink("addUser") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        userDetailsModalWindow.setContent(new UserPanel("content", new Model(new User()), userDetailsModalWindow));
        userDetailsModalWindow.show(target);
      }

    });

    template.setDeleted(false);

    userList = new OnyxEntityList<User>("user-list", new UserProvider(template), new UserListColumnProvider(), new StringResourceModel("UserList", UserSearchPage.this, null));
    add(userList);

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
      return userService.getUsers(template.isDeleted(), paging, clauses);
    }

    @Override
    public int size() {
      return userService.getUserCount();
    }

  }

  private class UserListColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = 1141339694945247910L;

    private List<IColumn> columns = new ArrayList<IColumn>();

    private List<IColumn> additional = new ArrayList<IColumn>();

    @SuppressWarnings("serial")
    public UserListColumnProvider() {
      columns.add(new PropertyColumn(new StringResourceModel("LastName", UserSearchPage.this, null), "lastName", "lastName"));
      columns.add(new PropertyColumn(new StringResourceModel("FirstName", UserSearchPage.this, null), "firstName", "firstName"));
      columns.add(new PropertyColumn(new StringResourceModel("Email", UserSearchPage.this, null), "email", "email"));

      columns.add(new AbstractColumn(new StringResourceModel("Role(s)", UserSearchPage.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          StringBuilder roleList = new StringBuilder();

          for(Role r : getUser(rowModel).getRoles()) {
            if(roleList.length() != 0) roleList.append(", ");
            roleList.append(new StringResourceModel("Role." + ((Role) r).getName(), UserSearchPage.this, null).getString());
          }

          cellItem.add(new Label(componentId, roleList.toString()));
        }

      });

      columns.add(new AbstractColumn(new StringResourceModel("Status", UserSearchPage.this, null)) {

        public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
          LinkPanel statusLink = new LinkPanel(componentId, new ILinkListener() {

            public void onLinkClicked() {
              Status newStatus;
              if(getUser(rowModel).getStatus().equals(Status.ACTIVE)) newStatus = Status.INACTIVE;
              else
                newStatus = Status.ACTIVE;
              userService.updateStatus(getUser(rowModel).getId(), newStatus);
            }

          }, new StringResourceModel("Status." + getUser(rowModel).getStatus(), UserSearchPage.this, null));

          if(OnyxAuthenticatedSession.get().getUser().getLogin().equals(getUser(rowModel).getLogin())) cellItem.add(new Label(componentId, new StringResourceModel("Status." + getUser(rowModel).getStatus(), UserSearchPage.this, null)));
          else
            cellItem.add(statusLink);
        }
      });

      columns.add(new HeaderlessColumn() {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          cellItem.add(new LinkFragment(componentId, rowModel));
        }
      });

      columns.add(new HeaderlessColumn() {

        public void populateItem(Item cellItem, String componentId, final IModel rowModel) {
          ConfirmLinkPanel deleteLink = new ConfirmLinkPanel(componentId, new StringResourceModel("Delete", UserSearchPage.this, null), new StringResourceModel("ConfirmDeleteUser", UserSearchPage.this, null, new Object[] { getUser(rowModel).getFullName() })) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
              userService.deleteUser(getUser(rowModel).getId());
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
      super(id, "linkFragment", UserSearchPage.this);
      add(new AjaxLink("editLink") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          // TODO Auto-generated method stub
          userDetailsModalWindow.setContent(new UserPanel("content", rowModel, userDetailsModalWindow));
          userDetailsModalWindow.show(target);
        }

      });

    }
  }
}
