/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.Validatable;
import org.apache.wicket.validation.validator.NumberValidator;
import org.junit.Test;
import org.junit.Assert;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.data.DataField;
import org.obiba.onyx.wicket.test.TestForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class InvalidFormFieldBehaviorTest {

  private static final Logger log = LoggerFactory.getLogger(InvalidFormFieldBehaviorTest.class);

  @Test
  public void testBehavior() {
    WicketTester tester = new WicketTester(DummyHomePage.class);

    tester.startPanel(new TestPanelSource() {

      public Panel getTestPanel(String panelId) {
        return new TestForm(panelId) {

          @Override
          public Component populateContent(String id) {
            DataField panel = new DataField(id, new Model(DataBuilder.buildInteger(10)), DataType.INTEGER);
            panel.add(new NumberValidator.RangeValidator(0, 2) {
              @Override
              protected void onValidate(IValidatable validatable) {
                Data data = (Data) validatable.getValue();
                Long value = data.getValue();
                Validatable intValidatable = new Validatable(value);
                super.onValidate(intValidatable);
                if(intValidatable.getErrors() != null) {
                  for(Object error : intValidatable.getErrors()) {
                    validatable.error((IValidationError) error);
                  }
                }
                log.info("validatable.isValid={}", validatable.isValid());

              }
            });
            panel.add(new InvalidFormFieldBehavior());
            return panel;
          }

        };

      }

    });

    // tester.dumpPage();

    TagTester tagTester = tester.getTagByWicketId("field");
    Assert.assertEquals("10", tagTester.getAttribute("value"));

    FormTester formTester = tester.newFormTester("panel:form");
    formTester.setValue("panel:form:content:input:field", "10");
    formTester.submit();

    // tester.dumpPage();

    tagTester = tester.getTagByWicketId("field");
    Assert.assertTrue(tagTester.getAttributeContains("class", "field-invalid"));
  }
}
