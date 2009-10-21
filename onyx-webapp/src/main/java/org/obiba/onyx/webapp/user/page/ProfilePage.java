/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.user.page;

import java.util.Arrays;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.UserService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.base.panel.AjaxLanguageChoicePanel;
import org.obiba.onyx.webapp.user.panel.ChangePasswordPanel;

/**
 * Allows the signed user to change his password and preferred language
 */
public class ProfilePage extends BasePage {

  @SpringBean
  private UserService userService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  public ProfilePage(int previousPageId) {
    super();

    AjaxLanguageChoicePanel languageSelect = new AjaxLanguageChoicePanel("languageSelect", new StringResourceModel("Language", this, null), Arrays.asList(new Locale[] { Locale.FRENCH, Locale.ENGLISH })) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onLanguageUpdate(Locale language, AjaxRequestTarget target) {
        if(language == null) return;
        userSessionService.setLocale(language);
        userService.updateUserLanguage(userSessionService.getUser(), language);
        setResponsePage(getPage());
      }

    };

    languageSelect.setSelectedLanguage(userSessionService.getLocale());

    add(languageSelect);

    ChangePasswordPanel changePassword = new ChangePasswordPanel("changePassword", previousPageId) {
      private static final long serialVersionUID = 1L;

      public void onSuccess() {
        setResponsePage(getApplication().getHomePage());
      }

      public void onFailure() {
        setResponsePage(getPage());
      }
    };

    add(changePassword);
  }
}
