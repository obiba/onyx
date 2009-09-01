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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.core.util.StringUtil;
import org.obiba.onyx.webapp.OnyxAuthenticatedSession;
import org.obiba.onyx.webapp.condition.page.WorkstationPage;
import org.obiba.onyx.webapp.participant.page.ParticipantSearchPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the menu that will appear in the menu bar and in the home page. Only page links current user is allowed to go
 * are visible.
 * @author Yannick Marcon
 * 
 */
public class MenuBuilder {

  /**
   * Build the default menu (with logout menu item).
   * @param container where adding the menu items
   */
  public static void build(final MarkupContainer container) {
    build(container, true);
  }

  /**
   * Build the default menu.
   * @param container where adding the menu items
   * @param fullMenu add logout menu item if true
   */
  public static void build(final MarkupContainer container, boolean fullMenu) {
    final List<MenuItem> menuItems = new ArrayList<MenuItem>();

    if(OnyxAuthenticatedSession.get().isSignedIn()) {
      menuItems.add(new MenuItem(Application.get().getHomePage(), "Home"));
      menuItems.add(new MenuItem(ParticipantSearchPage.class, "Participant"));
      menuItems.add(new MenuItem(WorkstationPage.class, "Workstation"));
    }

    // Creating the DataView containing the whole menu
    container.add(new DataView("menuItem", new ListDataProvider(menuItems)) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void populateItem(Item item) {
        MenuItem menuItem = menuItems.get(item.getIndex());
        Component menuLink = getLink("menuLink", menuItem.getPage(), menuItem.getParameters(), "menuLabel", menuItem.getLabel(), container);
        item.add(menuLink);

        // Creating the DataView holding all items of a submenu
        DataView menuItemDataView = new DataView("subMenuItem", new ListDataProvider(menuItem.getSubMenuItems())) {

          private static final long serialVersionUID = 1L;

          @Override
          protected void populateItem(Item subItem) {
            MenuItem subMenuItem = (MenuItem) subItem.getModelObject();
            Component link = getLink("subMenuLink", subMenuItem.getPage(), subMenuItem.getParameters(), "subMenuLabel", subMenuItem.getLabel(), container);
            subItem.add(link);
            setPageview(subItem, subMenuItem);
          }
        };

        item.add(menuItemDataView);
        if(menuItem.getRoles() != null) {
          MetaDataRoleAuthorizationStrategy.authorize(item, RENDER, menuItem.getRoles().toString());
        }
      }
    });

  }

  private static AbstractLink getLink(String linkId, Class<?> pageClass, PageParameters parameters, String labelId, String label, Component component) {
    AbstractLink link;
    if(pageClass == null) link = new BookmarkablePageLink(linkId, component.getPage().getClass(), component.getPage().getPageParameters());
    else if(parameters != null) link = new BookmarkablePageLink(linkId, pageClass, parameters);
    else
      link = new BookmarkablePageLink(linkId, pageClass);

    link.add(new Label(labelId, new StringResourceModel(label, component, null)));
    link.add(new MenuItemSelectionBehavior());

    return link;
  }

  private static void setPageview(Component c, MenuItem mi) {
    // Menu item will be displayed only if user has the roles to see the page
    AuthorizeInstantiation authorizationAnnotation = mi.getPage().getAnnotation(AuthorizeInstantiation.class);
    if(authorizationAnnotation != null) {
      String[] authorizedRoles = authorizationAnnotation.value();
      MetaDataRoleAuthorizationStrategy.authorize(c, Component.RENDER, StringUtil.stringArrayToString(authorizedRoles));
    }
  }

  static class MenuItemSelectionBehavior extends AbstractBehavior {

    private static final long serialVersionUID = 1L;

    private static final String SELECTED_CSS_CLASS = "ui-state-highlight";

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(MenuItemSelectionBehavior.class);

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
      super.onRendered(component);

      if(component instanceof BookmarkablePageLink) {
        BookmarkablePageLink link = (BookmarkablePageLink) component;

        Page currentPage = component.getPage();
        log.info("linkPageClass = {}, pageClass = {}", link.getPageClass().getSimpleName(), currentPage.getClass().getSimpleName());

        if(link.getPageClass().equals(currentPage.getClass())) {
          String cssClass = SELECTED_CSS_CLASS;
          if(tag.getAttributes().containsKey("class")) {
            cssClass += " " + tag.getAttributes().getString("class");
          }
          tag.getAttributes().put("class", cssClass);
        }
      }
    }
  }
}
