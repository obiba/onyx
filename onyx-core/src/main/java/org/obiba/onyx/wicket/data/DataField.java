/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.wicket.data;

import java.util.Calendar;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.DateValidator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * Data field is the component representation of {@link Data}.
 * @see DataConverter
 */
public class DataField extends Panel {

  private static final long serialVersionUID = 4522983933046975818L;

  private static final int DATE_YEAR_MAXIMUM = 3000;

  private FieldFragment input;

  private boolean required = false;

  public DataField(String id, IModel model, final DataType dataType) {
    this(id, model, dataType, "");
  }

  /**
   * Constructor.
   * @param id
   * @param model value set is of type {@link Data}
   * @param dataType
   * @param unit the representation of the unit for the value
   */
  public DataField(String id, IModel<Data> model, final DataType dataType, String unit) {
    super(id, model);

    input = new InputFragment("input", model, dataType);
    add(input);

    addUnitLabel(unit);
  }

  /**
   * Select field from given choices.
   * @param id
   * @param model
   * @param dataType
   * @param choices
   * @param unit
   */
  public DataField(String id, IModel model, final DataType dataType, IModel choices, String unit) {
    this(id, model, dataType, choices, null, unit);
  }

  /**
   * Select field from given choices.
   * @param id
   * @param model
   * @param dataType
   * @param choices
   * @param renderer
   * @param unit
   */
  public DataField(String id, IModel model, final DataType dataType, IModel choices, IChoiceRenderer renderer, String unit) {
    super(id);

    input = new SelectFragment("input", model, dataType, choices, renderer);
    add(input);

    addUnitLabel(unit);
  }

  /**
   * Select field from given choices.
   * @param id
   * @param model
   * @param dataType
   * @param choices
   * @param unit
   */
  @SuppressWarnings("unchecked")
  public DataField(String id, IModel model, final DataType dataType, List choices, String unit) {
    this(id, model, dataType, choices, null, unit);
  }

  /**
   * Select field from given choices.
   * @param id
   * @param model
   * @param dataType
   * @param choices
   * @param renderer
   * @param unit
   */
  @SuppressWarnings("unchecked")
  public DataField(String id, IModel model, final DataType dataType, List choices, IChoiceRenderer renderer, String unit) {
    super(id);

    input = new SelectFragment("input", model, dataType, choices, renderer);
    add(input);

    addUnitLabel(unit);
  }

  private void addUnitLabel(String unit) {
    add(new Label("unit", (unit == null ? "" : unit)));
  }

  /**
   * Set the model that identifies the underlying field in error messages.
   * @param labelModel
   */
  public void setLabel(IModel labelModel) {
    input.getField().setLabel(labelModel);
  }

  /**
   * Modify underlying field enability.
   * @param enabled
   */
  public void setFieldEnabled(boolean enabled) {
    input.getField().setEnabled(enabled);
  }

  /**
   * Get for underlying field if it is enabled.
   * @return
   */
  public boolean isFieldEnabled() {
    return input.getField().isEnabled();
  }

  /**
   * Set the model of the underlying field.
   * @param data
   */
  public void setFieldModel(IModel model) {
    input.getField().setModel(model);
  }

  /**
   * Set the model object of the underlying field.
   * @param data
   */
  public void setFieldModelObject(Data data) {
    input.getField().setModelObject(data);
  }

  /**
   * Check if underlying field has error message.
   * @return
   */
  public boolean hasFieldErrorMessage() {
    return input.getField().hasErrorMessage();
  }

  /**
   * Focus request on the inner input field.
   * @param target
   */
  public void focusField(AjaxRequestTarget target) {
    target.focusComponent(input.getField());
  }

  /**
   * Add a behavior to underlying field.
   * @return this for chaining
   */
  // @Override
  public Component add(IBehavior behavior) {
    input.getField().add(behavior);
    return this;
  }

  /**
   * Add a validator to the underlying field.
   * 
   * @param validator the validator
   * @return this for chaining
   */
  public Component add(IValidator validator) {
    input.getField().add(validator);
    return this;
  }

  /**
   * Set the underlying input field as required.
   * @param required
   * @return this for chaining
   */
  public Component setRequired(boolean required) {
    this.required = required;
    return this;
  }

  public boolean isRequired() {
    return this.required;
  }

  /**
   * Get the underlying field feeback message.
   * @return
   */
  public FeedbackMessage getFieldFeedbackMessage() {
    return input.getField().getFeedbackMessage();
  }

  /**
   * Get the underlying field component.
   * @return
   */
  public FormComponent getField() {
    return input.getField();
  }

  private abstract class FieldFragment extends Fragment {

    protected FormComponent field = null;

    public FieldFragment(String id, String markupId, MarkupContainer markupProvider) {
      super(id, markupId, markupProvider);

    }

    public FormComponent getField() {
      return field;
    }
  }

  private class InputFragment extends FieldFragment {

    private static final long serialVersionUID = 7003783791888047073L;

    @SuppressWarnings("serial")
    public InputFragment(String id, IModel model, final DataType dataType) {
      super(id, "inputFragment", DataField.this);

      switch(dataType) {
      case TEXT:
      case DATA:
        field = new TextField("field", model, String.class) {
          @SuppressWarnings("unchecked")
          @Override
          public IConverter getConverter(Class type) {
            return new DataConverter(dataType);
          }

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
        break;
      case BOOLEAN:
        field = new CheckBox("field", model) {
          // @SuppressWarnings("unchecked")
          // @Override
          // public IConverter getConverter(Class type) {
          // return new DataConverter(dataType);
          // }

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
        field.add(new AttributeModifier("type", new Model("checkbox")));
        break;
      case DATE:
        field = new DateTextField("field", model) {
          @SuppressWarnings("unchecked")
          @Override
          public IConverter getConverter(Class type) {
            return new DataConverter(dataType);
          }

          @Override
          public String getTextFormat() {
            return DataConverter.DATE_FORMAT;
          }

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, DATE_YEAR_MAXIMUM);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        field.add(new DataValidator(DateValidator.maximum(cal.getTime()), DataType.DATE));
        field.add(new DatePicker() {
          @Override
          protected boolean enableMonthYearSelection() {
            return true;
          }
        });
        break;
      case INTEGER:
        field = new TextField("field", model, Long.class) {
          @SuppressWarnings("unchecked")
          @Override
          public IConverter getConverter(Class type) {
            return new DataConverter(dataType);
          }

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
        break;
      case DECIMAL:
        field = new TextField("field", model, Double.class) {
          @SuppressWarnings("unchecked")
          @Override
          public IConverter getConverter(Class type) {
            return new DataConverter(dataType);
          }

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
        break;
      }
      add(field);
    }
  }

  private class SelectFragment extends FieldFragment {

    private static final long serialVersionUID = -6926320986227794949L;

    @SuppressWarnings("unchecked")
    public SelectFragment(String id, IModel model, final DataType dataType, List choices, IChoiceRenderer renderer) {
      super(id, "selectFragment", DataField.this);

      if(renderer == null) {
        field = new DropDownChoice("select", model, choices) {
          private static final long serialVersionUID = 1L;

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }

        };
      } else {
        field = new DropDownChoice("select", model, choices, renderer) {
          private static final long serialVersionUID = 1L;

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }

        };
      }
      add(field);
    }

    public SelectFragment(String id, IModel model, final DataType dataType, IModel choices, IChoiceRenderer renderer) {
      super(id, "selectFragment", DataField.this);

      if(renderer == null) {
        field = new DropDownChoice("select", model, choices) {
          private static final long serialVersionUID = 1L;

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }

        };
      } else {
        field = new DropDownChoice("select", model, choices, renderer) {
          private static final long serialVersionUID = 1L;

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }

        };
      }
      add(field);
    }
  }

}
