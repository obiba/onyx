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

import org.apache.wicket.validation.validator.NumberValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.util.data.DataType;

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

    builder.withSection("S1").withPage("P1").withQuestion("OPEN_QUESTION").withCategory("OPEN").withOpenAnswerDefinition("OPEN_MULTIPLE", DataType.TEXT);
    builder.inOpenAnswerDefinition("OPEN_MULTIPLE").withOpenAnswerDefinition("CHOICE1", DataType.TEXT).setDefaultData("A", "B", "C").setRequired(true);
    builder.inOpenAnswerDefinition("OPEN_MULTIPLE").withOpenAnswerDefinition("CHOICE2", DataType.TEXT).setDefaultData("X", "Y", "Z").setRequired(true);
    builder.inQuestion("OPEN_QUESTION").withSharedCategory(OTHER).withOpenAnswerDefinition("NUMERIC", DataType.INTEGER).addValidator(NumberValidator.range(1, 4)).setSize(2);

    builder.inPage("P1").withQuestion("Q_MULTIPLE", true).withCategories("1", "2", "3");
    builder.inQuestion("Q_MULTIPLE").withSharedCategory(PNA).setEscape(true).withSharedCategory(DNK).setEscape(true);

    builder.inPage("P1").withQuestion("ARRAY").withCategories(Y, N).withSharedCategories(PNA, DNK);
    builder.inQuestion("ARRAY").withQuestion("Q1");
    builder.inQuestion("ARRAY").withQuestion("Q2");

    builder.inPage("P1").withQuestion("Q_INNER_CONDITION").setCondition("Q_MULTIPLE", "PNA");
    builder.inQuestion("Q_INNER_CONDITION").withCategories("1", "2", "3");

    builder.inPage("P1").addTimestamp();

    return builder;
  }
}
