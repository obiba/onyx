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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.ui.LocalesPropertiesAjaxTabbedPanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionPropertiesPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private final ModalWindow modalWindow;

  private final FeedbackWindow feedbackWindow;

  private final FeedbackPanel feedbackPanel;

  private static final String SINGLE_COLUMN = "LayoutSingle";

  private static final String GRID = "LayoutGrid";

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private RadioGroup<?> radioGroup;

  // private IPropertyKeyProvider propertyKeyProvider;

  public QuestionPropertiesPanel(String id, IModel<Question> model, final ModalWindow modalWindow) {
    super(id, model);
    this.modalWindow = modalWindow;

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);
    add(new QuestionForm("questionForm", model));
  }

  private class QuestionForm extends Form<Question> {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings({ "rawtypes", "unchecked", "serial" })
    public QuestionForm(String id, final IModel<Question> model) {
      super(id, model);

      final IModel qLabelModel = new MessageSourceResolvableStringModel(getModel());
      add(new Link("previewLink") {

        @Override
        public void onClick() {
          // TODO Auto-generated method stub
          Question q = model.getObject();
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

      TextField<String> name = new TextField<String>("name", new PropertyModel<String>(getModel(), "name"));
      name.add(new RequiredFormFieldBehavior());
      name.add(new StringValidator.MaximumLengthValidator(20));
      add(name);

      TextField<String> variableName = new TextField<String>("variableName", new PropertyModel<String>(getModel(), "variableName"));
      variableName.add(new StringValidator.MaximumLengthValidator(20));
      add(variableName);

      add(new CheckBox("multiple", new PropertyModel<Boolean>(getModel(), "multiple")));

      // radio group without default selection
      radioGroup = new RadioGroup("radioGroup", new Model());
      radioGroup.setLabel(qLabelModel);
      add(radioGroup);

      ListView radioList = new ListView("radioItem", Arrays.asList(new String[] { SINGLE_COLUMN, GRID })) {

        @Override
        protected void populateItem(ListItem listItem) {
          final String key = (String) listItem.getModelObject();
          final LayoutSelection selection = new LayoutSelection();
          selection.setSelectionKey(key);

          Model selectModel = new Model(selection);

          Radio radio = new Radio("radio", selectModel);
          radio.setLabel(new StringResourceModel(key, QuestionPropertiesPanel.this, null));

          listItem.add(radio);

          FormComponentLabel radioLabel = new SimpleFormComponentLabel("radioLabel", radio);
          listItem.add(radioLabel);
        }

      }.setReuseItems(true);
      radioGroup.add(radioList);

      // Labels tab panel
      List<LocaleProperties> list = new ArrayList<LocaleProperties>();
      LocaleProperties lp = new LocaleProperties(Locale.ENGLISH, getModelObject());
      lp.getValues()[0] = "eeeee";
      LocaleProperties lp2 = new LocaleProperties(Locale.FRENCH, getModelObject());
      lp2.getValues()[0] = "eefrfrfreee";
      list.add(lp);
      list.add(lp2);

      ListModel<LocaleProperties> localePropertiesModel = new ListModel<LocaleProperties>(list);
      LocalesPropertiesAjaxTabbedPanel localesPropertiesAjaxTabbedPanel = new LocalesPropertiesAjaxTabbedPanel("localesPropertiesTabs", getModelObject(), localePropertiesModel);

      add(localesPropertiesAjaxTabbedPanel);

      add(new AjaxButton("save", this) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          super.onSubmit();
          Question question = model.getObject();

          // Layout single or grid
          // Make sure that the categories are added before this...
          LayoutSelection layoutSelection = (LayoutSelection) radioGroup.getModelObject();
          if(layoutSelection.isSingleSelected()) {
            question.addUIArgument(QuestionCategoryListToGridPermutator.ROW_COUNT_KEY, Integer.toString(question.getCategories().size()));
          }

          log.info("name: " + question.getName() + ", varName: " + question.getVariableName() + ", multiple: " + question.isMultiple());
          log.info(question.getUIArgumentsValueMap().toString());

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

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      });

      add(new AjaxButton("cancel", this) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          modalWindow.close(target);
        }
      }.setDefaultFormProcessing(false));
    }
  }

  private class LayoutSelection implements Serializable {

    private static final long serialVersionUID = 1L;

    private String selectionKey;

    @SuppressWarnings("unused")
    public String getSelectionKey() {
      return selectionKey;
    }

    public void setSelectionKey(String selectionKey) {
      this.selectionKey = selectionKey;
    }

    @SuppressWarnings("unused")
    public boolean isSelected() {
      return selectionKey.equals(SINGLE_COLUMN) || selectionKey.equals(GRID);
    }

    @SuppressWarnings("unused")
    public boolean isSingleSelected() {
      return selectionKey.equals(SINGLE_COLUMN);
    }
  }
}
