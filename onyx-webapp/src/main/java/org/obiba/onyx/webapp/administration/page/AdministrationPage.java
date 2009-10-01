/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.administration.page;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.obiba.onyx.webapp.administration.panel.DataManagementPanel;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.user.panel.UserSearchPanel;

/**
 * Displays the list of users Contains a link to edit user info, to add a new user and to delete an existing user
 * Contains a toggle to change user status
 * @author acarey
 * 
 */
@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR" })
public class AdministrationPage extends BasePage {

  @SuppressWarnings("serial")
  public AdministrationPage() {
    super();

    add(new AjaxLink("userTab") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        replaceContent(target, new UserSearchPanel(getContentId()));
      }

    });

    add(new AjaxLink("dataTab") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        replaceContent(target, new DataManagementPanel(getContentId()));
      }

    });

    Component content = new UserSearchPanel(getContentId());
    content.setOutputMarkupId(true);
    add(content);

  }

  private void replaceContent(AjaxRequestTarget target, Component content) {
    content.setOutputMarkupId(true);
    addOrReplace(content);
    target.addComponent(content);
  }

  private String getContentId() {
    return "administrationContent";
  }

}
