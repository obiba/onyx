/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

/**
 * Base class for question display.
 * @author Yannick Marcon
 * 
 */
public abstract class QuestionPanel extends Panel {

  @SpringBean
  protected ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  public QuestionPanel(String id, Question question) {
    super(id, new Model(question));
  }

  /**
   * Called when page is left to go to next page.
   */
  public void onNext(AjaxRequestTarget target) {
    activeQuestionnaireAdministrationService.setActiveAnswers((Question) getModelObject(), true);
  }

  /**
   * Called when page is left to go to previous page.
   */
  public void onPrevious(AjaxRequestTarget target) {
    activeQuestionnaireAdministrationService.setActiveAnswers((Question) getModelObject(), false);
  }

}
