/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.service.impl;

import java.util.List;

import org.apache.wicket.Session;
import org.obiba.onyx.core.exception.ExceptionUtils;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.service.INavigationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default questionnaire navigation strategy.
 * 
 * @author cag-dspathis
 * 
 */
public class DefaultNavigationStrategyImpl implements INavigationStrategy {

  private static final Logger log = LoggerFactory.getLogger(DefaultNavigationStrategyImpl.class);

  /**
   * Returns the earliest page of the questionnaire containing either no questions or at least one question without an
   * answer source.
   * 
   * In effect, this method skips over pages containing only questions with an answer source.
   * 
   * @param service service
   * @return start page (first page that either has no questions or contains at least one question without an answer
   * source)
   */
  public Page getPageOnStart(ActiveQuestionnaireAdministrationService service) {
    Page startPage = null;

    Questionnaire questionnaire = service.getQuestionnaire();

    List<Page> pages = questionnaire.getPages();

    for(Page page : pages) {
      if(page.getQuestions().isEmpty() || NavigationStrategySupport.hasQuestionToBeAnswered(service, page)) {
        startPage = page;
        break;
      }
    }

    return startPage;
  }

  /**
   * Returns the last page arrived at, when beginning from the start page (<code>getPageOnStart</code>) and navigating
   * forwards (<code>getPageOnNext</code>).
   * 
   * @service service
   * @return last page (last page arrived at when navigating forwards from the start page)
   */
  public Page getPageOnLast(ActiveQuestionnaireAdministrationService service) {
    Page lastPage = null;

    Page page = getPageOnStart(service);

    while(page != null) {
      lastPage = page;
      page = getPageOnNext(service, page);
    }

    return lastPage;
  }

  /**
   * Returns the earliest page, after the current page, containing either no questions or at least one question that is
   * to be answered (whether or not an answer currently exists).
   * 
   * In effect, this method skips over pages containing only questions that are not to be answered.
   * 
   * @param service service
   * @param currentPage currently displayed page
   * @return next page (first page, after current page, with either no questions or at least one question to be
   * answered), or <code>null</code> if the current page is <code>null</code>
   */
  public Page getPageOnNext(ActiveQuestionnaireAdministrationService service, Page currentPage) {
    Page nextPage = null;

    if(currentPage != null) {
      Questionnaire questionnaire = service.getQuestionnaire();

      List<Page> pages = questionnaire.getPages();

      int currentPageIndex = pages.indexOf(currentPage);

      for(int i = currentPageIndex + 1; i < pages.size(); i++) {
        Page page = pages.get(i);
        try {
          if(page.getQuestions().isEmpty() || NavigationStrategySupport.hasQuestionToBeAnswered(service, page)) {
            nextPage = page;
            break;
          }
        } catch(Exception e) {
          log.error("Error when evaluating if a question is to be answered in page: {}", page.getName());
          log.error("", e);
          Session.get().error(ExceptionUtils.getCauseMessage(e));
        }
      }
    }

    return nextPage;
  }

  /**
   * Returns the page from which the current page was arrived at.
   * 
   * Earlier pages of the questionnaire that were skipped over when the user navigated to the current page (see
   * <code>getPageOnNext</code>) will also be skipped over by this method.
   * 
   * @param service service
   * @param currentPage currently displayed page
   * @return previous page (page displayed before the current page), or <code>null</code> if the current page is
   * <code>null</code>
   */
  public Page getPageOnPrevious(ActiveQuestionnaireAdministrationService service, Page currentPage) {
    Page previousPage = null;

    if(currentPage != null) {
      Page page = getPageOnStart(service);

      if(currentPage.equals(page)) {
        // ONYX-417 cannot go before the start page.
        previousPage = page;
      } else {
        while(!page.getName().equals(currentPage.getName())) {
          previousPage = page;
          page = getPageOnNext(service, page);
        }
      }
    }

    return previousPage;
  }

  public Page getPageOnBegin(ActiveQuestionnaireAdministrationService service, Page currentPage) {
    Page beginPage = null;

    Questionnaire questionnaire = service.getQuestionnaire();

    List<Page> pages = questionnaire.getPages();

    for(Page page : pages) {
      if(NavigationStrategySupport.hasQuestionToBeAnswered(service, page)) {
        beginPage = page;
        break;
      }
    }

    return beginPage;
  }

  public Page getPageOnEnd(ActiveQuestionnaireAdministrationService service, Page currentPage) {
    Page endPage = currentPage;

    log.debug("searching for endPage");
    while(endPage != null) {
      log.debug("endPage = {}", endPage.getName());

      // Make active the answers to all questions to be answered on the current page.
      for(Question question : endPage.getQuestions()) {
        if(question.isToBeAnswered(service)) {
          if(question.hasSubQuestions()) {
            for(Question subQuestion : question.getQuestions()) {
              if(subQuestion.isToBeAnswered(service)) {
                service.setActiveAnswers(subQuestion, true);
              }
            }
          } else {
            service.setActiveAnswers(question, true);
          }
        }
      }

      if(NavigationStrategySupport.hasUnansweredQuestion(service, endPage)) {
        break;
      }

      endPage = getPageOnNext(service, endPage);
    }

    if(endPage == null) {
      endPage = getPageOnLast(service);
    }

    if(endPage != null) {
      log.debug("found endPage = {}", endPage.getName());
    }

    return endPage;
  }

  /**
   * Returns the current questionnaire participant's "resume page", which is the page that was displayed the last time
   * the participant pressed <code>Next</code>.
   * 
   * @param service service
   * @param participant current questionnaire participant
   * @return page on which to resume (page displayed the last time <code>Next</code> was pressed)
   */
  public Page getPageOnResume(ActiveQuestionnaireAdministrationService service, QuestionnaireParticipant participant) {
    Page resumePage = null;

    if(participant != null) {
      String resumePageName = participant.getResumePage();

      if(resumePageName != null) {
        Questionnaire questionnaire = service.getQuestionnaire();

        for(Page page : questionnaire.getPages()) {
          if(page.getName().equals(resumePageName)) {
            resumePage = page;
            break;
          }
        }
      }
    }

    return resumePage;
  }
}