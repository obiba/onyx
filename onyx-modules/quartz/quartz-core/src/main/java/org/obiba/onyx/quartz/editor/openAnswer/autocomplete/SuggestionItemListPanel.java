/**
 * ****************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * <p/>
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ****************************************************************************
 */
package org.obiba.onyx.quartz.editor.openAnswer.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinitionSuggestion;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.wicket.markup.html.table.EntityListTablePanel;
import org.obiba.wicket.markup.html.table.IColumnProvider;

import static org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.CloseButtonCallback;
import static org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnYesCallback;

/**
 *
 */
public class SuggestionItemListPanel extends Panel {

  private static final int ITEMS_PER_PAGE = 20;
  private static final int ITEM_WINDOW_HEIGHT = 300;
  private static final int ITEM_WINDOW_WIDTH = 850;

  private EntityListTablePanel<String> itemList;

  private final IModel<Questionnaire> questionnaireModel;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final ModalWindow itemWindow;

  private ConfirmationDialog deleteConfirm;

  private final OpenAnswerDefinitionSuggestion openAnswerSuggestion;

  public SuggestionItemListPanel(String id, IModel<OpenAnswerDefinition> model,
      IModel<Questionnaire> questionnaireModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);

    this.questionnaireModel = questionnaireModel;
    this.feedbackPanel = feedbackPanel;
    this.feedbackWindow = feedbackWindow;

    openAnswerSuggestion = new OpenAnswerDefinitionSuggestion(model.getObject());

    itemWindow = new ModalWindow("itemWindow");
    itemWindow.setCssClassName("onyx");
    itemWindow.setInitialWidth(ITEM_WINDOW_WIDTH);
    itemWindow.setInitialHeight(ITEM_WINDOW_HEIGHT);
    itemWindow.setResizable(true);
    itemWindow.setTitle(new ResourceModel("SuggestionItem"));
    itemWindow.setCloseButtonCallback(new CloseButtonCallback() {
      @Override
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        return true; // same as cancel
      }
    });
    add(itemWindow);

    add(deleteConfirm = new ConfirmationDialog("deleteConfirm"));
    deleteConfirm.setTitle(new StringResourceModel("ConfirmDeleteItem", this, null));
    deleteConfirm
        .setContent(new MultiLineLabel(deleteConfirm.getContentId(), new ResourceModel("ConfirmDeleteItemContent")));

    itemList = new EntityListTablePanel<String>("items", new ItemProvider(), new ItemListColumnProvider(),
        new ResourceModel("Items"), ITEMS_PER_PAGE);
    itemList.setAllowColumnSelection(false);
    add(itemList);

    List<ITab> tabs = new ArrayList<ITab>();
    tabs.add(new AbstractTab(new ResourceModel("Add.simple")) {
      @Override
      public Panel getPanel(String panelId) {
        return new SimpleAddPanel(panelId);
      }
    });
    tabs.add(new AbstractTab(new ResourceModel("Add.bulk")) {
      @Override
      public Panel getPanel(String panelId) {
        return new BulkAddPanel(panelId);
      }
    });
    add(new AjaxTabbedPanel("addTabs", tabs));

  }

  private class ItemProvider extends SortableDataProvider<String> {

    @Override
    public Iterator<String> iterator(int first, int count) {
      return openAnswerSuggestion.getSuggestionItems().iterator();
    }

    @Override
    public int size() {
      return openAnswerSuggestion.getSuggestionItems().size();
    }

    @Override
    public IModel<String> model(String item) {
      return new Model<String>(item);
    }

  }

  private class ItemListColumnProvider implements IColumnProvider<String>, Serializable {

    private final List<IColumn<String>> columns = new ArrayList<IColumn<String>>();

    private ItemListColumnProvider() {
      columns.add(new AbstractColumn<String>(new ResourceModel("Name"), "name") {
        @Override
        public void populateItem(Item<ICellPopulator<String>> cellItem, String componentId, IModel<String> rowModel) {
          cellItem.add(new Label(componentId, rowModel.getObject()));
        }
      });

      columns.add(new HeaderlessColumn<String>() {
        private static final long serialVersionUID = 1L;

        @Override
        public void populateItem(Item<ICellPopulator<String>> cellItem, String componentId, IModel<String> rowModel) {
          cellItem.add(new LinkFragment(componentId, rowModel));
        }
      });

    }

    @Override
    public List<IColumn<String>> getAdditionalColumns() {
      return null;
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<String>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<String>> getRequiredColumns() {
      return columns;
    }

  }

  private class LinkFragment extends Fragment {

    @SuppressWarnings("rawtypes")
    private LinkFragment(String id, final IModel<String> rowModel) {
      super(id, "linkFragment", SuggestionItemListPanel.this, rowModel);

      add(new AjaxLink<String>("editLink", rowModel) {
        @SuppressWarnings("unchecked")
        @Override
        public void onClick(AjaxRequestTarget target) {
          itemWindow.setContent(new SuggestionItemWindow("content",
              (IModel<OpenAnswerDefinition>) SuggestionItemListPanel.this.getDefaultModel(), rowModel,
              questionnaireModel, itemWindow) {
            @Override
            public void onSave(AjaxRequestTarget target) {
              target.addComponent(itemList);
            }
          });
          itemWindow.show(target);
        }
      });

      add(new AjaxLink("deleteLink") {
        @Override
        public void onClick(AjaxRequestTarget target) {
          deleteConfirm.setYesButtonCallback(new OnYesCallback() {
            @Override
            public void onYesButtonClicked(AjaxRequestTarget target) {
              openAnswerSuggestion.removeSuggestionItem(rowModel.getObject());
              target.addComponent(itemList);
            }
          });
          deleteConfirm.show(target);
        }
      });

    }
  }

  private class SimpleAddPanel extends Panel {

    private SimpleAddPanel(String id) {
      super(id);
      IModel<String> model = new Model<String>();
      Form<String> form = new Form<String>("form", model);
      form.setMultiPart(false);
      add(form);
      final TextField<String> value = new TextField<String>("value", model);
      value.setOutputMarkupId(true);
      value.setLabel(new ResourceModel("NewItem"));
      form.add(value);
      form.add(new SimpleFormComponentLabel("label", value));
      AjaxButton simpleAddButton = new AjaxButton("addButton", form) {
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form1) {
          if(value.getModelObject() == null) return;
          openAnswerSuggestion.addSuggestionItem(value.getModelObject());
          value.setModelObject(null);
          target.addComponent(value);
          target.addComponent(itemList);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      };
      value.add(
          new AttributeAppender("onkeypress", true, new Model<String>(buildPressEnterScript(simpleAddButton)), " "));
      simpleAddButton.add(new Image("img", Images.ADD));
      form.add(simpleAddButton);
    }

    private String buildPressEnterScript(AjaxButton addButton) {
      return "if (event.keyCode == 13) {document.getElementById('" + addButton
          .getMarkupId() + "').click(); return false;} else {return true;};";
    }
  }

  private class BulkAddPanel extends Panel {

    private BulkAddPanel(String id) {
      super(id);
      IModel<String> model = new Model<String>();
      Form<String> form = new Form<String>("form", model);
      form.setMultiPart(false);
      add(form);
      final TextArea<String> values = new TextArea<String>("values", model);
      values.setOutputMarkupId(true);
      values.setLabel(new ResourceModel("NewItems"));
      form.add(values);
      form.add(new SimpleFormComponentLabel("label", values));
      AjaxSubmitLink bulkAddLink = new AjaxSubmitLink("bulkAddLink") {

        @SuppressWarnings("unchecked")
        @Override
        protected void onSubmit(AjaxRequestTarget target, Form<?> form1) {
          String[] items = StringUtils.split(values.getModelObject(), ',');
          if(items == null) return;

          for(String item : new LinkedHashSet<String>(Arrays.asList(items))) {
            openAnswerSuggestion.addSuggestionItem(item);
          }
          values.setModelObject(null);
          target.addComponent(values);
          target.addComponent(itemList);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form1) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      };

      bulkAddLink
          .add(new Image("bulkAddImg", Images.ADD).add(new AttributeModifier("title", true, new ResourceModel("Add"))));
      form.add(bulkAddLink);
    }
  }

}
