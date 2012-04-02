/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition.OpenAnswerType;
import org.obiba.onyx.util.data.DataType;
import org.springframework.util.Assert;

/**
 *
 */
public class OpenAnswerDefinitionSuggestion implements Serializable {

  private static final long serialVersionUID = -8790254697570036428L;

  private static final String SUGGESTION_MAX_COUNT = "suggest.maxCount";

  private static final String SUGGESTION_ITEMS = "suggest.items";

  private static final String SUGGESTION_TABLE = "suggest.table";

  private static final String SUGGESTION_ENTITY_TYPE = "suggest.entityType";

  private static final String SUGGESTION_VARIABLE = "suggest.variable";

  private static final String SUGGESTION_NEW_VALUE_ALLOWED = "suggest.newValue.allowed";

  private static final String SUGGESTION_NEW_VALUE_PATTERN = "suggest.newValue.pattern";

  public enum Source {
    ITEMS_LIST, VARIABLE_VALUES
  }

  private final OpenAnswerDefinition openAnswer;

  public OpenAnswerDefinitionSuggestion(OpenAnswerDefinition openAnswer) {
    Assert.notNull(openAnswer);
    this.openAnswer = openAnswer;
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static OpenAnswerDefinitionSuggestion createNewSuggestionOpenAnswer() {
    OpenAnswerDefinition openAnswer = new OpenAnswerDefinition();
    openAnswer.setDataType(DataType.TEXT);
    openAnswer.addUIArgument(OpenAnswerType.UI_ARGUMENT_KEY, OpenAnswerType.AUTO_COMPLETE.getUiArgument());
    return new OpenAnswerDefinitionSuggestion(openAnswer);
  }

  public void resetSuggestionOpenAnswerDefinition() {
    openAnswer.setDataType(DataType.TEXT);
    openAnswer.clearUIArgument();
    openAnswer.addUIArgument(OpenAnswerType.UI_ARGUMENT_KEY, OpenAnswerType.AUTO_COMPLETE.getUiArgument());
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
    }
  }

  public List<String> getSuggestionItems() {
    ValueMap valueMap = openAnswer.getUIArgumentsValueMap();
    if(valueMap == null) return Collections.emptyList();
    String[] array = valueMap.getStringArray(SUGGESTION_ITEMS);
    return array == null ? Collections.<String> emptyList() : Arrays.asList(array);
  }

  public boolean hasSuggestionItems() {
    return !getSuggestionItems().isEmpty();
  }

  public boolean hasVariable() {
    return getTable() != null;
  }

  public void setVariableValues(Locale locale, String variableName) {
    openAnswer.replaceUIArgument(getVariableKey(locale), variableName);
  }

  public void clearVariableValues() {
    Iterator<String[]> it = openAnswer.getUIArgumentsIterator();
    if(it != null) {
      String keyStart = SUGGESTION_VARIABLE + ":";
      int keyLen = keyStart.length() + 2;
      while(it.hasNext()) {
        String[] strings = it.next();
        if(strings.length > 0) {
          String localizedKey = strings[0];
          if(localizedKey.length() == keyLen && localizedKey.startsWith(keyStart)) {
            it.remove();
          }
        }
      }
    }
  }

  public String getVariableValues(Locale locale) {
    return locale == null ? null : getValueMapString(getVariableKey(locale));
  }

  public String getTable() {
    return getValueMapString(SUGGESTION_TABLE);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setTable(String table) {
    setValueMapString(SUGGESTION_TABLE, table);
  }

  public void setEntityType(String entityType) {
    setValueMapString(SUGGESTION_ENTITY_TYPE, entityType);
  }

  public String getEntityType() {
    return getValueMapString(SUGGESTION_ENTITY_TYPE);
  }

  @SuppressWarnings("UnusedDeclaration")
  public Integer getMaxCount() {
    ValueMap valueMap = openAnswer.getUIArgumentsValueMap();
    return valueMap == null ? null : valueMap.getAsInteger(SUGGESTION_MAX_COUNT);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setMaxCount(Integer maxCount) {
    setValueMapString(SUGGESTION_MAX_COUNT, maxCount);
  }

  private static String getVariableKey(Locale locale) {
    return SUGGESTION_VARIABLE + ":" + locale.getLanguage();
  }

  public Source getSuggestionSource() {
    if(hasSuggestionItems()) return Source.ITEMS_LIST;
    if(hasVariable()) return Source.VARIABLE_VALUES;
    return null;
  }

  public String getDatasource() {
    String table = getTable();
    return table != null && table.contains(".") ? table.split("\\.")[0] : null;
  }

  public Boolean getNewValueAllowed() {
    ValueMap valueMap = openAnswer.getUIArgumentsValueMap();
    return valueMap != null && valueMap.getAsBoolean(SUGGESTION_NEW_VALUE_ALLOWED, false);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setNewValueAllowed(Boolean newValueAllowed) {
    setValueMapString(SUGGESTION_NEW_VALUE_ALLOWED, newValueAllowed);
  }

  public String getNewValuePattern() {
    return getValueMapString(SUGGESTION_NEW_VALUE_PATTERN);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setNewValuePattern(String newValuePattern) {
    setValueMapString(SUGGESTION_NEW_VALUE_PATTERN, newValuePattern);
  }

  private String getValueMapString(String key) {
    ValueMap valueMap = openAnswer.getUIArgumentsValueMap();
    return valueMap == null ? null : valueMap.getString(key);
  }

  private void setValueMapString(String key, Object value) {
    if(value == null) {
      openAnswer.removeUIArgument(key);
    } else {
      openAnswer.replaceUIArgument(key, value.toString());
    }
  }

}
