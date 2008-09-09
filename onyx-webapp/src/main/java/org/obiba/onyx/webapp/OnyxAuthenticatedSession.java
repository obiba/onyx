package org.obiba.onyx.webapp;

import java.util.Iterator;

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.User;

public final class OnyxAuthenticatedSession extends WebSession {

  private static final long serialVersionUID = 8437488796788626235L;

  private User user;

  @SpringBean
  private EntityQueryService queryService;

  protected OnyxAuthenticatedSession(final OnyxApplication application, Request request) {
    super(request);
    InjectorHolder.getInjector().inject(this);
  }

  /**
   * Checks the given username and password and, if they are correct, sets the session User object.
   * @param login the username
   * @param password the password
   * @return true if the user was authenticated
   */
  public final boolean authenticate(final String login, final String password) {
    if(user == null) {
      // Try to authenticate user with database.
      User template = new User();
      template.setLogin(login);
      User fetchedUser = null;
      for (User u : queryService.match(template)) {
        if (!u.isDeleted()) {
          fetchedUser = u;
          break;
        }
      }
      if(fetchedUser != null && !fetchedUser.isDeleted() && fetchedUser.getPassword().equals(User.digest(password))) {
        user = fetchedUser;

        if(user.getLanguage() != null)
          setLocale(user.getLanguage());
        
        // Forcing initialization of user's roles
        // user.getRoles().size();
      }
    }
    return user != null;
  }

  /**
   * @return true if user is signed in
   */
  public boolean isSignedIn() {
    return user != null;
  }

  /**
   * Returns the <tt>User</tt> instance associated with this session.
   * @return the <tt>User</tt> instance.
   */
  public User getUser() {
    return user;
  }

  public static OnyxAuthenticatedSession get() {
    return (OnyxAuthenticatedSession) Session.get();
  }

  /**
   * Sign the user out.
   */
  public void signOut() {
    user = null;
    invalidateNow();
  }

  public Roles getRoles() {
    if(isSignedIn()) {

      // Prepare the role list in a string provided to the Wicket Roles class. 
      StringBuilder roleList = new StringBuilder();
      for (Role r : queryService.matchOne(user).getRoles()) {
        if (roleList.length()!= 0)
          roleList.append(",");
        roleList.append(r.getName());
      }
      
      Roles userRoles = new Roles(roleList.toString());
      return userRoles;
    }
    return null;
  }

}
