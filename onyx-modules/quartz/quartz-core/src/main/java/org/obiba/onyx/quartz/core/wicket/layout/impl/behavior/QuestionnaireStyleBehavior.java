/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;

/**
 * Generate a css class representing the questionnaire element, if the component it is attached to has a model of type
 * {@link QuestionnaireModel}.
 */
public class QuestionnaireStyleBehavior extends AbstractBehavior {

  private static final long serialVersionUID = 1L;

  @Override
  public void onComponentTag(Component component, ComponentTag tag) {
    IModel model = component.getDefaultModel();

    if(QuestionnaireModel.class.isInstance(model)) {
      QuestionnaireModel qModel = (QuestionnaireModel) model;
      String cssClass = qModel.getQuestionnaireName() + "-" + qModel.getElementClass().getSimpleName() + "-" + qModel.getElementName().replace('.', '-');

      if(tag.getAttributes().containsKey("class")) {
        cssClass += " " + tag.getAttributes().getString("class");
      }
      tag.getAttributes().put("class", cssClass);
    }
  }

}
