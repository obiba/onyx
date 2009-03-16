/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.integration.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;

/**
 * 
 */
public class TestQuestionnaire {

  private static final String N = "N";

  private static final String Y = "Y";

  private static final String OTHER = "OTHER";

  private static final String PNA = "PNA";

  private static final String DNK = "DNK";

  public static QuestionnaireBuilder buildQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("Test", "1.0");

    builder.withSection("S1").withPage("P1").withQuestion("Q_MULTIPLE", true).withCategories("1", "2", "3");
    builder.inQuestion("Q_MULTIPLE").withSharedCategory(PNA).setEscape(true).withSharedCategory(DNK).setEscape(true);

    builder.inPage("P1").withQuestion("ARRAY").withCategories(Y, N, PNA, DNK);
    builder.inQuestion("ARRAY").withQuestion("Q1");
    builder.inQuestion("ARRAY").withQuestion("Q2");

    return builder;
  }
}
