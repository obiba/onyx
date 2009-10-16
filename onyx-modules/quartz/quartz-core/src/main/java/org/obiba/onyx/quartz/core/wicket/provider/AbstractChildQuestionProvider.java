/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.provider;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public abstract class AbstractChildQuestionProvider extends AbstractQuestionnaireElementProvider<Question, Question> {

  private static final long serialVersionUID = 227294946626164090L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractChildQuestionProvider.class);

  public AbstractChildQuestionProvider(IModel<Question> model) {
    super(model);
  }

  protected List<Question> getElementList() {
    List<Question> selectedChilds = new ArrayList<Question>();
    for(Question question : getProviderElement().getQuestions()) {
      if(acceptChild(question)) {
        selectedChilds.add(question);
      }
    }
    return selectedChilds;
  }

  protected abstract boolean acceptChild(Question question);

}
