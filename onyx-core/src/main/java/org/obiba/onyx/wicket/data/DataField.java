/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.wicket.data;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.resource.ByteArrayResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converters.DateConverter;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.upload.FileUploadException;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.DateValidator;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.wicket.nanogong.NanoGongApplet;
import org.obiba.wicket.nanogong.NanoGongApplet.Format;
import org.obiba.wicket.nanogong.NanoGongApplet.Option;
import org.obiba.wicket.nanogong.NanoGongApplet.Rate;

/**
 * Data field is the component representation of {@link Data}.
 * @see DataConverter
 */
@SuppressWarnings("serial")
public class DataField extends Panel {

  private static final long serialVersionUID = 4522983933046975818L;

  //private static final Logger logger = LoggerFactory.getLogger(DataField.class);

  private static final int DATE_YEAR_MAXIMUM = 3000;

  private FieldFragment input;

  private boolean required = false;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  public DataField(String id, IModel<Data> model, final DataType dataType) {
    this(id, model, dataType, "", null, null);
  }

  /**
   * Creates a DataField component.
   * 
   * @param id Wicket Id the of the component.
   * @param model The model for this component.
   * @param dataType The type of the Data object.
   * @param unit The unit for the Data object.
   * @param size The width of the component on the UI.
   * @param rows The number rows displayed by the component (if rows == 1 an html input field is used, if rows > 1 an
   * html textarea will be displayed).
   */
  public DataField(String id, IModel<Data> model, DataType dataType, String unit, Integer size, Integer rows) {
    super(id, model);

    if(rows != null && rows > 1 && dataType.equals(DataType.TEXT)) {
      input = new TextAreaFragment("input", model, dataType, size, rows);
    } else {
      if(dataType == DataType.BOOLEAN) {
        input = new CheckboxFragment("input", model);
      } else {
        input = new InputFragment("input", model, dataType, size == null ? -1 : size);
      }
    }
    add(input);
    addUnitLabel(unit);

  }

  /**
   * Constructor.
   * @param id
   * @param model value set is of type {@link Data}
   * @param dataType
   * @param unit the representation of the unit for the value
   */
  public DataField(String id, IModel<Data> model, final DataType dataType, String unit) {
    this(id, model, dataType, unit, null, null);
  }

  /**
   * Select field from given choices.
   * @param id
   * @param model
   * @param dataType
   * @param choices
   * @param unit
   */
  public DataField(String id, IModel<Data> model, final DataType dataType, IModel<List<Data>> choices, String unit) {
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
  public DataField(String id, IModel<Data> model, final DataType dataType, IModel<List<Data>> choices, IChoiceRenderer<Data> renderer, String unit) {
    super(id);

    input = new SelectFragment("input", model, choices, renderer);
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
  public DataField(String id, IModel<Data> model, final DataType dataType, List<Data> choices, String unit) {
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
  public DataField(String id, IModel<Data> model, final DataType dataType, List<Data> choices, IChoiceRenderer<Data> renderer, String unit) {
    super(id);

    input = new SelectFragment("input", model, choices, renderer);
    add(input);

    addUnitLabel(unit);
  }

  /**
   * @param string
   * @param propertyModel
   * @param dataType
   * @param samplingRate
   * @param maxDuration
   */
  public DataField(String id, IModel<Data> model, DataType dataType, Rate samplingRate, int maxDuration) {
    super(id);
    add(input = new AudioRecorderFragment("input", model, samplingRate, maxDuration));
    addUnitLabel(null);
  }

  private void addUnitLabel(String unit) {
    add(new Label("unit", StringUtils.trimToEmpty(unit)));
  }

  /**
   * Set the model that identifies the underlying field in error messages.
   * @param labelModel
   */
  public void setLabel(IModel<String> labelModel) {
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
  public void setFieldModel(IModel<Data> model) {
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
  public Component add(IValidator<?> validator) {
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

    private static final long serialVersionUID = 1L;

    protected FormComponent field;

    public FieldFragment(String id, String markupId, MarkupContainer markupProvider) {
      super(id, markupId, markupProvider);
    }

    public FormComponent getField() {
      return field;
    }
  }

  private class TextAreaFragment extends FieldFragment {

    public TextAreaFragment(String id, IModel<Data> model, final DataType dataType, Integer columns, Integer rows) {
      super(id, "textAreaFragment", DataField.this);

      field = new TextArea<Data>("field", model) {

        @Override
        public IConverter getConverter(Class<?> type) {
          return new DataConverter(dataType, userSessionService);
        }

        @Override
        public boolean isRequired() {
          return DataField.this.isRequired();
        }
      };
      if(columns != null) {
        field.add(new AttributeAppender("cols", new Model<Integer>(columns), ""));
      }
      field.add(new AttributeAppender("rows", new Model<Integer>(rows), ""));

      add(field);
    }
  }

  private class InputFragment extends FieldFragment {

    @SuppressWarnings("incomplete-switch")
    public InputFragment(String id, IModel<Data> model, final DataType dataType, Integer size) {
      super(id, "inputFragment", DataField.this);

      switch(dataType) {
      case TEXT:
      case DATA:
        field = new TextField<Data>("field", model) {
          @Override
          public IConverter getConverter(Class<?> type) {
            return new DataConverter(dataType, userSessionService);
          }

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
        break;

      case DATE:
        // cannot use DateTextField because we have IModel<Data> and not IModel<Date>
        field = new DataDateTextField("field", model, userSessionService.getDatePattern()) {
          @Override
          public IConverter getConverter(Class<?> type) {
            return new DataConverter(dataType, userSessionService);
          }

          @Override
          public String getTextFormat() {
            return userSessionService.getDatePattern();
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

          @Override
          protected String getDatePattern() {
            return userSessionService.getDatePattern();
          }
        });
        break;

      case INTEGER:
        field = new TextField<Data>("field", model) {
          @Override
          public IConverter getConverter(Class<?> type) {
            return new DataConverter(dataType, userSessionService);
          }

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
        break;

      case DECIMAL:
        field = new TextField<Data>("field", model) {
          @Override
          public IConverter getConverter(Class<?> type) {
            return new DataConverter(dataType, userSessionService);
          }

          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
        break;
      }
      field.add(new AttributeAppender("size", new Model<String>(Integer.toString(size)), ""));
      add(field);
    }

    private class DataDateTextField extends TextField<Data> implements ITextFormatProvider {

      /**
       * The date pattern of the text field
       */
      private String datePattern = null;

      /**
       * The converter for the TextField
       */
      private IConverter converter = null;

      /**
       * Creates a new DateTextField bound with a specific <code>SimpleDateFormat</code> pattern.
       * 
       * @param id The id of the text field
       * @param model The model
       * @param datePattern A <code>SimpleDateFormat</code> pattern
       * 
       * @see org.apache.wicket.markup.html.form.TextField
       */
      public DataDateTextField(String id, IModel<Data> model, String datePattern) {
        super(id, model, Data.class);
        this.datePattern = datePattern;
        converter = new DateConverter() {

          /**
           * @see org.apache.wicket.util.convert.converters.DateConverter#getDateFormat(java.util.Locale)
           */
          @Override
          public DateFormat getDateFormat(Locale locale) {
            if(locale == null) {
              locale = Locale.getDefault();
            }
            return new SimpleDateFormat(DataDateTextField.this.datePattern, locale);
          }
        };
      }

      /**
       * Returns the default converter if created without pattern; otherwise it returns a pattern-specific converter.
       * 
       * @param type The type for which the convertor should work
       * 
       * @return A pattern-specific converter
       * 
       * @see org.apache.wicket.markup.html.form.TextField
       */
      @Override
      public IConverter getConverter(Class<?> type) {
        if(converter == null) {
          return super.getConverter(type);
        }
        return converter;
      }

      /**
       * Returns the date pattern.
       * 
       * @see org.apache.wicket.markup.html.form.AbstractTextComponent.ITextFormatProvider#getTextFormat()
       */
      @Override
      public String getTextFormat() {
        return datePattern;
      }

    }
  }

  private class CheckboxFragment extends FieldFragment {

    public CheckboxFragment(String id, final IModel<Data> model) {
      super(id, "checkboxFragment", DataField.this);
      // it seems that we cannot use PropertyModel because Data is not a real POJO
      field = new CheckBox("field", new Model<Boolean>() {
        @Override
        public void setObject(Boolean object) {
          model.setObject(new Data(DataType.BOOLEAN, object));
          modelChanged();
        }
      });
      add(field);
    }
  }

  private class SelectFragment extends FieldFragment {

    public SelectFragment(String id, IModel<Data> model, List<Data> choices, IChoiceRenderer<Data> renderer) {
      super(id, "selectFragment", DataField.this);

      if(renderer == null) {
        field = new DropDownChoice<Data>("select", model, choices) {
          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
      } else {
        field = new DropDownChoice<Data>("select", model, choices, renderer) {
          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
      }
      add(field);
    }

    public SelectFragment(String id, IModel<Data> model, IModel<List<Data>> choices, IChoiceRenderer<Data> renderer) {
      super(id, "selectFragment", DataField.this);

      if(renderer == null) {
        field = new DropDownChoice<Data>("select", model, choices) {
          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
      } else {
        field = new DropDownChoice<Data>("select", model, choices, renderer) {
          @Override
          public boolean isRequired() {
            return DataField.this.isRequired();
          }
        };
      }
      add(field);
    }
  }

  private class AudioRecorderFragment extends FieldFragment {

    public AudioRecorderFragment(String id, final IModel<Data> model, Rate samplingRate, int maxDuration) {
      super(id, "audioRecorderFragment", DataField.this);

      Map<Option, Object> options = new HashMap<NanoGongApplet.Option, Object>();
      options.put(Option.AudioFormat, Format.PCM);
      options.put(Option.SamplingRate, samplingRate);
      options.put(Option.MaxDuration, String.valueOf(maxDuration));
      options.put(Option.ShowSpeedButton, "false");
      options.put(Option.ShowSaveButton, "false");
      options.put(Option.ShowTime, "true");
      options.put(Option.Color, "#FFFFFF");

      if(model.getObject() != null) {
        CharSequence audioFileUrl = urlFor(new ResourceReference(AudioRecorderFragment.class, "audio.wav") {
          @Override
          protected Resource newResource() {
            return new WebResource() {
              @Override
              public IResourceStream getResourceStream() {
                return new ByteArrayResource("audio/x-wav", (byte[]) model.getObject().getValue()).getResourceStream();
              }
            };
          }
        });
        options.put(Option.SoundFileURL, audioFileUrl);
      }

      field = new NanoGongApplet("nanoGong", "140", "60", options) {
        @Override
        protected void onAudioData(FileUpload fileUpload) {
          model.setObject(new Data(DataType.DATA, fileUpload.getBytes()));
          for(AudioDataListener listener : listeners) {
            listener.onDataUploaded();
          }
        }

        @Override
        protected void onAudioDataProcessed(AjaxRequestTarget target) {
          for(AudioDataListener listener : listeners) {
            listener.onAudioDataProcessed(target);
          }
        }

        @Override
        protected void onFileUploadException(FileUploadException exception, Map<String, Object> exceptionModel) {
          for(AudioDataListener listener : listeners) {
            listener.onError(exception, exceptionModel);
          }
        }
      };
      add(field);

    }
  }

  private Set<AudioDataListener> listeners = new HashSet<AudioDataListener>();

  public void addListener(AudioDataListener dataListener) {
    listeners.add(dataListener);
  }

  public interface AudioDataListener extends EventListener, Serializable {

    void onDataUploaded();

    void onAudioDataProcessed(AjaxRequestTarget target);

    void onError(FileUploadException exception, Map<String, Object> exceptionModel);
  }

}
