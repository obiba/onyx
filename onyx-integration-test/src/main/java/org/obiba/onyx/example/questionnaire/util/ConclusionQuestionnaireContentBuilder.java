/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.example.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;

public class ConclusionQuestionnaireContentBuilder {

  public static QuestionnaireBuilder buildQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("ConclusionQuestionnaire", "1.0");

    builder.withSection("CHECKLIST").withPage("1").withQuestion("PARTICIPANT_OFFERED_DOCUMENT", true).setAnswerCount(2, 2);
    builder.inQuestion("PARTICIPANT_OFFERED_DOCUMENT").withCategory("CONSENT_FORM").setExportName("1");
    builder.inQuestion("PARTICIPANT_OFFERED_DOCUMENT").withCategory("PHYSICAL_MEASURES_REPORT").setExportName("2");

    return builder;
  }
}
