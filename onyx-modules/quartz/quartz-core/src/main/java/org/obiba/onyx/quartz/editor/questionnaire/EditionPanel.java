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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;

/**
 *
 */
@SuppressWarnings("serial")
public class EditionPanel extends Panel {

  private Label prpertiesTitle;

  private Component properties;

  public EditionPanel(String id, IModel<Questionnaire> model) {
    super(id, model);

    add(CSSPackageResource.getHeaderContribution(EditionPanel.class, "EditionPanel.css"));

    final WebMarkupContainer propertiesContainer = new WebMarkupContainer("propertiesContainer");
    propertiesContainer.setOutputMarkupId(true);
    add(propertiesContainer);

    properties = new WebMarkupContainer("properties");
    propertiesContainer.add(properties);
    propertiesContainer.add(prpertiesTitle = new Label("title"));

    QuestionnaireTreePanel tree = new QuestionnaireTreePanel("tree", model) {
      @Override
      public void show(Component component, IModel<String> title, AjaxRequestTarget target) {
        properties.replaceWith(component);
        properties = component;
        prpertiesTitle.setDefaultModel(title);
        target.addComponent(propertiesContainer);
      }

      @Override
      public String getShownComponentId() {
        return "properties";
      }
    };
    add(tree);

  }

}
