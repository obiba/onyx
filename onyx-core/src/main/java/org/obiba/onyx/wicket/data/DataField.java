package org.obiba.onyx.wicket.data;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;
import org.obiba.onyx.util.data.DataType;

public class DataField extends Panel {

  private static final long serialVersionUID = 4522983933046975818L;

  private FormComponent field = null;
  
  @SuppressWarnings("serial")
  public DataField(String id, IModel model, final DataType dataType) {
    super(id);

    switch(dataType) {
    case TEXT:
    case DATA:
      field = new RequiredTextField("field", model, String.class) {
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
      field = new RequiredTextField("field", model, Long.class) {
        @SuppressWarnings("unchecked")
        @Override
        public IConverter getConverter(Class type) {
          return new DataConverter(dataType);
        }
      };
      break;
    case DECIMAL:
      field = new RequiredTextField("field", model, Double.class) {
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
  
  @Override
  public Component add(IBehavior behavior) {
    field.add(behavior);
    
    return this;
  }
}
