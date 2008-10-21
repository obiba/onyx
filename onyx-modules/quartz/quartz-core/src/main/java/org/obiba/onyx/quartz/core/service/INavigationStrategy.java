/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.service;

import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;

/**
 * Interface for questionnaire navigation strategies.
 * 
 * @author cag-dspathis
 * 
 */
public interface INavigationStrategy {
  /**
   * Returns the questionnaire's start page.
   * 
   * @param service service that may be used by implementors to determine the start page (e.g., if the start page
   * depends on whether certain questions have already been answered, this service may be used to check those questions)
   * @return start page
   */
  public Page getPageOnStart(ActiveQuestionnaireAdministrationService service);

  /**
   * Returns the questionnaire page that should be displayed when the "next" event occurs (i.e., when the user navigates
   * forwards).
   * 
   * @param service service that may be used by implementors to determine the next page
   * @param currentPage currently displayed page
   * @return next page
   */
  public Page getPageOnNext(ActiveQuestionnaireAdministrationService service, Page currentPage);

  /**
   * Returns the questionnaire page that should be displayed when the "previous" event occurs (i.e., when the user
   * navigates backwards).
   * 
   * Note: When the user navigates backwards, the pages displayed should follow the path that was originally taken. For
   * example, if the user went from page 1, to 2, to 4, the path back should be 4, to 2, to 1. Implementors must ensure
   * that this method behaves accordingly.
   * 
   * @param service service that may be used by implementors to determine the previous page
   * @param currentPage currently displayed page
   * @return previous page
   */
  public Page getPageOnPrevious(ActiveQuestionnaireAdministrationService service, Page currentPage);

  /**
   * Returns the questionnaire page that should be displayed when the questionnaire stage is resumed (i.e., after having
   * been interrupted).
   * 
   * @param service service that may be used by implementors to determine the page on which to resume
   * @param participant current questionnaire participant, attributes of which may affect the resume page
   * @return page on which to resume
   */
  public Page getPageOnResume(ActiveQuestionnaireAdministrationService service, QuestionnaireParticipant participant);
}
