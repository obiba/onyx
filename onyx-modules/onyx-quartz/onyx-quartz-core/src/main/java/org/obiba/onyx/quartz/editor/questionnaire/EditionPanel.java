/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.behavior.tooltip.TooltipBehavior;
import org.obiba.onyx.quartz.editor.questionnaire.tree.DefaultRightPanel;
import org.obiba.onyx.quartz.editor.questionnaire.tree.QuestionnaireTreePanel;

/**
 *
 */
@SuppressWarnings("serial")
public class EditionPanel extends Panel {

  public static final String RIGHT_PANEL = "rightPanel";

  private Label rightPanelTitle;

  private Component rightPanel;

  private QuestionnaireTreePanel tree;

  private WebMarkupContainer rightPanelContainer;

  private ListView<MenuItem> menu;

  private WebMarkupContainer titleContainer;

  private Image titleIcon;

  public EditionPanel(String id, IModel<Questionnaire> model) {
    this(id, model, false);
  }

  public EditionPanel(String id, IModel<Questionnaire> model, boolean isNewQuestionnaire) {
    super(id, model);
    this.rightPanel = new DefaultRightPanel(RIGHT_PANEL);

    add(CSSPackageResource.getHeaderContribution(EditionPanel.class, "EditionPanel.css"));

    rightPanelContainer = new WebMarkupContainer("rightPanelContainer");
    rightPanelContainer.setOutputMarkupId(true);
    add(rightPanelContainer);

    rightPanelContainer.add(rightPanel);

    rightPanelContainer.add(titleContainer = new WebMarkupContainer("titleContainer"));
    titleContainer.setVisible(false);

    titleContainer.add(titleIcon = new Image("titleIcon"));
    titleContainer.add(rightPanelTitle = new Label("title"));
    titleContainer.add(menu = createMenu(null));

    tree = new QuestionnaireTreePanel("tree", model, isNewQuestionnaire) {
      @Override
      public void show(Component component, IModel<String> title, ResourceReference icon, List<MenuItem> menuItems, AjaxRequestTarget target) {
        setRightPanel(component, title, icon, menuItems);
        target.addComponent(rightPanelContainer);
      }

      @Override
      public String getShownComponentId() {
        return RIGHT_PANEL;
      }
    };
    tree.setOutputMarkupId(true);
    add(tree);

  }

  public void setNewQuestionnaire(boolean isNewQuestionnaire) {
    tree.setNewQuestionnaire(isNewQuestionnaire);
  }

  public void setRightPanel(Component component, IModel<String> title, ResourceReference icon, List<MenuItem> menuItems) {
    rightPanel.replaceWith(component);
    rightPanel = component;
    rightPanelTitle.setDefaultModel(title);
    titleIcon.setImageResourceReference(icon);
    titleContainer.setVisible(title != null && StringUtils.isNotBlank(title.getObject()));

    ListView<MenuItem> newMenu = createMenu(menuItems);
    menu.replaceWith(newMenu);
    menu = newMenu;
  }

  public void restoreDefaultRightPanel(AjaxRequestTarget target) {
    setRightPanel(new DefaultRightPanel(RIGHT_PANEL), new Model<String>(""), null, null);
    target.addComponent(rightPanelContainer);
  }

  public QuestionnaireTreePanel getTree() {
    return tree;
  }

  private ListView<MenuItem> createMenu(List<MenuItem> menuItems) {
    ListView<MenuItem> listView = new ListView<EditionPanel.MenuItem>("menu", menuItems == null ? new ArrayList<MenuItem>() : menuItems) {
      @Override
      protected void populateItem(ListItem<MenuItem> item) {
        final MenuItem menuItem = item.getModelObject();
        AjaxLink<Void> ajaxLink = new AjaxLink<Void>("button") {
          @Override
          public void onClick(AjaxRequestTarget target) {
            menuItem.onClick(target);
          }
        };
        ajaxLink.add(new Image("buttonImg", menuItem.getImg()));
        ajaxLink.add(new TooltipBehavior(menuItem.getTitle()));
        item.add(ajaxLink);
      }
    };
    listView.setVisible(menuItems != null && menuItems.size() > 0);
    return listView;
  }

  public static abstract class MenuItem implements Serializable {

    private final ResourceReference img;

    private final IModel<String> title;

    public MenuItem(IModel<String> title, ResourceReference img) {
      this.img = img;
      this.title = title;
    }

    public abstract void onClick(AjaxRequestTarget target);

    public ResourceReference getImg() {
      return img;
    }

    public IModel<String> getTitle() {
      return title;
    }
  }

}
