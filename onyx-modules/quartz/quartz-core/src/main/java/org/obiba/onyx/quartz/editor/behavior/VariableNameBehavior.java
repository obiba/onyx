/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.behavior;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;

/**
 *
 */
@SuppressWarnings("serial")
public class VariableNameBehavior extends AbstractBehavior {

  private boolean variableNameDefined;

  public VariableNameBehavior(final TextField<String> name, final TextField<String> variable, final Question parentQuestion, final Question question, final Category category) {

    variableNameDefined = StringUtils.isNotBlank(variable.getModelObject());

    if(!variableNameDefined) {

      variable.setOutputMarkupId(true);

      variable.setModelObject(generateVariableName(parentQuestion, question, category, name.getModelObject()));
      variable.add(new AttributeModifier("class", true, new Model<String>("autoDefined")));
      final AjaxEventBehavior updateVariableNameBehavior = new OnChangeAjaxBehavior() {

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          if(!variableNameDefined) {
            variable.setModelObject(generateVariableName(parentQuestion, question, category, name.getModelObject()));
            target.addComponent(variable);
          }
        }
      };
      name.add(updateVariableNameBehavior);

      variable.add(new AjaxEventBehavior("onclick") {
        @Override
        protected void onEvent(AjaxRequestTarget target) {
          variable.setModelObject("");
          variable.add(new AttributeModifier("class", true, new Model<String>("userDefined")));
          variableNameDefined = true;
          target.addComponent(variable);
        }

        @Override
        public boolean isEnabled(Component component) {
          return !variableNameDefined;
        }
      });

    }

  }

  @Override
  public void renderHead(IHeaderResponse response) {
    super.renderHead(response);
    response.renderCSSReference(new CompressedResourceReference(VariableNameBehavior.class, "VariableNameBehavior.css"));
  }

  public boolean isVariableNameDefined() {
    return variableNameDefined;
  }

  protected String generateVariableName(Question parentQuestion, Question question, Category category, String name) {
    if(StringUtils.isBlank(name)) return "";
    String variableName = (parentQuestion == null ? "" : parentQuestion.getName() + ".");
    if(question != null) {
      variableName += question.getName() + ".";
      if(category != null) {
        variableName += category.getName() + ".";
      }
    }
    return variableName + StringUtils.trimToEmpty(name);
  }
}
