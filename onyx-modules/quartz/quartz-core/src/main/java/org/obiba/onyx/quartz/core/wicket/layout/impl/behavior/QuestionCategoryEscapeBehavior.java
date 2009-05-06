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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;

/**
 * 
 */
public class QuestionCategoryEscapeBehavior extends AbstractBehavior {

  private static final long serialVersionUID = 1L;

  private static final String ESCAPE_CSS_CLASS = "obiba-quartz-escape-category";

  @Override
  public void onComponentTag(Component component, ComponentTag tag) {
    IModel model = component.getModel();

    if(QuestionnaireModel.class.isInstance(model)) {
      QuestionCategory category = (QuestionCategory) ((QuestionnaireModel) model).getObject();

      if(category.isEscape()) {
        // watch if this category is the first escaped for the question
        for(Category cat : category.getQuestion().getCategories()) {
          if(!cat.isEscape()) continue;
          if(!category.getCategory().equals(cat)) break;
          String cssClass = ESCAPE_CSS_CLASS;

          if(tag.getAttributes().containsKey("class")) cssClass += " " + tag.getAttributes().getString("class");
          tag.getAttributes().put("class", cssClass);

        }
      }
    }
  }
}
