/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import java.io.IOException;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireCreator;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.editor.input.LocaleChoiceRenderer;
import org.obiba.onyx.quartz.editor.input.LocaleListModel;
import org.obiba.onyx.quartz.editor.input.palette.InputPalette;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionnairePropertiesPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private final ModalWindow modalWindow;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  private FeedbackPanel feedbackPanel;

  private FeedbackWindow feedbackWindow;

  protected final Logger log = LoggerFactory.getLogger(getClass());

  public QuestionnairePropertiesPanel(String id, IModel<Questionnaire> model, ModalWindow modalWindow) {
    super(id, model);
    this.modalWindow = modalWindow;

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);
    add(new QuestionnaireForm("questionnaireForm", model));

  }

  public class QuestionnaireForm extends Form<Questionnaire> {

    private static final long serialVersionUID = 1L;

    /**
     * @param id
     * @param model
     */
    public QuestionnaireForm(String id, final IModel<Questionnaire> model) {
      super(id, model);

      TextField<String> name = new TextField<String>("name", new PropertyModel<String>(getModel(), "name"));
      name.add(new RequiredFormFieldBehavior());
      name.add(new StringValidator.MaximumLengthValidator(20));
      add(name);

      TextField<String> version = new TextField<String>("version", new PropertyModel<String>(getModel(), "version"));
      version.add(new RequiredFormFieldBehavior());
      version.add(new StringValidator.MaximumLengthValidator(20));
      add(version);

      // Locale.getAvailableLocales()[0].getDisplayName()
      WebMarkupContainer localesContainer = new WebMarkupContainer("localesContainer");
      localesContainer.setVisible(true);
      localesContainer.add(new InputPalette<Locale>("locales", getDefaultModel(), "locales", "locales", new LocaleListModel<Locale>(), new LocaleChoiceRenderer()));

      add(localesContainer);

      add(new AjaxButton("save", this) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          super.onSubmit();
          Questionnaire questionnaire = model.getObject();
          log.info(questionnaire.getName() + " " + questionnaire.getVersion());

          try {
            QuestionnaireCreator qCreator = new QuestionnaireCreator();
            qCreator.createQuestionnaire(QuestionnaireBuilder.createQuestionnaire(questionnaire.getName(), questionnaire.getVersion()));
          } catch(IOException e) {
            e.printStackTrace();
          }
          QuestionnaireBuilder.createQuestionnaire(questionnaire.getName(), questionnaire.getVersion());
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      });

      // A cancel button: use an AjaxButton and disable the "defaultFormProcessing". Seems that this is the best
      // practice for this type of behavior
      add(new AjaxButton("cancel", this) {

        private static final long serialVersionUID = 1L;

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          modalWindow.close(target);
        }
      }.setDefaultFormProcessing(false));
    }
  }
}
