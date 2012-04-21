/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.model;

import java.util.Locale;

import org.apache.wicket.model.IModel;
import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
import org.obiba.magma.Value;
import org.obiba.magma.Variable;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.magma.OnyxAttributeHelper;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionnaireStringResourceModel extends QuestionnaireResourceModel<String> {

  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(QuestionnaireStringResourceModel.class);

  private static final String OBIBA_QUARTZ_ANSWER_CSS = "obiba-quartz-answer";

  //
  // Instance Variables
  //

  private boolean asQuartzAnswerFormat;

  //
  // Constructors
  //

  /**
   * Constructor using a model for providing the {@link IQuestionnaireElement}.
   * @param localizableModel
   * @param property
   * @param stringArgs
   */
  public QuestionnaireStringResourceModel(IModel<? extends IQuestionnaireElement> localizableModel, String property, Object... stringArgs) {
    super(localizableModel, property, stringArgs);
  }

  public QuestionnaireStringResourceModel(IQuestionnaireElement localizable, String property, Object... stringArgs) {
    this(localizable, property, true, stringArgs);
  }

  /**
   * Constructor using directly the {@link IQuestionnaireElement}.
   * @param localizable
   * @param property
   * @param stringArgs
   */
  public QuestionnaireStringResourceModel(IQuestionnaireElement localizable, String property, boolean asQuartzAnswerFormat, Object... stringArgs) {
    super(localizable, property, stringArgs);
    this.asQuartzAnswerFormat = asQuartzAnswerFormat;
  }

  //
  // LoadableDetachableModel Methods
  //

  @Override
  protected String load() {
    // Now use these services to get current questionnaire bundle.
    Locale locale = activeQuestionnaireAdministrationService.getLanguage();
    String bundleName = activeQuestionnaireAdministrationService.getQuestionnaire().getName();
    return resolveVariableValuesInMessage(resolveMessage(), bundleName, locale);
  }

  private String resolveVariableValuesInMessage(String message, String tableContext, Locale locale) {
    if(activeQuestionnaireAdministrationService.isQuestionnaireDevelopmentMode()) return message;
    String msg = message;
    // Look for variable references and replace by the value as a string
    try {
      int refIndex = msg.indexOf("$('");
      while(refIndex != -1) {
        int refEndIndex = msg.indexOf("')", refIndex);
        String path = msg.substring(refIndex + 3, refEndIndex);
        if(!path.contains(":")) {
          path = tableContext + ":" + path;
        }
        VariableDataSource varDs = new VariableDataSource(path);
        Value value = varDs.getValue(activeQuestionnaireAdministrationService.getQuestionnaireParticipant().getParticipant());

        String dataStr = getValueAsString(varDs, value, locale);
        msg = msg.substring(0, refIndex) + dataStr + msg.substring(refEndIndex + 2, msg.length());
        refIndex = msg.indexOf("$('");
      }
    } catch(Exception e) {
      log.error("Error while resolving variable values in: " + message, e);
    }
    return msg;
  }

  //
  // Methods
  //

  private String getValueAsString(VariableDataSource varDs, Value value, Locale locale) {
    if(value == null || value.getValue() == null) return "";

    String dataStr = value.toString();
    if(value.getValueType().equals(TextType.get())) {
      Variable variable = varDs.getVariable();
      if(!value.isSequence()) {
        if(asQuartzAnswerFormat) {
          dataStr = "<span class=\"" + OBIBA_QUARTZ_ANSWER_CSS + "\">" + getValueAsLabel(variable, value, locale) + "</span>";
        } else {
          dataStr = getValueAsLabel(variable, value, locale);
        }
      } else {
        StringBuffer buff = new StringBuffer();
        if(asQuartzAnswerFormat) {
          buff.append("<ul class=\"" + OBIBA_QUARTZ_ANSWER_CSS + "\">");
        } else {
          buff.append("<ul>");
        }
        for(Value val : value.asSequence().getValues()) {
          buff.append("<li>").append(getValueAsLabel(variable, val, locale)).append("</li>");
        }
        buff.append("</ul>");
        dataStr = buff.toString();
      }
    }
    return dataStr;
  }

  private String getValueAsLabel(Variable variable, Value value, Locale locale) {
    if(value == null || value.getValue() == null) return "";

    String valueStr = value.getValue().toString();
    if(variable.hasCategories()) {
      for(Category category : variable.getCategories()) {
        if(category.getName().equals(valueStr)) {
          return getCategoryLabel(category, locale);
        }
      }
    }
    return valueStr;
  }

  private String getCategoryLabel(Category category, Locale locale) {
    if(category.hasAttribute(OnyxAttributeHelper.LABEL, locale)) {
      Attribute attr = category.getAttribute(OnyxAttributeHelper.LABEL, locale);
      if(attr.getValue() != null) {
        return attr.getValue().toString();
      }
    }
    return category.getName();
  }

  /**
   * Convenience method. Equivalent to <code>(String)getObject()</code>.
   * 
   * @return model object as string
   */
  public String getString() {
    return getObject();
  }
}
