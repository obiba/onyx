package org.obiba.onyx.quartz.core.wicket.layout.impl.array;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

public class ArrayExample extends Panel {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("serial")
  public ArrayExample(String id) {
    super(id);

    List<IColumn> columns = new ArrayList<IColumn>();
    columns.add(new PropertyColumn(new Model(""), "id"));
    columns.add(new AbstractColumn(new Model("Category 1")) {

      public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        cellItem.add(new RadioPanel(componentId, rowModel));
      }

      @Override
      public String getCssClass() {
        return "category";
      }

    });
    columns.add(new AbstractColumn(new Model("Category 2")) {

      public void populateItem(Item cellItem, String componentId, IModel rowModel) {
        cellItem.add(new RadioPanel(componentId, rowModel));
      }

      @Override
      public String getCssClass() {
        return "category";
      }

    });

    add(new AbstractQuestionArray("array", new Model(), columns, new RowProvider(3)) {

      public Component getRows(String id, List<IColumn> columns, IDataProvider rows) {
        return new RadioRows(id, columns, rows);
      }

    });

  }

  private class RadioRows extends Fragment {

    private static final long serialVersionUID = 1L;

    /**
     * @param id
     * @param markupId
     * @param markupProvider
     */
    public RadioRows(String id, List<IColumn> columns, IDataProvider rows) {
      super(id, "radioRows", ArrayExample.this);
      add(new RadioGroupView(id, (List) columns, rows) {

        @Override
        protected RadioGroup newRadioGroup(String id, int index) {
          RadioGroup group = super.newRadioGroup(id, index);
          // add ajax call back on group
          group.add(new AjaxFormChoiceComponentUpdatingBehavior() {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {

            }

          });
          return group;
        }
      });
    }

  }

  private static class RadioPanel extends Panel implements IMarkupResourceStreamProvider {

    public RadioPanel(String id, IModel model) {
      super(id, model);
      add(new Radio("radio", model));
      add(new Label("label", new PropertyModel(model, "label")));
    }

    public IResourceStream getMarkupResourceStream(MarkupContainer container, Class containerClass) {
      return new StringResourceStream("<wicket:panel><input wicket:id=\"radio\" type=\"radio\"/><span wicket:id=\"label\"/></wicket:panel>");
    }
  }

}
