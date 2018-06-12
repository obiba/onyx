/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.test;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireCreator;

/**
 * 
 */
public class CreateTestQuestionnaire {

  private static File bundleRootDirectory = new File("target", "questionnaires");

  private static File bundleSourceDirectory = new File("src" + File.separatorChar + "test" + File.separatorChar + "resources" + File.separatorChar + "questionnaires");

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    QuestionnaireCreator creator = new QuestionnaireCreator(bundleRootDirectory, bundleSourceDirectory);

    // Select the questionnaire you wish to create
    creator.createQuestionnaire(HealthQuestionnaireTest.buildQuestionnaire(), Locale.ENGLISH);
    creator.createQuestionnaire(LargeQuestionnaireTest.buildQuestionnaire(), Locale.ENGLISH);

  }

}
