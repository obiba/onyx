package org.obiba.onyx.webapp.base.panel;

import java.io.Serializable;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.webapp.login.page.LoginPage;
import org.obiba.onyx.webapp.user.page.ProfilePage;

public class HeaderPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private EntityQueryService queryService;

  HeaderPanelModel model = new HeaderPanelModel();

  public HeaderPanel(String id) {
    super(id);
    
    add(new Label("studyName", new PropertyModel(model, "config.studyName")));
    add(new Label("siteName", new PropertyModel(model, "config.siteName")));
    add(new Label("userLoggedIn", new PropertyModel(model, "userLoggedIn.fullName")));
    
    add(new Link("profile"){

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick() {
        OnyxAuthenticatedSession.get().getUser();
        setResponsePage(ProfilePage.class);
      }
    });
    
    add(new Link("quit") {
      
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick() {
        OnyxAuthenticatedSession.get().signOut();
        setResponsePage(LoginPage.class);
      }
    });

  }

  private class HeaderPanelModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public User getUserLoggedIn() {
      return OnyxAuthenticatedSession.get().getUser();
    }

    public ApplicationConfiguration getConfig() {
      ApplicationConfiguration template = new ApplicationConfiguration();
      //  TODO not supposed to happen, but test environment was broken
      for (ApplicationConfiguration conf : queryService.match(template)) {
        return conf;
      }
      return null;
      //return ( queryService.matchOne( template ) );
    }

  }

}
