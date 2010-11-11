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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
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

  public EditionPanel(String id, IModel<Questionnaire> model) {
    super(id, model);
    this.rightPanel = new DefaultRightPanel(RIGHT_PANEL);

    add(CSSPackageResource.getHeaderContribution(EditionPanel.class, "EditionPanel.css"));

    rightPanelContainer = new WebMarkupContainer("rightPanelContainer");
    rightPanelContainer.setOutputMarkupId(true);
    add(rightPanelContainer);

    rightPanelContainer.add(rightPanel);
    rightPanelContainer.add(rightPanelTitle = new Label("title"));

    tree = new QuestionnaireTreePanel("tree", model) {
      @Override
      public void show(Component component, IModel<String> title, AjaxRequestTarget target) {
        setRightPanel(component, title);
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

  public void setRightPanel(Component component, IModel<String> title) {
    rightPanel.replaceWith(component);
    rightPanel = component;
    rightPanelTitle.setDefaultModel(title);
  }

  public void restoreDefaultRightPanel(AjaxRequestTarget target) {
    setRightPanel(new DefaultRightPanel(RIGHT_PANEL), new Model<String>(""));
    target.addComponent(rightPanelContainer);
  }

  public QuestionnaireTreePanel getTree() {
    return tree;
  }

}
