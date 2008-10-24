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
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for question display.
 * @author Yannick Marcon
 * 
 */
public class QuestionPanel extends Panel {

  private static final long serialVersionUID = -3204686612721034839L;

  private static final Logger log = LoggerFactory.getLogger(QuestionPanel.class);

  @SpringBean
  protected ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  public QuestionPanel(String id, IModel questionModel) {
    super(id, questionModel);
  }

  /**
   * Called when page is left to go to next page.
   */
  public void onNext(AjaxRequestTarget target) {
    Question question = (Question) getModelObject();
    log.info("onNext.{}.active=true", question.getName());
    activeQuestionnaireAdministrationService.setActiveAnswers(question, true);
  }

  /**
   * Called when page is left to go to previous page.
   */
  public void onPrevious(AjaxRequestTarget target) {
    Question question = (Question) getModelObject();
    log.info("onPrevious.{}.active=false", question.getName());
    activeQuestionnaireAdministrationService.setActiveAnswers(question, false);
  }

}
