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

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;

public class Page implements IHasQuestion {

  private static final long serialVersionUID = -7732601103831162009L;

  private String name;

  private Section section;

  private String uIFactoryName;

  private List<Question> questions;

  public Page(String name) {
    this.name = name;
  }

  public Section getSection() {
    return section;
  }

  public void setSection(Section section) {
    this.section = section;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUIFactoryName() {
    return uIFactoryName;
  }

  public void setUIFactoryName(String factoryName) {
    uIFactoryName = factoryName;
  }

  @Override
  public List<Question> getQuestions() {
    return questions != null ? questions : (questions = new ArrayList<Question>());
  }

  @Override
  public void addQuestion(Question question) {
    if(question != null && getQuestions().add(question)) {
      question.setPage(this);
    }
  }

  @Override
  public void addQuestion(Question question, int index) {
    if(question != null) {
      getQuestions().add(index, question);
      question.setPage(this);
    }
  }

  @Override
  public void removeQuestion(Question question) {
    if(question != null && getQuestions().remove(question)) {
      question.setPage(null);
    }
  }

  @Override
  public void accept(IVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return getName();
  }
}
