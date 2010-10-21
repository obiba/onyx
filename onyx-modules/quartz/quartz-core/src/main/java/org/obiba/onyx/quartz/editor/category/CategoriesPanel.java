/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.category;

import static org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator.ROW_COUNT_KEY;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.ListToGridPermutator;
import org.obiba.onyx.quartz.editor.question.EditedQuestion;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
@SuppressWarnings("serial")
public class CategoriesPanel extends Panel {

  private static final String SINGLE_COLUMN_LAYOUT = "singleColumnLayout";

  private static final String GRID_LAYOUT = "gridLayout";

  private final ModalWindow categoryWindow;

  public CategoriesPanel(String id, final IModel<EditedQuestion> model, final IModel<Questionnaire> questionnaireModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);

    Question question = model.getObject().getElement();

    add(CSSPackageResource.getHeaderContribution(CategoriesPanel.class, "CategoriesPanel.css"));

    categoryWindow = new ModalWindow("categoryWindow");
    categoryWindow.setCssClassName("onyx");
    categoryWindow.setInitialWidth(1000);
    categoryWindow.setInitialHeight(600);
    categoryWindow.setResizable(true);
    add(categoryWindow);

    // radio group without default selection
    String layoutValue = null;
    ValueMap uiArgumentsValueMap = question.getUIArgumentsValueMap();
    Integer nbRows = ListToGridPermutator.DEFAULT_ROW_COUNT;
    if(uiArgumentsValueMap != null && uiArgumentsValueMap.containsKey(ROW_COUNT_KEY)) {
      layoutValue = Integer.parseInt((String) uiArgumentsValueMap.get(ROW_COUNT_KEY)) == question.getCategories().size() ? SINGLE_COLUMN_LAYOUT : GRID_LAYOUT;
      nbRows = uiArgumentsValueMap.getInt(ROW_COUNT_KEY);
    }

    RadioGroup<String> layout = new RadioGroup<String>("layout", new Model<String>(uiArgumentsValueMap == null ? null : layoutValue));
    layout.setLabel(new ResourceModel("Layout"));
    layout.setRequired(true);
    add(layout);

    Radio<String> singleColumnLayout = new Radio<String>(SINGLE_COLUMN_LAYOUT, new Model<String>(SINGLE_COLUMN_LAYOUT));
    singleColumnLayout.setLabel(new ResourceModel("Layout.single"));
    layout.add(singleColumnLayout);
    layout.add(new SimpleFormComponentLabel("singleColumnLayoutLabel", singleColumnLayout));

    Radio<String> gridLayout = new Radio<String>(GRID_LAYOUT, new Model<String>(GRID_LAYOUT));
    gridLayout.setLabel(new ResourceModel("Layout.grid"));
    layout.add(gridLayout);
    layout.add(new SimpleFormComponentLabel("gridLayoutLabel", gridLayout));

    TextField<Integer> nbRowsField = new TextField<Integer>("nbRows", new Model<Integer>(nbRows), Integer.class);
    gridLayout.setLabel(new ResourceModel("NbRows"));
    add(nbRowsField);

    final IModel<String> addCategoryModel = new Model<String>();

    List<ITab> tabs = new ArrayList<ITab>();
    tabs.add(new AbstractTab(new ResourceModel("Add.simple")) {
      @Override
      public Panel getPanel(String panelId) {
        return new SimpleAddPanel(panelId, addCategoryModel);
      }
    });
    tabs.add(new AbstractTab(new ResourceModel("Add.bulk")) {
      @Override
      public Panel getPanel(String panelId) {
        return new BulkAddPanel(panelId, addCategoryModel);
      }
    });
    add(new AjaxTabbedPanel("addTabs", tabs));

    SortableList<QuestionCategory> categoryList = new SortableList<QuestionCategory>("categories", question.getQuestionCategories()) {

      @Override
      public Component getItemTitle(@SuppressWarnings("hiding") String id, QuestionCategory questionCategory) {
        return new Label(id, questionCategory.getName());
      }

      @Override
      public void editItem(QuestionCategory questionCategory, AjaxRequestTarget target) {
        categoryWindow.setTitle(new ResourceModel("Category"));
        categoryWindow.setContent(new CategoryPropertiesPanel("content", new Model<QuestionCategory>(questionCategory), questionnaireModel, categoryWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, EditedQuestionCategory editedCategory) {
            super.onSave(target1, editedCategory);
            refreshList(target1);
          }
        });
        categoryWindow.show(target);
      }

      @Override
      public void deleteItem(QuestionCategory questionCategory, AjaxRequestTarget target) {
        // QuestionPropertiesPanel.this.form.getModelObject().getElement().getQuestionCategories().remove(questionCategory);
        // refreshList(target);
        // persist(target);
      }

      @Override
      public Button[] getButtons() {
        return null;
      }

    };
    add(categoryList);

  }

  public class SimpleAddPanel extends Panel {

    public SimpleAddPanel(String id, IModel<String> model) {
      super(id, model);
      TextField<String> category = new TextField<String>("category", model, String.class);
      category.setLabel(new ResourceModel("NewCategory"));
      category.add(new StringValidator.MaximumLengthValidator(20));
      add(category);
      add(new SimpleFormComponentLabel("categoryLabel", category));
      AjaxLink<String> simpelAddLink = new AjaxLink<String>("simpleAddLink") {
        @Override
        public void onClick(AjaxRequestTarget target) {
          // TODO simple add
        }
      };
      Image image = new Image("simpleAddImg", new Model<String>("add.png"));
      image.add(new AttributeModifier("title", new ResourceModel("Add")));
      simpelAddLink.add(image);
      add(simpelAddLink);
    }
  }

  public class BulkAddPanel extends Panel {

    public BulkAddPanel(String id, IModel<String> model) {
      super(id, model);
      TextArea<String> categories = new TextArea<String>("categories", model);
      categories.setLabel(new ResourceModel("NewCategories"));
      add(categories);
      add(new SimpleFormComponentLabel("categoriesLabel", categories));
      AjaxLink<String> bulkAddLink = new AjaxLink<String>("bulkAddLink") {
        @Override
        public void onClick(AjaxRequestTarget target) {
          // TODO bulk add
        }
      };
      Image image = new Image("bulkAddImg", new Model<String>("add.png"));
      image.add(new AttributeModifier("title", new ResourceModel("Add")));
      bulkAddLink.add(image);
      add(bulkAddLink);
    }
  }

}
