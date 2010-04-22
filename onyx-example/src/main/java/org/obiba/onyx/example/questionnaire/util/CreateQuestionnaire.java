/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.example.questionnaire.util;

import java.io.File;
import java.util.Locale;

import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireCreator;

public class CreateQuestionnaire {

  public static void main(String args[]) {
    QuestionnaireCreator creator;

    try {
      if(args.length > 0) {
        File workingDir = new File(args[0]);
        creator = new QuestionnaireCreator(workingDir);
      } else {
        creator = new QuestionnaireCreator();
      }

      // Select the questionnaire you wish to create
      creator.createQuestionnaire(SelfAdminHealthQuestionnaireContentBuilder.buildQuestionnaire(), Locale.FRENCH, Locale.ENGLISH);
      creator.createQuestionnaire(AssistedHealthQuestionnaireContentBuilder.buildQuestionnaire(), Locale.FRENCH, Locale.ENGLISH);
      creator.createQuestionnaire(CIPreliminaryQuestionnaireContentBuilder.buildQuestionnaire(), Locale.ENGLISH);
      creator.createQuestionnaire(QuartzDemoQuestionnaireContentBuilder.buildQuestionnaire(), Locale.ENGLISH);
      creator.createQuestionnaire(VariableRenamingDemoQuestionnaireContentBuilder.buildQuestionnaire(), Locale.ENGLISH);
      creator.createQuestionnaire(ConclusionQuestionnaireContentBuilder.buildQuestionnaire(), Locale.ENGLISH);
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

}
