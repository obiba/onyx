/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import static org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator.ROW_COUNT_KEY;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.ListToGridPermutator;
import org.obiba.onyx.quartz.editor.category.CategoryPropertiesPanel;
import org.obiba.onyx.quartz.editor.form.AbstractQuestionnaireElementPanelForm;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class QuestionPropertiesPanel extends AbstractQuestionnaireElementPanelForm<Question> {

  private static final String SINGLE_COLUMN_LAYOUT = "singleColumnLayout";

  private static final String GRID_LAYOUT = "gridLayout";

  protected final Logger log = LoggerFactory.getLogger(getClass());

  protected ModalWindow categoryWindow;

  @SpringBean
  protected QuestionnaireBundleManager questionnaireBundleManager;

  private FormComponent<String> layoutRadioGroup;

  private TextField<Integer> nbRowsField;

  public QuestionPropertiesPanel(String id, IModel<Question> model, Questionnaire questionnaireParent, final ModalWindow questionWindow) {
    super(id, model, questionnaireParent, questionWindow);
    createComponent();
  }

  public void createComponent() {

    // TODO for test only

    categoryWindow = new ModalWindow("categoryWindow");
    categoryWindow.setCssClassName("onyx");
    categoryWindow.setInitialWidth(1000);
    categoryWindow.setInitialHeight(600);
    categoryWindow.setResizable(true);
    form.add(categoryWindow);

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(form.getModel(), "name"));
    name.add(new RequiredFormFieldBehavior());
    form.add(name);

    TextField<String> variableName = new TextField<String>("variableName", new PropertyModel<String>(form.getModel(), "variableName"));
    variableName.add(new StringValidator.MaximumLengthValidator(20));
    form.add(variableName);

    form.add(new CheckBox("multiple", new PropertyModel<Boolean>(form.getModel(), "multiple")));

    // radio group without default selection
    layoutRadioGroup = new RadioGroup<String>("layoutRadioGroup", new Model<String>());
    form.add(layoutRadioGroup);

    Radio<String> singleColumnLayout = new Radio<String>(SINGLE_COLUMN_LAYOUT, new Model<String>(SINGLE_COLUMN_LAYOUT));
    singleColumnLayout.setLabel(new StringResourceModel("LayoutSingle", QuestionPropertiesPanel.this, null));
    layoutRadioGroup.add(singleColumnLayout);
    layoutRadioGroup.add(new SimpleFormComponentLabel("singleColumnLayoutLabel", singleColumnLayout));

    Radio<String> gridLayout = new Radio<String>(GRID_LAYOUT, new Model<String>(GRID_LAYOUT));
    gridLayout.setLabel(new StringResourceModel("LayoutGrid", QuestionPropertiesPanel.this, null));
    layoutRadioGroup.add(gridLayout);
    layoutRadioGroup.add(new SimpleFormComponentLabel("gridLayoutLabel", gridLayout));

    Integer nbRows = ListToGridPermutator.DEFAULT_ROW_COUNT;
    ValueMap uiArguments = form.getModelObject().getUIArgumentsValueMap();
    if(uiArguments != null && uiArguments.containsKey(ROW_COUNT_KEY)) {
      nbRows = uiArguments.getInt(ROW_COUNT_KEY);
    }
    form.add(nbRowsField = new TextField<Integer>("nbRows", new Model<Integer>(nbRows)));

    form.add(new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", form.getModelObject(), localePropertiesModel));

    @SuppressWarnings("hiding")
    final SortableList<QuestionCategory> categoryList = new SortableList<QuestionCategory>("categoryList", form.getModelObject().getQuestionCategories()) {
      @Override
      public String getItemLabel(QuestionCategory questionCategory) {
        return questionCategory.getCategory().getName();
      }

      @Override
      public void onUpdate(Component sortedComponent, int index, AjaxRequestTarget target) {
        QuestionCategory questionCategory = (QuestionCategory) sortedComponent.getDefaultModelObject();
        List<QuestionCategory> questionCategories = getForm().getModelObject().getQuestionCategories();
        questionCategories.remove(questionCategory);
        questionCategories.add(index, questionCategory);
      }

      @Override
      public void addItem(AjaxRequestTarget target) {
        categoryWindow.setContent(new CategoryPropertiesPanel("content", new Model<Category>(new Category(null)), getQuestionnaireParent(), categoryWindow) {

          @Override
          public void onSave(AjaxRequestTarget target, Category category) {
            super.onSave(target, category);
            Question question = QuestionPropertiesPanel.this.getForm().getModelObject();
            QuestionCategory questionCategory = new QuestionCategory();
            questionCategory.setCategory(category);
            // questionCategory.setExportName(exportName); TODO set exportName
            question.addQuestionCategory(questionCategory);
            refreshList(target);
          }
        });
        categoryWindow.show(target);
      }

      @Override
      public void editItem(QuestionCategory questionCategory, AjaxRequestTarget target) {
        categoryWindow.setContent(new CategoryPropertiesPanel("content", new Model<Category>(questionCategory.getCategory()), getQuestionnaireParent(), categoryWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, Category category) {
            super.onSave(target, category);
            refreshList(target);
          }
        });
        categoryWindow.show(target);
      }

      @Override
      public void deleteItem(QuestionCategory questionCategory, AjaxRequestTarget target) {
        getForm().getModelObject().getQuestionCategories().remove(questionCategory);
        refreshList(target);
      }
    };
    form.add(categoryList);

    form.add(new Link<Void>("previewLink") {

      @Override
      public void onClick() {
        Question q = getForm().getModelObject();
        log.info("name: " + q.getName() + ", varName: " + q.getVariableName() + ", multiple: " + q.isMultiple());
        //
        // PageBuilder pBuilder = QuestionnaireBuilder.createQuestionnaire("TEST",
        // "1.0").withSection("SECTION_1").withPage("PAGE_1");
        // QuestionBuilder qBuilder = pBuilder.withQuestion(q.getName(), "1", q.isMultiple()); //
        //
        // SingleDocumentQuestionnairePage preview = new SingleDocumentQuestionnairePage(new
        // Model(qBuilder.getQuestionnaire()));
        // setResponsePage(preview);
      }
    });

  }

  @Override
  public void onSave(AjaxRequestTarget target, Question question) {
    super.onSave(target, question);

    // Layout single or grid
    // Make sure that the categories are added before this...
    String layoutSelection = layoutRadioGroup.getModelObject();
    if(SINGLE_COLUMN_LAYOUT.equals(layoutSelection)) {
      question.addUIArgument(ROW_COUNT_KEY, Integer.toString(question.getCategories().size()));
    } else if(GRID_LAYOUT.equals(layoutSelection)) {
      question.addUIArgument(ROW_COUNT_KEY, Integer.toString(nbRowsField.getModelObject()));
    }

    // PageBuilder pBuilder = QuestionnaireBuilder.createQuestionnaire("TEST",
    // "1.0").withSection("SECTION_1").withPage("PAGE_1");
    // QuestionBuilder qBuilder = pBuilder.withQuestion(question.getName(), "1", question.isMultiple()); //
    // PropertiesPropertyKeyWriterImpl
    //
    // SingleDocumentQuestionnairePage preview = new SingleDocumentQuestionnairePage(new
    // Model(qBuilder.getQuestionnaire()));
    // modalWindow.getId()
    // modalWindow.getCsetContent(preview);
    // modalWindow.repreview.setVisible(true);
    //
    // PopupSettings popupSettings = new PopupSettings(PageMap.forName("popuppagemap")).setHeight(
    // 500).setWidth(500);
    // BookmarkablePageLink link = new BookmarkablePageLink("popupLink",
    // Popup.class).setPopupSettings(popupSettings))
    // DefaultQuestionPanel qPanel = new DefaultQuestionPanel("1", new Model(qBuilder.getElement()));
    //
    // ModalWindow modal = new Window("modalWindow");
    // modal.setCssClassName("onyx");
    // modal.setInitialWidth(500);
    // modal.setInitialHeight(300);
    // modal.setResizable(true);
    // modal.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
    // @Override
    // public boolean onCloseButtonClicked(AjaxRequestTarget target) {
    // return true; // same as cancel
    // }
    // });
    // modal.setContent(preview.render());
    // modal.show(target);

    // QuestionnaireRenderer qBuilder.getElement();
    // propWriter = new
    // PropertiesPropertyKeyWriterImpl();
    // propWriter.write(key, "");

    // TODO process this question
    // DefaultPropertyKeyProviderImpl
    // modalWindow.close(target);
  }
}
