/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.base.panel;

import java.io.Serializable;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.application.ApplicationConfiguration;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.webapp.administration.page.AdministrationPage;
import org.obiba.onyx.webapp.login.page.LoginPage;
import org.obiba.onyx.webapp.user.page.ProfilePage;

@SuppressWarnings("serial")
public class HeaderPanel extends Panel {

  @SpringBean
  private EntityQueryService queryService;

  HeaderPanelModel model = new HeaderPanelModel();

  @SuppressWarnings("rawtypes")
  public HeaderPanel(String id) {
    super(id);

    add(new Link("profile") {

      @Override
      public void onClick() {
        // OnyxAuthenticatedSession.get().getUser();
        setResponsePage(new ProfilePage(getPage().getPageMapEntry().getNumericId()));
      }
    });

    add(new AdminLink("admin"));

    Link<?> helpLink = new Link("help") {

      @Override
      public void onClick() {
      }
    };
    helpLink.setEnabled(false);
    add(helpLink);

    add(new Link("quit") {

      @Override
      public void onClick() {
        OnyxAuthenticatedSession.get().signOut();
        setResponsePage(LoginPage.class);
      }
    });

  }

  private class HeaderPanelModel implements Serializable {

    // public User getUserLoggedIn() {
    // return OnyxAuthenticatedSession.get().getUser();
    // }

    public ApplicationConfiguration getConfig() {
      ApplicationConfiguration template = new ApplicationConfiguration();
      // TODO not supposed to happen, but test environment was broken
      for(ApplicationConfiguration conf : queryService.match(template)) {
        return conf;
      }
      return null;
      // return ( queryService.matchOne( template ) );
    }

  }

  @AuthorizeAction(action = "RENDER", roles = { "SYSTEM_ADMINISTRATOR", "QUESTIONNAIRE_EDITOR" })
  private class AdminLink extends Link<Object> {

    public AdminLink(String id) {
      super(id);
    }

    @Override
    public void onClick() {
      // OnyxAuthenticatedSession.get().getUser();
      setResponsePage(AdministrationPage.class);
    }

  }

}
