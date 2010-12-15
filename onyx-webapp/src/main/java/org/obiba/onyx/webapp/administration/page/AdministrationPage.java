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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.webapp.OnyxApplication;
import org.obiba.onyx.webapp.administration.panel.DataManagementPanel;
import org.obiba.onyx.webapp.administration.panel.DevelopersPanel;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.user.panel.UserSearchPanel;

/**
 * Displays the list of users Contains a link to edit user info, to add a new user and to delete an existing user
 * Contains a toggle to change user status
 * @author acarey
 * 
 */
@SuppressWarnings("serial")
@AuthorizeInstantiation({ "SYSTEM_ADMINISTRATOR", "QUESTIONNAIRE_EDITOR" })
public class AdministrationPage extends BasePage {

  @SpringBean
  private ModuleRegistry moduleRegistry;

  private List<AjaxLink<?>> links;

  public AdministrationPage() {

    links = new ArrayList<AjaxLink<?>>();

    AjaxLink<?> userTab = new AdminTab("userTab") {

      @Override
      public Component getComponent() {
        return new UserSearchPanel(getContentId());
      }

    };
    userTab.setOutputMarkupId(true);
    add(userTab);
    links.add(userTab);

    AjaxLink<?> dataTab = new AdminTab("dataTab") {

      @Override
      public Component getComponent() {
        return new DataManagementPanel(getContentId());
      }

    };
    dataTab.setOutputMarkupId(true);
    add(dataTab);
    links.add(dataTab);

    AjaxLink<?> editorTab = new EditorTab("editorTab") {

      @Override
      public Component getComponent() {
        for(Module module : moduleRegistry.getModules()) {
          Component editorComponent = module.getEditorPanel(getContentId());
          if(editorComponent != null) return editorComponent;
        }
        return null;
      }

    };
    editorTab.setOutputMarkupId(true);
    add(editorTab);
    links.add(editorTab);

    AjaxLink<?> devTab = new AdminTab("devTab") {

      @Override
      public Component getComponent() {
        return new DevelopersPanel(getContentId());
      }

      @Override
      public boolean isVisible() {
        return ((OnyxApplication) WebApplication.get()).isDevelopmentMode();
      }
    };
    devTab.setOutputMarkupId(true);
    add(devTab);
    links.add(devTab);

    // Display first permitted tab
    for(AjaxLink<?> link : links) {
      if(link.isActionAuthorized(new Action(Action.RENDER))) {
        add(((Link) link).getComponent());
        link.add(new AttributeModifier("class", true, new Model<String>("obiba-button ui-corner-all selected")));
        break;
      }
    }

  }

  private void replaceContent(AjaxRequestTarget target, Component content) {
    content.setOutputMarkupId(true);
    addOrReplace(content);
    target.appendJavascript("$(document).ready(function () {$('.obiba-content-region').each(function() {$(this).addClass('ui-corner-all');});});");
    target.appendJavascript("$(document).ready(function () {$('.obiba-button').each(function() {$(this).addClass('ui-corner-all');});});");
    target.addComponent(content);
  }

  private void activateLink(AjaxLink<?> selectedLink, AjaxRequestTarget target) {
    for(AjaxLink<?> link : links) {
      link.add(new AttributeModifier("class", true, new Model<String>("obiba-button ui-corner-all" + (link == selectedLink ? " selected" : ""))));
      target.addComponent(link);
    }
  }

  private String getContentId() {
    return "administrationContent";
  }

  private interface Link {

    Component getComponent();

  }

  @AuthorizeAction(action = "RENDER", roles = { "SYSTEM_ADMINISTRATOR" })
  private abstract class AdminTab extends AjaxLink<Object> implements Link {

    public AdminTab(String id) {
      super(id);
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
      replaceContent(target, getComponent());
      activateLink(this, target);
    }

    public abstract Component getComponent();

  }

  @AuthorizeAction(action = "RENDER", roles = { "QUESTIONNAIRE_EDITOR" })
  private abstract class EditorTab extends AjaxLink<Object> implements Link {

    public EditorTab(String id) {
      super(id);
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
      replaceContent(target, getComponent());
      activateLink(this, target);
    }

    public abstract Component getComponent();
  }

}
