package org.obiba.onyx.wicket.data;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * Data field is the component representation of {@link Data}.
 * @see DataConverter
 * @author Yannick Marcon
 *
 */
public class DataField extends Panel {

  private static final long serialVersionUID = 4522983933046975818L;

  private FormComponent field = null;
  
  /**
   * Constructor.
   * @param id
   * @param model value set is of type {@link Data}
   * @param dataType
   */
  @SuppressWarnings("serial")
  public DataField(String id, IModel model, final DataType dataType) {
    super(id);

    switch(dataType) {
    case TEXT:
    case DATA:
      field = new TextField("field", model, String.class) {
        @SuppressWarnings("unchecked")
        @Override
        public IConverter getConverter(Class type) {
          return new DataConverter(dataType);
        }
      };
      break;
    case BOOLEAN:
      field = new CheckBox("field", model) {
        @SuppressWarnings("unchecked")
        @Override
        public IConverter getConverter(Class type) {
          return new DataConverter(dataType);
        }
      };
      break;
    case DATE:
      field = new DateTextField("field", model) {
        @SuppressWarnings("unchecked")
        @Override
        public IConverter getConverter(Class type) {
          return new DataConverter(dataType);
        }
      };
      field.add(new DatePicker());
      break;
    case INTEGER:
      field = new TextField("field", model, Long.class) {
        @SuppressWarnings("unchecked")
        @Override
        public IConverter getConverter(Class type) {
          return new DataConverter(dataType);
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
      };
      break;
    }
    add(field);
  }
  
  /**
   * Set the model that identifies the underlying field in error messages.
   * @param labelModel
   */
  public void setLabel(IModel labelModel) {
    field.setLabel(labelModel);
  }
  
  /**
   * Add a behavior to underlying field.
   * @return this for chaining
   */
  @Override
  public Component add(IBehavior behavior) {
    field.add(behavior);
    return this;
  }
  
  /**
   * Set the underlying input field as required.
   * @param required
   * @return this for chaining
   */
  public Component setRequired(boolean required) {
    field.setRequired(required);
    return this;
  }
  
  /**
   * Get the underlying field feeback message.
   * @return
   */
  public FeedbackMessage getFieldFeedbackMessage() {
    return field.getFeedbackMessage();
  }
}
