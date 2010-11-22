/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.utils;

import java.util.Map.Entry;

import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.IDataValidator;

/**
 *
 */
public class QuestionnaireElementCloner {

  public static QuestionCategory cloneQuestionCategory(QuestionCategory questionCategory) {
    QuestionCategory clone = new QuestionCategory();
    clone.setQuestion(questionCategory.getQuestion());
    clone.setExportName(questionCategory.getExportName());

    Category category = questionCategory.getCategory();
    Category cloneCategory = new Category(category.getName());
    clone.setCategory(cloneCategory);
    cloneCategory.setEscape(category.isEscape());
    cloneCategory.setNoAnswer(category.isNoAnswer());
    for(Entry<String, String> entry : category.getVariableNames().entrySet()) {
      cloneCategory.addVariableName(entry.getKey(), entry.getValue());
    }

    OpenAnswerDefinition openAnswer = category.getOpenAnswerDefinition();
    if(openAnswer != null) {
      OpenAnswerDefinition cloneOpenAnswer = cloneOpenAnswerDefinition(openAnswer);
      cloneCategory.setOpenAnswerDefinition(cloneOpenAnswer);
      for(OpenAnswerDefinition child : openAnswer.getOpenAnswerDefinitions()) {
        cloneOpenAnswer.addOpenAnswerDefinition(cloneOpenAnswerDefinition(child));
      }
    }

    return clone;
  }

  public static OpenAnswerDefinition cloneOpenAnswerDefinition(OpenAnswerDefinition openAnswer) {
    OpenAnswerDefinition clone = new OpenAnswerDefinition(openAnswer.getName(), openAnswer.getDataType());
    clone.setDataSource(openAnswer.getDataSource());
    clone.setRequired(openAnswer.isRequired());
    clone.setUnit(openAnswer.getUnit());
    for(IDataValidator<?> validator : openAnswer.getDataValidators()) {
      clone.addDataValidator(validator);
    }
    ValueMap uiArgumentsValueMap = openAnswer.getUIArgumentsValueMap();
    if(uiArgumentsValueMap != null) {
      for(Entry<String, Object> entry : uiArgumentsValueMap.entrySet()) {
        clone.addUIArgument(entry.getKey(), (String) entry.getValue());
      }
    }
    for(ComparingDataSource dataSource : openAnswer.getValidationDataSources()) {
      clone.addValidationDataSource(dataSource);
    }
    for(Data data : openAnswer.getDefaultValues()) {
      clone.addDefaultValue(data);
    }
    for(Entry<String, String> entry : openAnswer.getVariableNames().entrySet()) {
      clone.addVariableName(entry.getKey(), entry.getValue());
    }
    return clone;
  }

}
