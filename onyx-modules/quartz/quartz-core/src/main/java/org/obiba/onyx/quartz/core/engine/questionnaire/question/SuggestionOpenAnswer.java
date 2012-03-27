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

import org.obiba.onyx.util.data.DataType;

/**
 *
 */
public class SuggestionOpenAnswer implements Serializable {

  private static final String SUGGESTION_ITEMS = "suggest.items";

  private static final String SUGGESTION_LABELS = "suggest.labels";

  private static final String SUGGESTION_VARIABLE = "suggest.variable";

  private static final String SUGGESTION_VARIABLE_SELECT_ENTITY = "suggest.variable.selectEntity";

  private static final String SUGGESTION_VARIABLE_MAX_COUNT = "suggest.variable.maxCount";

  private final OpenAnswerDefinition openAnswer;

  public SuggestionOpenAnswer(OpenAnswerDefinition openAnswer) {
    this.openAnswer = openAnswer;
  }

  public static SuggestionOpenAnswer createNewSuggestionOpenAnswer() {
    OpenAnswerDefinition openAnswer = new OpenAnswerDefinition();
    openAnswer.setDataType(DataType.TEXT);
    openAnswer.addUIArgument(
        OpenAnswerDefinition.OpenAnswerType.UI_ARGUMENT_KEY,
        OpenAnswerDefinition.OpenAnswerType.AUTO_COMPLETE.getUiArgument());
    return new SuggestionOpenAnswer(openAnswer);
  }

}
