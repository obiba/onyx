/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question.condition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.editor.question.condition.datasource.ComparingDS;
import org.obiba.onyx.quartz.editor.question.condition.datasource.QuestionnaireDS;

/**
 *
 */
public class Conditions implements Serializable {

  private static final long serialVersionUID = 1L;

  private String expression;

  private List<QuestionnaireDS> questionnaireDataSources = new ArrayList<QuestionnaireDS>();

  private List<ComparingDS> comparingDataSources = new ArrayList<ComparingDS>();

  public List<QuestionnaireDS> getQuestionnaireDataSources() {
    return questionnaireDataSources;
  }

  public void setQuestionnaireDataSources(List<QuestionnaireDS> dataSources) {
    this.questionnaireDataSources = dataSources;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public List<ComparingDS> getComparingDataSources() {
    return comparingDataSources;
  }

  public void setComparingDataSources(List<ComparingDS> comparingDataSources) {
    this.comparingDataSources = comparingDataSources;
  }

  public int getNbDataSources() {
    return comparingDataSources.size() + questionnaireDataSources.size();
  }

}
