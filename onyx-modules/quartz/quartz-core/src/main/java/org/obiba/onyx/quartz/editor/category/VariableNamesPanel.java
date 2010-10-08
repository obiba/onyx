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
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;

@SuppressWarnings({ "serial", "unchecked" })
public class VariableNamesPanel extends Panel {

  private WebMarkupContainer itemsContainer;

  private final FeedbackPanel feedbackPanel;

  private final VariableNamesForm variableNamesForm;

  /**
   * Constructor of VariableNamesPanel <br/>
   * The given map is not updated. Call getNewMapData() to have updated data
   * @param id
   * @param map
   */
  public VariableNamesPanel(String id, Map<String, String> map) {
    super(id);

    add(CSSPackageResource.getHeaderContribution(VariableNamesPanel.class, "VariableNamesPanel.css"));

    // transform given map to a list
    List<String[]> listVariableName = new ArrayList<String[]>();
    for(Map.Entry<String, String> entry : map.entrySet()) {
      if(!StringUtils.isWhitespace(entry.getKey())) {
        listVariableName.add(new String[] { entry.getKey(), entry.getValue() });
      }
    }
    feedbackPanel = new FeedbackPanel("feedback");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);
    add(variableNamesForm = new VariableNamesForm("variableNamesForm", new ListModel<String[]>(listVariableName)));
  }

  /**
   * Return map of data filled by UI
   * @return
   */
  public Map<String, String> getNewMapData() {
    Map<String, String> map = new HashMap<String, String>();
    for(String[] strings : variableNamesForm.getModelObject()) {
      map.put(strings[0], strings[1]);
    }
    return map;
  }

  public class VariableNamesForm extends Form<List<String[]>> {

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
            form.error(new StringResourceModel("DuplicatedKeys", VariableNamesPanel.this, null).getObject());
          }
        }
      });
    }

    void addListView() {

      itemsContainer.addOrReplace(new ListView<String[]>("items", getModel()) {

        @Override
        protected void populateItem(ListItem<String[]> item) {
          final TextField<String> keyField = new TextField<String>("keyInput", new PropertyModel<String>(item.getModelObject(), "[0]"));
          keyField.add(new AjaxFormComponentUpdatingBehavior("onblur") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
              validateAndSubmit(target);
            }

          });

          TextField<String> valueField = new TextField<String>("valueInput", new PropertyModel<String>(item.getModelObject(), "[1]"));
          valueField.add(new AjaxFormComponentUpdatingBehavior("onblur") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
              validateAndSubmit(target);
            }
          });

          item.add(keyField);
          item.add(valueField);

          item.add(new RemoveAjaxLink("removeLink", item.getIndex()));
        }

      }.setReuseItems(true));

    }

    protected void validateAndSubmit(AjaxRequestTarget target) {
      VariableNamesForm.this.validateFormValidators();
      VariableNamesForm.this.delegateSubmit(null);
      target.addComponent(feedbackPanel);
    }

    private class AddAjaxButton extends AjaxButton {

      public AddAjaxButton(String id) {
        super(id, null, null);
        setDefaultFormProcessing(false);
      }

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        List<String[]> list = (List<String[]>) VariableNamesForm.this.getDefaultModelObject();
        list.add(new String[] { null, null });
        addListView();
        target.addComponent(itemsContainer);
        validateAndSubmit(target);
      }
    }

    private class RemoveAjaxLink extends AjaxLink<Void> {

      private int rowToDelete;

      public RemoveAjaxLink(String id, int rowToDelete) {
        super(id);
        this.rowToDelete = rowToDelete;
      }

      @Override
      public void onClick(AjaxRequestTarget target) {
        List<String[]> list = (List<String[]>) VariableNamesForm.this.getDefaultModelObject();
        list.remove(rowToDelete);
        addListView();
        target.addComponent(itemsContainer);
        validateAndSubmit(target);
      }
    }
  }
}
