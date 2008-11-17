/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

import java.io.Serializable;

import org.obiba.onyx.quartz.core.engine.questionnaire.answer.AnswerSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.FixedSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.OpenAnswerSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;

public class DataComparator implements Serializable {

  private static final long serialVersionUID = 6128481252934955909L;

  private ComparisionOperator comparisionOperator;

  private AnswerSource dataSource;

  private OpenAnswerDefinition openAnswerDefinition;

  public DataComparator(ComparisionOperator comparisionOperator, OpenAnswerDefinition openAnswerDefinition, Data data) {
    this(comparisionOperator, openAnswerDefinition, new FixedSource(data));
  }

  public DataComparator(ComparisionOperator comparisionOperator, Question question, OpenAnswerDefinition openAnswerDefinition, Category category, OpenAnswerDefinition openAnswerDefinitionSource) {
    this(comparisionOperator, openAnswerDefinition, new OpenAnswerSource(question, category, openAnswerDefinitionSource));
  }

  public DataComparator(ComparisionOperator comparisionOperator, OpenAnswerDefinition openAnswerDefinition, AnswerSource dataSource) {
    super();
    this.comparisionOperator = comparisionOperator;
    this.dataSource = dataSource;
    this.openAnswerDefinition = openAnswerDefinition;
  }

  public ComparisionOperator getComparisionOperator() {
    return comparisionOperator;
  }

  public void setComparisionOperator(ComparisionOperator comparisionOperator) {
    this.comparisionOperator = comparisionOperator;
  }

  public OpenAnswerDefinition getOpenAnswerDefinition() {
    return openAnswerDefinition;
  }

  public void setOpenAnswerDefinition(OpenAnswerDefinition openAnswerDefinition) {
    this.openAnswerDefinition = openAnswerDefinition;
  }

  public boolean compare(ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService, Data dataToCompare) {
    if(dataSource == null) {
      if(dataToCompare == null) return true;
      else
        return false;
    }

    Data data = dataSource.getData(activeQuestionnaireAdministrationService);
    if(dataToCompare == null) return (data == null);

    return isComparisonValid(dataToCompare.compareTo(data));
  }

  protected boolean isComparisonValid(int result) {

    switch(getComparisionOperator()) {
    case eq:
      return result == 0;
    case ne:
      return result != 0;
    case lt:
      return result < 0;
    case le:
      return result <= 0;
    case gt:
      return result > 0;
    case ge:
      return result >= 0;
    default:
      return false;
    }
  }
}
