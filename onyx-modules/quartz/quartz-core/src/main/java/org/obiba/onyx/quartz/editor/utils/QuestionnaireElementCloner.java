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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties.KeyValue;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.IDataValidator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class QuestionnaireElementCloner {

  /**
   * Return a clone of given element, clone contains element and his LocaleProperties
   * @param questionCategory
   * @param settings
   * @param localeProperties
   * @return
   */
  public static ElementClone<QuestionCategory> clone(QuestionCategory questionCategory, CloneSettings settings, LocaleProperties localeProperties) {
    return createElementClone(questionCategory, clone(questionCategory, settings), localeProperties);
  }

  /**
   * Return a clone of given element, clone contains element and his LocaleProperties
   * @param openAnswer
   * @param settings
   * @param localeProperties
   * @return
   */
  public static ElementClone<OpenAnswerDefinition> clone(OpenAnswerDefinition openAnswer, CloneSettings settings, LocaleProperties localeProperties) {
    return createElementClone(openAnswer, clone(openAnswer, settings), localeProperties);
  }

  /**
   * Return a clone of given element, clone contains element and his LocaleProperties
   * @param question
   * @param settings
   * @param localeProperties
   * @return
   */
  public static ElementClone<Question> clone(Question question, CloneSettings settings, LocaleProperties localeProperties) {
    return createElementClone(question, clone(question, settings), localeProperties);
  }

  /**
   * Add properties of clone into given localeProperties
   * @param clone
   * @param localeProperties
   */
  public static void addProperties(ElementClone<? extends IQuestionnaireElement> clone, LocaleProperties localeProperties) {
    localeProperties.addElementLabel(clone.getElement(), clone.getLocaleProperties().getElementLabels(clone.getElement()));
  }

  private static Question clone(Question question, CloneSettings settings) {
    Question clone = new Question();
    copy(question, clone, settings);
    return clone;
  }

  private static void copy(Question from, Question to, CloneSettings settings) {
    if(!settings.isRenameQuestion()) {
      to.setName(from.getName());
    } else {
      // TODO currentTimeMillis to ensure unique name (other solution ?)
      to.setName("_" + from.getName() + "_" + System.currentTimeMillis());
    }
    to.setCondition(from.getCondition());
    to.setMaxCount(from.getMaxCount());
    to.setMinCount(from.getMinCount());
    to.setMultiple(from.isMultiple());
    to.setNumber(from.getNumber());
    to.setUIFactoryName(from.getUIFactoryName());
    if(settings.isCloneVariableName()) to.setVariableName(from.getVariableName());
    to.getQuestionCategories().addAll(from.getQuestionCategories());

    if(settings.isCloneChildQuestion()) {
      for(Question child : from.getQuestions()) {
        to.addQuestion(clone(child, settings));
      }
    } else {
      for(Question child : from.getQuestions()) {
        to.addQuestion(child);
      }
    }

    ValueMap uiArgumentsValueMap = from.getUIArgumentsValueMap();
    if(uiArgumentsValueMap != null) {
      for(Entry<String, Object> entry : uiArgumentsValueMap.entrySet()) {
        to.addUIArgument(entry.getKey(), (String) entry.getValue());
      }
    }
  }

  private static <T extends IQuestionnaireElement> ElementClone<T> createElementClone(T element, T elementCloned, LocaleProperties localeProperties) {
    ListMultimap<Locale, KeyValue> elementLabel = localeProperties.getElementLabels(element);
    ListMultimap<Locale, KeyValue> elementLabelClone = ArrayListMultimap.create();
    if(elementLabel != null) {
      for(Locale locale : localeProperties.getLocales()) {
        List<KeyValue> keyValues = elementLabel.get(locale);
        for(KeyValue keyValue : keyValues) {
          elementLabelClone.put(locale, keyValue.duplicate());
        }
      }
    }
    LocaleProperties localePropertiesClone = new LocaleProperties();
    localePropertiesClone.setLocales(new ArrayList<Locale>(localeProperties.getLocales()));
    localePropertiesClone.addElementLabel(elementCloned, elementLabelClone);
    return new ElementClone<T>(elementCloned, localePropertiesClone);
  }

  private static QuestionCategory clone(QuestionCategory questionCategory, CloneSettings settings) {
    QuestionCategory clone = new QuestionCategory();
    copy(questionCategory, clone, settings);
    return clone;
  }

  private static void copy(QuestionCategory from, QuestionCategory to, CloneSettings settings) {
    to.setQuestion(from.getQuestion());
    to.setExportName(from.getExportName());
    Category category = new Category();
    to.setCategory(category);
    copy(from.getCategory(), category, settings);
  }

  public static void copy(Category from, Category to, CloneSettings settings) {
    to.setName(from.getName());
    to.setEscape(from.isEscape());
    to.setNoAnswer(from.isNoAnswer());
    if(settings.isCloneVariableName()) {
      to.clearVariableNames();
      for(Entry<String, String> entry : from.getVariableNames().entrySet()) {
        to.addVariableName(entry.getKey(), entry.getValue());
      }
    }

    OpenAnswerDefinition openAnswer = from.getOpenAnswerDefinition();
    if(openAnswer != null) {
      if(settings.isCloneChildOpenAnswer()) {
        OpenAnswerDefinition cloneOpenAnswer = clone(openAnswer, settings);
        to.setOpenAnswerDefinition(cloneOpenAnswer);
        for(OpenAnswerDefinition child : openAnswer.getOpenAnswerDefinitions()) {
          cloneOpenAnswer.addOpenAnswerDefinition(clone(child, settings));
        }
      } else {
        to.setOpenAnswerDefinition(openAnswer);
      }
    } else {
      to.setOpenAnswerDefinition(openAnswer);
    }
  }

  private static OpenAnswerDefinition clone(OpenAnswerDefinition openAnswer, CloneSettings settings) {
    OpenAnswerDefinition clone = new OpenAnswerDefinition();
    copy(openAnswer, clone, settings);
    return clone;
  }

  private static void copy(OpenAnswerDefinition from, OpenAnswerDefinition to, CloneSettings settings) {
    if(!settings.isRenameOpenAnswer()) {
      to.setName(from.getName());
    } else {
      // TODO currentTimeMillis to ensure unique name (other solution ?)
      to.setName("_" + from.getName() + "_" + System.currentTimeMillis());
    }
    to.setDataType(from.getDataType());
    to.setDataSource(from.getDataSource());
    to.setRequired(from.isRequired());
    to.setUnit(from.getUnit());
    for(IDataValidator<?> validator : from.getDataValidators()) {
      to.addDataValidator(validator);
    }
    ValueMap uiArgumentsValueMap = from.getUIArgumentsValueMap();
    if(uiArgumentsValueMap != null) {
      for(Entry<String, Object> entry : uiArgumentsValueMap.entrySet()) {
        to.addUIArgument(entry.getKey(), (String) entry.getValue());
      }
    }
    for(ComparingDataSource dataSource : from.getValidationDataSources()) {
      to.addValidationDataSource(dataSource);
    }
    for(Data data : from.getDefaultValues()) {
      to.addDefaultValue(data);
    }
    if(settings.isCloneVariableName()) {
      for(Entry<String, String> entry : from.getVariableNames().entrySet()) {
        to.addVariableName(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Represents an IQuestionnaireElement and his own LocaleProperties
   * 
   * @param <T>
   */
  public static class ElementClone<T extends IQuestionnaireElement> implements Serializable {

    private static final long serialVersionUID = 1L;

    private T element;

    private LocaleProperties localeProperties;

    public ElementClone(T element, LocaleProperties localeProperties) {
      this.element = element;
      this.localeProperties = localeProperties;
    }

    public T getElement() {
      return element;
    }

    public void setElement(T element) {
      this.element = element;
    }

    public LocaleProperties getLocaleProperties() {
      return localeProperties;
    }

    public void setLocaleProperties(LocaleProperties localeProperties) {
      this.localeProperties = localeProperties;
    }

  }

  // TODO use flags
  public static class CloneSettings {

    private boolean cloneVariableName = true;

    private boolean cloneChildQuestion = false;

    private boolean renameOpenAnswer = false;

    /**
     * used to clone open answer of a category
     */
    private boolean cloneChildOpenAnswer = true;

    private boolean renameQuestion = false;

    public CloneSettings(boolean cloneVariableName) {
      this(cloneVariableName, false, false);
    }

    public CloneSettings(boolean cloneVariableName, boolean cloneChildQuestion) {
      this(cloneVariableName, cloneChildQuestion, false);
    }

    public CloneSettings(boolean cloneVariableName, boolean cloneChildQuestion, boolean renameOpenAnswer) {
      this.cloneVariableName = cloneVariableName;
      this.cloneChildQuestion = cloneChildQuestion;
      this.renameOpenAnswer = renameOpenAnswer;
    }

    public CloneSettings(boolean cloneVariableName, boolean cloneChildQuestion, boolean renameOpenAnswer, boolean cloneChildOpenAnswer) {
      this.cloneVariableName = cloneVariableName;
      this.cloneChildQuestion = cloneChildQuestion;
      this.renameOpenAnswer = renameOpenAnswer;
      this.cloneChildOpenAnswer = cloneChildOpenAnswer;
    }

    public CloneSettings(boolean cloneVariableName, boolean cloneChildQuestion, boolean renameOpenAnswer, boolean cloneChildOpenAnswer, boolean renameQuestion) {
      this.cloneVariableName = cloneVariableName;
      this.cloneChildQuestion = cloneChildQuestion;
      this.renameOpenAnswer = renameOpenAnswer;
      this.cloneChildOpenAnswer = cloneChildOpenAnswer;
      this.renameQuestion = renameQuestion;
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

    public boolean isRenameOpenAnswer() {
      return renameOpenAnswer;
    }

    public void setRenameOpenAnswer(boolean renameOpenAnswer) {
      this.renameOpenAnswer = renameOpenAnswer;
    }

    public boolean isCloneChildOpenAnswer() {
      return cloneChildOpenAnswer;
    }

    public void setCloneChildOpenAnswer(boolean cloneChildOpenAnswer) {
      this.cloneChildOpenAnswer = cloneChildOpenAnswer;
    }

    public boolean isRenameQuestion() {
      return renameQuestion;
    }

    public void setRenameQuestion(boolean renameQuestion) {
      this.renameQuestion = renameQuestion;
    }

  }
}
