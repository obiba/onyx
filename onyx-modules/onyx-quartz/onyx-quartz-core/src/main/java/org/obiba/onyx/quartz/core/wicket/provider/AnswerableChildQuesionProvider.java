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

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

/**
 *
 */
public class AnswerableChildQuesionProvider extends AbstractChildQuestionProvider {

  private static final long serialVersionUID = 5289961743689207044L;

  /**
   * @param model
   */
  public AnswerableChildQuesionProvider(IModel<Question> model) {
    super(model);
  }

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @Override
  protected boolean acceptChild(Question question) {
    return question.isToBeAnswered(activeQuestionnaireAdministrationService);
  }

}
