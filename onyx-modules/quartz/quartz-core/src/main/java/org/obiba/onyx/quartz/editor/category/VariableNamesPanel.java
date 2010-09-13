package org.obiba.onyx.quartz.editor.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

@SuppressWarnings("serial")
public class VariableNamesPanel extends Panel {

  protected WebMarkupContainer itemsContainer;

  protected FeedbackPanel feedbackPanel;

  private VariableNamesForm variableNamesForm;

  public VariableNamesPanel(String id, Map<String, String> map) {
    super(id);
    List<String[]> listVariableName = new ArrayList<String[]>();
    for(Map.Entry<String, String> entry : map.entrySet()) {
      listVariableName.add(new String[] { entry.getKey(), entry.getValue() });
    }
    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);
    add(variableNamesForm = new VariableNamesForm("variableNamesForm", new ListModel<String[]>(listVariableName)));
  }

  public Map<String, String> getMap() {
    Map<String, String> map = new HashMap<String, String>();
    for(String[] strings : variableNamesForm.getModelObject()) {
      map.put(strings[0], strings[1]);
    }
    return map;
  }

  public class VariableNamesForm extends Form<List<String[]>> {

    private static final String ONBLUR = "onblur";

    public VariableNamesForm(String id, ListModel<String[]> listModel) {
      super(id, listModel);
      itemsContainer = new WebMarkupContainer("itemsContainer");
      itemsContainer.setOutputMarkupId(true);

      addListView();

      itemsContainer.add(new AddAjaxButton("addButton"));
      add(itemsContainer);
      add(new AbstractFormValidator() {

        @Override
        public FormComponent<?>[] getDependentFormComponents() {
          return null;
        }

        @Override
        public void validate(Form<?> form) {
          Set<String> set = new HashSet<String>();
          for(String[] strings : getModel().getObject()) {
            set.add(strings[0]);
          }
          if(getModel().getObject().size() != set.size()) {
            form.error(new ResourceModel("DuplicatedKeys").getObject());
          }
        }
      });
    }

    void addListView() {

      itemsContainer.addOrReplace(new ListView<String[]>("items", getModel()) {
        private static final long serialVersionUID = 0L;

        @Override
        protected void populateItem(ListItem<String[]> item) {
          final TextField<String> keyField = new TextField<String>("keyInput", new PropertyModel<String>(item.getModelObject(), "[0]"));
          keyField.add(new AjaxFormComponentUpdatingBehavior(ONBLUR) {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
              validateAndSubmit(target);
            }

          });

          TextField<String> valueField = new TextField<String>("valueInput", new PropertyModel<String>(item.getModelObject(), "[1]"));
          valueField.add(new AjaxFormComponentUpdatingBehavior(ONBLUR) {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
              validateAndSubmit(target);
            }
          });

          item.add(keyField);
          item.add(valueField);

          RemoveAjaxButton removeAjaxButton = new RemoveAjaxButton("removeButton", item.getIndex());
          item.add(removeAjaxButton);
        }

        private void validateAndSubmit(AjaxRequestTarget target) {
          VariableNamesForm.this.validateFormValidators();
          VariableNamesForm.this.delegateSubmit(null);
          target.addComponent(feedbackPanel);
        }

      }.setReuseItems(true));

    }

    private class AddAjaxButton extends AjaxButton {

      private static final long serialVersionUID = -4238963161016807826L;

      public AddAjaxButton(String id) {
        super(id, null, null);
        setDefaultFormProcessing(false);
        add(new Image("addImg"));
        add(new Label("addLabel", "+"));
      }

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        List<String[]> list = (List<String[]>) VariableNamesForm.this.getDefaultModelObject();
        boolean notEmptys = Iterables.all(list, new Predicate<String[]>() {

          @Override
          public boolean apply(String[] input) {
            return StringUtils.isNotBlank(input[0]);
          }
        });
        if(notEmptys) {
          list.add(new String[] { "", "" });
          addListView();
          target.addComponent(itemsContainer);
        }
      }
    }

    private class RemoveAjaxButton extends AjaxButton {

      private static final long serialVersionUID = -4238963161016807826L;

      private int rowToDelete;

      public RemoveAjaxButton(String id, int rowToDelete) {
        super(id, null, null);
        this.rowToDelete = rowToDelete;
        setDefaultFormProcessing(false);
        add(new Image("removeImg"));
        add(new Label("removeLabel", "x"));
      }

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        List<String[]> list = (List<String[]>) VariableNamesForm.this.getDefaultModelObject();
        list.remove(rowToDelete);
        addListView();
        target.addComponent(itemsContainer);
      }
    }
  }
}
