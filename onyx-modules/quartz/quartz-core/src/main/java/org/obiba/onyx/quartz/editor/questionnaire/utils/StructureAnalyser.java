/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire.utils;

import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check that questions have a type supported by the editor
 */
public class StructureAnalyser {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final Questionnaire questionnaire;

  public static StructureAnalyser getInstance(Questionnaire questionnaire) {
    return new StructureAnalyser(questionnaire);
  }

  private StructureAnalyser(Questionnaire questionnaire) {
    this.questionnaire = questionnaire;
    if(questionnaire.getQuestionnaireCache() == null) {
      QuestionnaireFinder.getInstance(questionnaire).buildQuestionnaireCache();
    }
  }

  public void analyze() {
    Map<Question, String> unsupportedTypes = new HashMap<Question, String>();
    for(Page page : questionnaire.getQuestionnaireCache().getPageCache().values()) {
      for(Question question : page.getQuestions()) {
        try {
          if(question.getType() == null) unsupportedTypes.put(question, "Unknown type");
        } catch(Exception e) {
          logger.debug("Cannot detect question type", e);
          unsupportedTypes.put(question, e.getMessage());
        }
      }
    }
    if(unsupportedTypes.size() > 0) {
      throw new StructureAnalyserException("Unsupported questions types: " + unsupportedTypes);
    }
  }

}
