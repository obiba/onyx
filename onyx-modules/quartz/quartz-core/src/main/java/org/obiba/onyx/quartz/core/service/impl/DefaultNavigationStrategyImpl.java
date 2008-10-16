/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.service.impl;

import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.service.INavigationStrategy;

/**
 * Default questionnaire navigation strategy.
 * 
 * @author cag-dspathis
 * 
 */
public class DefaultNavigationStrategyImpl implements INavigationStrategy {

  /**
   * Returns the earliest page of the questionnaire containing at least one (answerable) question that has not been
   * answered.
   * 
   * In effect, this method skips over pages with no questions to be answered.
   * 
   * @param service service
   * @return start page (first page with unanswered questions)
   */
  public Page getPageOnStart(ActiveQuestionnaireAdministrationService service) {
    Page startPage = null;

    Questionnaire questionnaire = service.getQuestionnaire();

    List<Page> pages = questionnaire.getPages();

    for(Page page : pages) {
      if(NavigationStrategySupport.hasUnansweredQuestion(service, page)) {
        startPage = page;
        break;
      }
    }

    return startPage;
  }

  /**
   * Returns the earliest page, after the current page, containing at least one (answerable) question that has not been
   * answered.
   * 
   * In effect, this method skips over pages with no questions to be answered.
   * 
   * @param service service
   * @param currentPage currently displayed page
   * @return next page (first page, after current page, with unanswered questions)
   */
  public Page getPageOnNext(ActiveQuestionnaireAdministrationService service, Page currentPage) {
    Page nextPage = null;

    Questionnaire questionnaire = service.getQuestionnaire();

    List<Page> pages = questionnaire.getPages();

    int currentPageIndex = pages.indexOf(currentPage);

    for(int i = currentPageIndex + 1; i < pages.size(); i++) {
      Page page = pages.get(i);

      if(NavigationStrategySupport.hasUnansweredQuestion(service, page)) {
        nextPage = page;
        break;
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
   * @return previous page (page displayed before the current page)
   */
  public Page getPageOnPrevious(ActiveQuestionnaireAdministrationService service, Page currentPage) {
    Page previousPage = null;
    
    Questionnaire questionnaire = service.getQuestionnaire();

    List<Page> pages = questionnaire.getPages();
    
    int startPageIndex = pages.indexOf(getPageOnStart(service));
    int currentPageIndex = pages.indexOf(currentPage);
    
    for (int i=startPageIndex; i<currentPageIndex; i++) {
      Page page = pages.get(i);
      
      if (!page.getName().equals(currentPage.getName())) {
        previousPage = getPageOnNext(service, previousPage);
      }
    }
    
    return previousPage;
  }

  /**
   * Finds the latest page of the questionnaire containing at least one answered question and either returns that page
   * if it contains additional unanswered questions, or the following page (see <code>getPageOnNext</code>) if it
   * does not.
   * 
   * If there are pages containing questions with inactive answers, then the earliest of those pages is returned
   * instead (the idea, being, that those questions need to be revisited first when resuming).
   * 
   * @param service service
   * @return
   */
  public Page getPageOnResume(ActiveQuestionnaireAdministrationService service) {
    Page resumePage = null;

    Questionnaire questionnaire = service.getQuestionnaire();

    List<Page> pages = questionnaire.getPages();

    int startPageIndex = pages.indexOf(getPageOnStart(service));
        
    // Look for first page containing questions with inactive answers.
    for (int i=startPageIndex; i<pages.size(); i++) {
      Page page = pages.get(i);
      
      if (NavigationStrategySupport.hasInactiveAnswer(service, page)) {
        resumePage = page;
        break;
      }
    }

    // If no such page exists, then look for the latest page of the questionnaire with active
    // answers.
    if (resumePage == null) {
      int lastPageIndex = pages.size() - 1;
      
      for (int i=lastPageIndex; i>=startPageIndex; i--) {
        Page page = pages.get(i);
        
        if (NavigationStrategySupport.hasActiveAnswer(service, page)) {
          resumePage = page;
          break;
        }
      }
      
      if (resumePage != null) {
        if (!NavigationStrategySupport.hasUnansweredQuestion(service, resumePage)) {
          resumePage = getPageOnNext(service, resumePage);
        }
      }
    }
    
    return resumePage;
  }
}
