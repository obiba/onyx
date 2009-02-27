/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.validation;

import junit.framework.Assert;

import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.junit.Test;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.FixedDataSource;
import org.obiba.onyx.core.data.IDataSource;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.OpenAnswerDefinitionValidatorFactory;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class ComparingDataSourceValidatorTest {

  static final Logger log = LoggerFactory.getLogger(ComparingDataSourceValidatorTest.class);

  @Test
  public void testEqualDataSource() {
    IValidator validator = createComparingDataSourceValidator(ComparisonOperator.eq, new FixedDataSource(DataBuilder.buildInteger(1)));
    Validatable validatable = new Validatable(DataBuilder.buildInteger(1));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(2));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testDifferentDataSource() {
    IValidator validator = createComparingDataSourceValidator(ComparisonOperator.ne, new FixedDataSource(DataBuilder.buildInteger(1)));
    Validatable validatable = new Validatable(DataBuilder.buildInteger(0));
    validator.validate(validatable);
    Assert.assertEquals(0, validatable.getErrors().size());

    validatable = new Validatable(DataBuilder.buildInteger(1));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testNullDataSource() {
    IValidator validator = createComparingDataSourceValidator(ComparisonOperator.eq, new FixedDataSource(null));
    Validatable validatable = new Validatable(DataBuilder.buildInteger(0));
    validator.validate(validatable);
    Assert.assertEquals(1, validatable.getErrors().size());
  }

  @Test
  public void testLowerDataSource() {
    IValidator validator = createComparingDataSourceValidator(ComparisonOperator.lt, new FixedDataSource(DataBuilder.buildInteger(1)));
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
    IValidator validator = createComparingDataSourceValidator(ComparisonOperator.le, new FixedDataSource(DataBuilder.buildInteger(1)));
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
    IValidator validator = createComparingDataSourceValidator(ComparisonOperator.gt, new FixedDataSource(DataBuilder.buildInteger(1)));
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
    IValidator validator = createComparingDataSourceValidator(ComparisonOperator.ge, new FixedDataSource(DataBuilder.buildInteger(1)));
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

  @SuppressWarnings("serial")
  public IValidator createComparingDataSourceValidator(ComparisonOperator operator, IDataSource right) {
    IModel model = new IModel() {

      OpenAnswerDefinition open;

      public Object getObject() {
        return open;
      }

      public void setObject(Object object) {
        this.open = (OpenAnswerDefinition) object;
      }

      public void detach() {
        this.open = null;
      }

    };
    OpenAnswerDefinition object = new OpenAnswerDefinition("OPEN", DataType.INTEGER);
    object.addValidationDataSource(new ComparingDataSource(null, operator, right));
    model.setObject(object);
    IValidator validator = OpenAnswerDefinitionValidatorFactory.getValidators(model, new Participant()).get(0);
    Assert.assertNotNull(validator);

    return validator;
  }

}
