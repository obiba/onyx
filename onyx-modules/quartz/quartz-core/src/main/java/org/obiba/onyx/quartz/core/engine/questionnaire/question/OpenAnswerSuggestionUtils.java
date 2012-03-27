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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.util.data.DataType;

import static org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition.OpenAnswerType;

/**
 *
 */
public class OpenAnswerSuggestionUtils {

  private static final String SUGGESTION_ITEMS = "suggest.items";

  private static final String SUGGESTION_LABELS = "suggest.labels";

  private static final String SUGGESTION_VARIABLE = "suggest.variable";

  private static final String SUGGESTION_VARIABLE_SELECT_ENTITY = "suggest.variable.selectEntity";

  private static final String SUGGESTION_VARIABLE_MAX_COUNT = "suggest.variable.maxCount";

  public static OpenAnswerDefinition createAutoComplete() {
    OpenAnswerDefinition openAnswerDefinition = new OpenAnswerDefinition();
    openAnswerDefinition.setDataType(DataType.TEXT);
    openAnswerDefinition.addUIArgument(OpenAnswerType.UI_ARGUMENT_KEY, OpenAnswerType.AUTO_COMPLETE.getUiArgument());
    return openAnswerDefinition;
  }

  public static void addSuggestionItem(OpenAnswerDefinition openAnswerDefinition, String item) {
    if(StringUtils.isNotBlank(item)) openAnswerDefinition.addUIArgument(SUGGESTION_ITEMS, item);
  }

  public static void removeSuggestionItem(OpenAnswerDefinition openAnswerDefinition, String item) {
    if(StringUtils.isNotBlank(item)) {
      openAnswerDefinition.removeUIArgument(SUGGESTION_ITEMS, item);

      // remove suggestion labels for all locales
      Iterator<String[]> it = openAnswerDefinition.getUIArgumentsIterator();
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

  public static void setSuggestionLabel(OpenAnswerDefinition openAnswerDefinition, Locale locale, String item,
      String label) {
    if(locale != null && StringUtils.isNotBlank(item)) {
      removeSuggestionLabel(openAnswerDefinition, locale, item);
      if(StringUtils.isNotBlank(label)) {
        openAnswerDefinition.addUIArgument(getSuggestionLabelKey(locale, item), label);
      }
    }
  }

  public static List<String> getSuggestionItems(OpenAnswerDefinition openAnswerDefinition) {
    ValueMap valueMap = openAnswerDefinition.getUIArgumentsValueMap();
    if(valueMap == null) return Collections.emptyList();
    String[] array = valueMap.getStringArray(SUGGESTION_ITEMS);
    return (List<String>) (array == null ? Collections.emptyList() : Arrays.asList(array));
  }

  public static String getSuggestionLabel(OpenAnswerDefinition openAnswerDefinition, Locale locale, String item) {
    ValueMap valueMap = openAnswerDefinition.getUIArgumentsValueMap();
    if(valueMap == null || StringUtils.isBlank(item)) return null;
    if(locale == null) return item;
    return valueMap.getString(getSuggestionLabelKey(locale, item));
  }

  public static void removeSuggestionLabel(OpenAnswerDefinition openAnswerDefinition, Locale locale, String item) {
    if(locale != null && StringUtils.isNotBlank(item)) {
      openAnswerDefinition.removeUIArgument(getSuggestionLabelKey(locale, item));
    }
  }

  public static boolean hasSuggestionItems(OpenAnswerDefinition openAnswerDefinition) {
    return !getSuggestionItems(openAnswerDefinition).isEmpty();
  }

  private static String getSuggestionLabelKey(Locale locale, String item) {
    return SUGGESTION_LABELS + ":" + item + ":" + locale.getLanguage();
  }

  public static boolean hasVariableValues(OpenAnswerDefinition openAnswerDefinition) {
    Iterator<String[]> it = openAnswerDefinition.getUIArgumentsIterator();
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

  public static void setVariableValues(OpenAnswerDefinition openAnswerDefinition, Locale locale,
      String variableName) {
    openAnswerDefinition.removeUIArgument(getVariableKey(locale));
    openAnswerDefinition.addUIArgument(getVariableKey(locale), variableName);
  }

  public static String getVariableValues(OpenAnswerDefinition openAnswerDefinition, Locale locale) {
    ValueMap valueMap = openAnswerDefinition.getUIArgumentsValueMap();
    return valueMap == null || locale == null ? null : valueMap.getString(getVariableKey(locale));
  }

  public static boolean getVariableSelectEntity(OpenAnswerDefinition openAnswerDefinition) {
    ValueMap valueMap = openAnswerDefinition.getUIArgumentsValueMap();
    return valueMap == null ? false : valueMap.getBoolean(SUGGESTION_VARIABLE_SELECT_ENTITY);
  }

  public static int getVariableMaxCount(OpenAnswerDefinition openAnswerDefinition) {
    ValueMap valueMap = openAnswerDefinition.getUIArgumentsValueMap();
    return valueMap == null ? -1 : valueMap.getInt(SUGGESTION_VARIABLE_MAX_COUNT, -1);
  }

  private static String getVariableKey(Locale locale) {
    return SUGGESTION_VARIABLE + ":" + locale.getLanguage();
  }
}
