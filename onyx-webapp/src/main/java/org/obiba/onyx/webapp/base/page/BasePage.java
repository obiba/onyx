/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.base.page;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.webapp.base.panel.HeaderPanel;
import org.obiba.onyx.webapp.base.panel.MenuBar;
import org.obiba.onyx.wicket.util.DateModelUtils;

public abstract class BasePage extends AbstractBasePage implements IAjaxIndicatorAware {

  @SpringBean
  UserSessionService userSessionService;

  public BasePage() {
    super();

    ContextImage img = new ContextImage("logo", new Model("images/logo/logo_on_dark.png"));
    img.setMarkupId("logo");
    img.setOutputMarkupId(true);

    Panel headerPanel = new EmptyPanel("header");

    Panel menuBar = new EmptyPanel("menuBar");
    menuBar.setMarkupId("menuBar");
    setOutputMarkupId(true);

    Label userFullName = new Label("userFullName");
    Label currentTime = new Label("currentTime");

    Session session = getSession();
    // Tests the session type for unit testing
    if(session instanceof OnyxAuthenticatedSession) {
      if(((OnyxAuthenticatedSession) getSession()).isSignedIn()) {
        headerPanel = new HeaderPanel("header");
        menuBar = new MenuBar("menuBar");

        userFullName.setModel(new Model(OnyxAuthenticatedSession.get().getUser().getFullName()));
        currentTime.setModel(DateModelUtils.getDateTimeModel(new Model(new SimpleDateFormat("yyyy-MM-dd - HH:mm:ss", getLocale())), new Model(new Date())));
      }
    }

    add(currentTime);
    add(img);
    add(userFullName);
    add(headerPanel);
    add(menuBar);

    add(new Label("baseAjaxIndicator", new StringResourceModel("Processing", this, null)));
  }

  public void setMenuBarVisible(boolean visible) {
    get("menuBar").setVisible(visible);
  }

  /**
   * @see org.apache.wicket.ajax.IAjaxIndicatorAware#getAjaxIndicatorMarkupId()
   */
  public String getAjaxIndicatorMarkupId() {
    return "base-ajax-indicator";
  }

  public void onLanguageUpdate(Locale language, AjaxRequestTarget target) {
    setResponsePage(getPage());
  }

}
