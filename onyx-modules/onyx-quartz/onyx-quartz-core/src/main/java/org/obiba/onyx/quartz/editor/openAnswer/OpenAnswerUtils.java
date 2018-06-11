/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.openAnswer;

import java.text.ParseException;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.OnyxSettings;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner;
import org.obiba.onyx.quartz.editor.utils.QuestionnaireElementCloner.ElementClone;
import org.obiba.onyx.util.data.DataType;
import org.springframework.beans.factory.annotation.Required;

/**
 *
 */
public class OpenAnswerUtils {

  private OnyxSettings onyxSettings;

  public boolean isValidDefaultValue(DataType dataType, String value) {
    switch(dataType) {
      case DATE:
        try {
          onyxSettings.getDateFormat().parse(value);
        } catch(ParseException nfe) {
          return false;
        }
        break;
      case DECIMAL:
        try {
          Double.parseDouble(value);
        } catch(NumberFormatException nfe) {
          return false;
        }
        break;
      case INTEGER:
        try {
          Long.parseLong(value);
        } catch(NumberFormatException nfe) {
          return false;
        }
        break;
      case TEXT:
        break;
      case BOOLEAN:
        break;
      case DATA:
        break;
    }
    return true;
  }

  public static synchronized void rollback(OpenAnswerDefinition modified, ElementClone<OpenAnswerDefinition> original,
      LocaleProperties localeProperties, Questionnaire questionnaire, Category category) {
    if(category.getOpenAnswerDefinition() == modified) { // parent open answer
      category.setOpenAnswerDefinition(original.getElement());
      for(OpenAnswerDefinition child : modified.getOpenAnswerDefinitions()) {
        original.getElement().addOpenAnswerDefinition(child);
      }
    } else {
      int index = category.getOpenAnswerDefinition().getOpenAnswerDefinitions().indexOf(modified);
      category.getOpenAnswerDefinition().removeOpenAnswerDefinition(modified);
      category.getOpenAnswerDefinition().addOpenAnswerDefinition(original.getElement(), index);
    }
    localeProperties.remove(questionnaire, modified);
    QuestionnaireElementCloner.addProperties(original, localeProperties);
  }

  @Required
  public void setOnyxSettings(OnyxSettings onyxSettings) {
    this.onyxSettings = onyxSettings;
  }

}
