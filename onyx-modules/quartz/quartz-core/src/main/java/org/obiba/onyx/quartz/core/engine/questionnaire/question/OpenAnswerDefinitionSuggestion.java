/*
 * ***************************************************************************
 *  Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *  <p/>
 *  This program and the accompanying materials
 *  are made available under the terms of the GNU Public License v3.0.
 *  <p/>
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  ****************************************************************************
 */
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.util.data.DataType;
import org.springframework.util.Assert;

import static org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition.OpenAnswerType;

/**
 *
 */
public class OpenAnswerDefinitionSuggestion implements Serializable {

  private static final String SUGGESTION_ITEMS = "suggest.items";

  private static final String SUGGESTION_LABELS = "suggest.labels";

  private static final String SUGGESTION_VARIABLE = "suggest.variable";

  private static final String SUGGESTION_VARIABLE_SELECT_ENTITY = "suggest.variable.selectEntity";

  private static final String SUGGESTION_VARIABLE_MAX_COUNT = "suggest.variable.maxCount";



  public enum Source {
    ITEMS_LIST, VARIABLE_VALUES
  }

  private final OpenAnswerDefinition openAnswer;

  public OpenAnswerDefinitionSuggestion(OpenAnswerDefinition openAnswer) {
    Assert.notNull(openAnswer);
    this.openAnswer = openAnswer;
  }

  public static OpenAnswerDefinitionSuggestion createNewSuggestionOpenAnswer() {
    OpenAnswerDefinition openAnswer = new OpenAnswerDefinition();
    openAnswer.setDataType(DataType.TEXT);
    openAnswer.addUIArgument(OpenAnswerType.UI_ARGUMENT_KEY, OpenAnswerType.AUTO_COMPLETE.getUiArgument());
    return new OpenAnswerDefinitionSuggestion(openAnswer);
  }

  public OpenAnswerDefinition getOpenAnswerDefinition() {
    return openAnswer;
  }

  public void addSuggestionItem(String item) {
    if(StringUtils.isNotBlank(item)) openAnswer.addUIArgument(SUGGESTION_ITEMS, item);
  }

  public void removeSuggestionItem(String item) {
    if(StringUtils.isNotBlank(item)) {
      openAnswer.removeUIArgument(SUGGESTION_ITEMS, item);

      // remove suggestion labels for all locales
      Iterator<String[]> it = openAnswer.getUIArgumentsIterator();
      if(it != null) {
        String keyStart = SUGGESTION_LABELS + ":" + item + ":";
        int keyLen = keyStart.length() + 2;
        while(it.hasNext()) {
          String[] strings = it.next();
          if(strings.length > 0) {
            String localizedKey = strings[0];
            if(localizedKey.length() == keyLen && localizedKey.startsWith(keyStart)) {
              it.remove();
              break;
            }
          }
        }
      }
    }
  }

  public void setSuggestionLabel(Locale locale, String item,
      String label) {
    if(locale != null && StringUtils.isNotBlank(item)) {
      removeSuggestionLabel(locale, item);
      if(StringUtils.isNotBlank(label)) {
        openAnswer.addUIArgument(getSuggestionLabelKey(locale, item), label);
      }
    }
  }

  public List<String> getSuggestionItems() {
    ValueMap valueMap = openAnswer.getUIArgumentsValueMap();
    if(valueMap == null) return Collections.emptyList();
    String[] array = valueMap.getStringArray(SUGGESTION_ITEMS);
    return (List<String>) (array == null ? Collections.emptyList() : Arrays.asList(array));
  }

  public String getSuggestionLabel(Locale locale, String item) {
    ValueMap valueMap = openAnswer.getUIArgumentsValueMap();
    if(valueMap == null || StringUtils.isBlank(item)) return null;
    if(locale == null) return item;
    return valueMap.getString(getSuggestionLabelKey(locale, item));
  }

  public void removeSuggestionLabel(Locale locale, String item) {
    if(locale != null && StringUtils.isNotBlank(item)) {
      openAnswer.removeUIArgument(getSuggestionLabelKey(locale, item));
    }
  }

  public boolean hasSuggestionItems() {
    return !getSuggestionItems().isEmpty();
  }

  private static String getSuggestionLabelKey(Locale locale, String item) {
    return SUGGESTION_LABELS + ":" + item + ":" + locale.getLanguage();
  }

  public boolean hasVariableValues() {
    Iterator<String[]> it = openAnswer.getUIArgumentsIterator();
    if(it != null) {
      String keyStart = SUGGESTION_VARIABLE + ":";
      int keyLen = keyStart.length() + 2;
      while(it.hasNext()) {
        String[] strings = it.next();
        if(strings.length > 0) {
          String localizedKey = strings[0];
          if(localizedKey.length() == keyLen && localizedKey.startsWith(keyStart)) return true;
        }
      }
    }
    return false;
  }

  public void setVariableValues(Locale locale, String variableName) {
    openAnswer.removeUIArgument(getVariableKey(locale));
    openAnswer.addUIArgument(getVariableKey(locale), variableName);
  }

  public String getVariableValues(Locale locale) {
    ValueMap valueMap = openAnswer.getUIArgumentsValueMap();
    return valueMap == null || locale == null ? null : valueMap.getString(getVariableKey(locale));
  }

  public boolean getVariableSelectEntity() {
    ValueMap valueMap = openAnswer.getUIArgumentsValueMap();
    return valueMap == null ? false : valueMap.getBoolean(SUGGESTION_VARIABLE_SELECT_ENTITY);
  }

  public int getVariableMaxCount() {
    ValueMap valueMap = openAnswer.getUIArgumentsValueMap();
    return valueMap == null ? -1 : valueMap.getInt(SUGGESTION_VARIABLE_MAX_COUNT, -1);
  }

  private static String getVariableKey(Locale locale) {
    return SUGGESTION_VARIABLE + ":" + locale.getLanguage();
  }

  public Source getSuggestionSource() {
    if(hasSuggestionItems()) return Source.ITEMS_LIST;
    if(hasVariableValues()) return Source.VARIABLE_VALUES;
    return null;
  }

}
