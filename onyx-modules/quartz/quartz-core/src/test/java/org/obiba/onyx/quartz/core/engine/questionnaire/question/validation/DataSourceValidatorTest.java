/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question.validation;

import static org.easymock.EasyMock.createMock;

import java.util.Locale;

import junit.framework.Assert;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.spring.injection.annot.AnnotSpringInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.spring.test.SpringContextLocatorMock;
import org.apache.wicket.validation.Validatable;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.quartz.core.engine.questionnaire.condition.ComparisionOperator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.DataSourceBuilder;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class DataSourceValidatorTest {

  static final Logger log = LoggerFactory.getLogger(DataSourceValidatorTest.class);

  private ApplicationContextMock applicationContextMock;

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  @SuppressWarnings("unused")
  private Questionnaire questionnaire;

  @Before
  public void setUp() {
    activeQuestionnaireAdministrationServiceMock = createMock(ActiveQuestionnaireAdministrationService.class);

    applicationContextMock = new ApplicationContextMock();
    applicationContextMock.putBean("activeQuestionnaireAdministrationService", activeQuestionnaireAdministrationServiceMock);

    InjectorHolder.setInjector(new AnnotSpringInjector(new SpringContextLocatorMock(applicationContextMock)));

    questionnaire = createQuestionnaire();

  }

  @Test
  public void testEqualDataSource() {
    DataSourceValidator validator = new DataSourceValidator(ComparisionOperator.eq, DataSourceBuilder.createFixedSource(DataBuilder.buildInteger(1)).getDataSource());
    Validatable validatable = new Validatable(DataBuilder.buildInteger(1));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(2));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testDifferentDataSource() {
    DataSourceValidator validator = new DataSourceValidator(ComparisionOperator.ne, DataSourceBuilder.createFixedSource(DataBuilder.buildInteger(1)).getDataSource());
    Validatable validatable = new Validatable(DataBuilder.buildInteger(0));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(1));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testNullDataSource() {
    DataSourceValidator validator = new DataSourceValidator(ComparisionOperator.eq, DataSourceBuilder.createFixedSource(null).getDataSource());
    Validatable validatable = new Validatable(DataBuilder.buildInteger(0));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testLowerDataSource() {
    DataSourceValidator validator = new DataSourceValidator(ComparisionOperator.lt, DataSourceBuilder.createFixedSource(DataBuilder.buildInteger(1)).getDataSource());
    Validatable validatable = new Validatable(DataBuilder.buildInteger(0));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(1));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(2));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testLowerEqualDataSource() {
    DataSourceValidator validator = new DataSourceValidator(ComparisionOperator.le, DataSourceBuilder.createFixedSource(DataBuilder.buildInteger(1)).getDataSource());
    Validatable validatable = new Validatable(DataBuilder.buildInteger(0));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(1));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(2));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testGreaterDataSource() {
    DataSourceValidator validator = new DataSourceValidator(ComparisionOperator.gt, DataSourceBuilder.createFixedSource(DataBuilder.buildInteger(1)).getDataSource());
    Validatable validatable = new Validatable(DataBuilder.buildInteger(2));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(1));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(0));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testGreaterEqualDataSource() {
    DataSourceValidator validator = new DataSourceValidator(ComparisionOperator.ge, DataSourceBuilder.createFixedSource(DataBuilder.buildInteger(1)).getDataSource());
    Validatable validatable = new Validatable(DataBuilder.buildInteger(2));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(1));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(0));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  public Questionnaire createQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("HealthQuestionnaire", "1.0");

    builder.withSection("SB").withSection("GENDER").withPage("P1").withQuestion("Q1").withCategories("1", "2", "3");
    builder.withSection("S1").withPage("P2").withQuestion("Q2").withCategory("1").withOpenAnswerDefinition("OPEN_INT", DataType.INTEGER);
    builder.inQuestion("Q2").withCategory("2").withOpenAnswerDefinition("OPEN_TEXT", DataType.TEXT);
    builder.inQuestion("Q2").withCategory("3").withOpenAnswerDefinition("OPEN_DATE", DataType.DATE);
    builder.inQuestion("Q2").withCategory("4").withOpenAnswerDefinition("OPEN_TEXT_DEFAULT_VALUES", DataType.TEXT).setDefaultData("a", "b", "c");

    Questionnaire q = builder.getQuestionnaire();
    q.addLocale(Locale.FRENCH);
    q.addLocale(Locale.ENGLISH);

    return q;
  }

}
