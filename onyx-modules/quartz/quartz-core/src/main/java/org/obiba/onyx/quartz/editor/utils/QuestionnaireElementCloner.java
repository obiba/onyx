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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.IDataValidator;

/**
 *
 */
public class QuestionnaireElementCloner {

  /**
   * Simple question clone
   * @param question
   * @return
   */
  public static Question cloneQuestion(Question question, CloneSettings settings) {
    Question clone = new Question(question.getName());
    clone.setCondition(question.getCondition());
    clone.setMaxCount(question.getMaxCount());
    clone.setMinCount(question.getMinCount());
    clone.setMultiple(question.isMultiple());
    clone.setNumber(question.getNumber());
    clone.setPage(question.getPage());
    clone.setParentQuestion(question.getParentQuestion());
    clone.setUIFactoryName(question.getUIFactoryName());
    if(settings.isCloneVariableName()) clone.setVariableName(question.getVariableName());
    clone.getQuestionCategories().addAll(question.getQuestionCategories());

    if(settings.isCloneChildQuestion()) {
      for(Question child : question.getQuestions()) {
        clone.addQuestion(cloneQuestion(child, settings));
      }
    } else {
      clone.getQuestions().addAll(question.getQuestions());
    }

    ValueMap uiArgumentsValueMap = question.getUIArgumentsValueMap();
    if(uiArgumentsValueMap != null) {
      for(Entry<String, Object> entry : uiArgumentsValueMap.entrySet()) {
        clone.addUIArgument(entry.getKey(), (String) entry.getValue());
      }
    }

    return clone;
  }

  public static QuestionCategory cloneQuestionCategory(QuestionCategory questionCategory, CloneSettings settings) {
    QuestionCategory clone = new QuestionCategory();
    clone.setQuestion(questionCategory.getQuestion());
    clone.setExportName(questionCategory.getExportName());

    Category category = questionCategory.getCategory();
    Category cloneCategory = new Category(category.getName());
    clone.setCategory(cloneCategory);
    cloneCategory.setEscape(category.isEscape());
    cloneCategory.setNoAnswer(category.isNoAnswer());
    if(settings.isCloneVariableName()) {
      for(Entry<String, String> entry : category.getVariableNames().entrySet()) {
        cloneCategory.addVariableName(entry.getKey(), entry.getValue());
      }
    }

    OpenAnswerDefinition openAnswer = category.getOpenAnswerDefinition();
    if(openAnswer != null) {
      OpenAnswerDefinition cloneOpenAnswer = cloneOpenAnswerDefinition(openAnswer, settings);
      cloneCategory.setOpenAnswerDefinition(cloneOpenAnswer);
      for(OpenAnswerDefinition child : openAnswer.getOpenAnswerDefinitions()) {
        cloneOpenAnswer.addOpenAnswerDefinition(cloneOpenAnswerDefinition(child, settings));
      }
    }

    return clone;
  }

  public static OpenAnswerDefinition cloneOpenAnswerDefinition(OpenAnswerDefinition openAnswer, CloneSettings settings) {
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
    if(settings.isCloneVariableName()) {
      for(Entry<String, String> entry : openAnswer.getVariableNames().entrySet()) {
        clone.addVariableName(entry.getKey(), entry.getValue());
      }
    }
    return clone;
  }

  public static class CloneSettings {

    private boolean cloneVariableName = true;

    private boolean cloneChildQuestion;

    public CloneSettings(boolean cloneVariableName) {
      this(cloneVariableName, false);
    }

    public CloneSettings(boolean cloneVariableName, boolean cloneChildQuestion) {
      this.cloneVariableName = cloneVariableName;
      this.cloneChildQuestion = cloneChildQuestion;
    }

    public boolean isCloneVariableName() {
      return cloneVariableName;
    }

    public void setCloneVariableName(boolean cloneVariableName) {
      this.cloneVariableName = cloneVariableName;
    }

    public boolean isCloneChildQuestion() {
      return cloneChildQuestion;
    }

    public void setCloneChildQuestion(boolean cloneChildQuestion) {
      this.cloneChildQuestion = cloneChildQuestion;
    }

  }

}
