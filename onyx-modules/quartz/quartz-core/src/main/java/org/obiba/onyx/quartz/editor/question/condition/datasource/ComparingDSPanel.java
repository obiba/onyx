/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question.condition.datasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class ComparingDSPanel extends Panel {

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private Fragment valuesFragment;

  private SimpleFormComponentLabel valueLabel;

  public ComparingDSPanel(String id, IModel<ComparingDS> model, final ModalWindow dataSourceWindow) {
    super(id, model);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    final Form<ComparingDS> form = new Form<ComparingDS>("form", model);
    add(form);

    final DropDownChoice<ComparisonOperator> operatorChoice = new DropDownChoice<ComparisonOperator>("operator", new PropertyModel<ComparisonOperator>(form.getModel(), "operator"), Arrays.asList(ComparisonOperator.values()), new IChoiceRenderer<ComparisonOperator>() {
      @Override
      public Object getDisplayValue(ComparisonOperator element) {
        return new StringResourceModel("Operator." + element.name(), ComparingDSPanel.this, null).getString();
      }

      @Override
      public String getIdValue(ComparisonOperator element, int index) {
        return element.name();
      }
    });
    operatorChoice.setLabel(new ResourceModel("Operator"));
    operatorChoice.setRequired(true);
    operatorChoice.setNullValid(false);
    form.add(operatorChoice);
    form.add(new SimpleFormComponentLabel("operatorLabel", operatorChoice));

    List<String> types = new ArrayList<String>();
    for(DataType dataType : DataType.values()) {
      types.add(dataType.name());
    }
    types.add(ComparingDS.GENDER_TYPE);

    final DropDownChoice<String> typeChoice = new DropDownChoice<String>("type", new PropertyModel<String>(form.getModel(), "type"), types, new IChoiceRenderer<String>() {
      @Override
      public Object getDisplayValue(String element) {
        return WordUtils.capitalizeFully(element);
      }

      @Override
      public String getIdValue(String element, int index) {
        return element;
      }
    });
    typeChoice.setLabel(new ResourceModel("Type"));
    typeChoice.setRequired(true);
    typeChoice.setNullValid(false);
    form.add(typeChoice);
    form.add(new SimpleFormComponentLabel("typeLabel", typeChoice));

    final WebMarkupContainer valueContainer = new WebMarkupContainer("valueContainer");
    valueContainer.setOutputMarkupId(true);
    form.add(valueContainer);

    valueContainer.add(valuesFragment = new InputTextFragment("value", form.getModel()));
    valueContainer.add(valueLabel = new SimpleFormComponentLabel("valueLabel", ((FormComponentFragment) valuesFragment).getFormComponent()));

    typeChoice.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        String type = typeChoice.getModelObject();
        ComparingDS comparingDS = form.getModel().getObject();
        Fragment newFragment = null;
        if(StringUtils.equals(type, ComparingDS.GENDER_TYPE)) {
          newFragment = new GenderChoiceFragment("value", form.getModel());
          comparingDS.setValue(null);
        } else if(StringUtils.equals(type, DataType.BOOLEAN.name())) {
          newFragment = new BooleanChoiceFragment("value", form.getModel());
          comparingDS.setGender(null);
        } else {
          newFragment = new InputTextFragment("value", form.getModel());
          comparingDS.setGender(null);
        }
        valuesFragment.replaceWith(newFragment);
        valuesFragment = newFragment;

        SimpleFormComponentLabel newLabel = new SimpleFormComponentLabel("valueLabel", ((FormComponentFragment) valuesFragment).getFormComponent());
        valueLabel.replaceWith(newLabel);
        valueLabel = newLabel;

        target.addComponent(valueContainer);
      }
    });

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        onSave(target, form.getModelObject());
        dataSourceWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form2) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

    form.add(new AjaxButton("cancel", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        dataSourceWindow.close(target);
      }
    }.setDefaultFormProcessing(false));
  }

  /**
   * 
   * @param target
   * @param genderDataSource
   */
  public abstract void onSave(AjaxRequestTarget target, ComparingDS genderDataSource);

  public interface FormComponentFragment {
    FormComponent<?> getFormComponent();
  }

  public class GenderChoiceFragment extends Fragment implements FormComponentFragment {

    private final DropDownChoice<Gender> select;

    public GenderChoiceFragment(String id, final IModel<ComparingDS> model) {
      super(id, "selectFragment", ComparingDSPanel.this, model);

      select = new DropDownChoice<Gender>("select", new PropertyModel<Gender>(model, "gender"), Arrays.asList(Gender.values()), new IChoiceRenderer<Gender>() {
        @Override
        public Object getDisplayValue(Gender element) {
          return new StringResourceModel("Gender." + element.name(), ComparingDSPanel.this, null).getString();
        }

        @Override
        public String getIdValue(Gender element, int index) {
          return element.name();
        }
      });
      select.setLabel(new ResourceModel("Value"));
      select.setNullValid(false);
      select.setRequired(true);
      select.setEnabled(StringUtils.isNotBlank(model.getObject().getType()));
      add(select);
    }

    @Override
    public FormComponent<?> getFormComponent() {
      return select;
    }

  }

  public class BooleanChoiceFragment extends Fragment implements FormComponentFragment {

    private final DropDownChoice<String> select;

    public BooleanChoiceFragment(String id, final IModel<ComparingDS> model) {
      super(id, "selectFragment", ComparingDSPanel.this, model);

      select = new DropDownChoice<String>("select", new PropertyModel<String>(model, "value"), Arrays.asList("true", "false"), new IChoiceRenderer<String>() {
        @Override
        public Object getDisplayValue(String element) {
          return WordUtils.capitalizeFully(element);
        }

        @Override
        public String getIdValue(String element, int index) {
          return element;
        }
      });
      select.setLabel(new ResourceModel("Value"));
      select.setNullValid(false);
      select.setRequired(true);
      select.setEnabled(StringUtils.isNotBlank(model.getObject().getType()));
      add(select);
    }

    @Override
    public FormComponent<?> getFormComponent() {
      return select;
    }

  }

  public class InputTextFragment extends Fragment implements FormComponentFragment {

    private final TextField<String> value;

    public InputTextFragment(String id, final IModel<ComparingDS> model) {
      super(id, "inputFragment", ComparingDSPanel.this, model);

      value = new TextField<String>("input", new PropertyModel<String>(model, "value"));
      value.setLabel(new ResourceModel("Value"));
      value.setRequired(true);
      value.setEnabled(StringUtils.isNotBlank(model.getObject().getType()));
      add(value);
    }

    @Override
    public FormComponent<?> getFormComponent() {
      return value;
    }
  }

}
