/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;

public class QuestionCategory implements Serializable, IQuestionnaireElement {

  private static final long serialVersionUID = 5244745063169629959L;

  private Question question;

  private Category category;

  private boolean reselectable;

  private boolean selected;

  private String exportName;

  public QuestionCategory() {
  }

  public Question getQuestion() {
    return question;
  }

  public void setQuestion(Question question) {
    this.question = question;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public boolean isReselectable() {
    return reselectable;
  }

  public void setReselectable(boolean reselectable) {
    this.reselectable = reselectable;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isEscape() {
    return category.isEscape();
  }

  public String getExportName() {
    return exportName;
  }

  public void setExportName(String exportName) {
    this.exportName = exportName;
  }

  public String getName() {
    return question.getName() + "." + category.getName();
  }

  public void accept(IVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return getName();
  }

}
