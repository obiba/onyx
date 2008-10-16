package org.obiba.onyx.quartz.core.wicket;

import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.wizard.QuestionnaireWizardForm;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardPanel;

public class QuartzPanel extends Panel implements IEngineComponentAware {

  private static final long serialVersionUID = 0L;

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;
  
  @SpringBean
  private ModuleRegistry moduleRegistry;

  private StageModel model;

  private ActionWindow actionWindow;

  private FeedbackPanel feedbackPanel;

  @SuppressWarnings("serial")
  public QuartzPanel(String id, Stage stage) {
    super(id);

    Questionnaire questionnaire = questionnaireBundleManager.getBundle(stage.getName()).getQuestionnaire();
    
    activeQuestionnaireAdministrationService.setQuestionnaire(questionnaire);
    if (activeQuestionnaireAdministrationService.getLanguage() == null) setDefaultLanguage();
    
    model = new StageModel(moduleRegistry, stage.getName());

    add(new WizardPanel("content", new Model(questionnaire)) {
      @Override
      public WizardForm createForm(String componentId) {
        return new QuestionnaireWizardForm(componentId) {

          @Override
          public void onCancel(AjaxRequestTarget target) {
            IStageExecution exec = activeInterviewService.getStageExecution((Stage) model.getObject());
            ActionDefinition actionDef = exec.getActionDefinition(ActionType.STOP);
            if(actionDef != null) {
              actionWindow.show(target, model, actionDef);
            }
          }

          @Override
          public void onFinish(AjaxRequestTarget target, Form form) {
            IStageExecution exec = activeInterviewService.getStageExecution((Stage) model.getObject());
            ActionDefinition actionDef = exec.getSystemActionDefinition(ActionType.COMPLETE);
            if(actionDef != null) {
              actionWindow.show(target, model, actionDef);
            }
          }

          @Override
          public void onError(AjaxRequestTarget target, Form form) {
            target.addComponent(feedbackPanel);
          }

          @Override
          public FeedbackPanel getFeedbackPanel() {
            return feedbackPanel;
          }

        };
      }

    });
  }

  public void setActionWindwon(ActionWindow window) {
    this.actionWindow = window;
  }

  public void setFeedbackPanel(FeedbackPanel feedbackPanel) {
    this.feedbackPanel = feedbackPanel;
  }

  public FeedbackPanel getFeedbackPanel() {
    return feedbackPanel;
  }

  private void setDefaultLanguage() {
    Locale sessionLocale = getSession().getLocale();
    
    if (activeQuestionnaireAdministrationService.getQuestionnaire().getLocales().contains(sessionLocale))
      activeQuestionnaireAdministrationService.setDefaultLanguage(sessionLocale);
    else activeQuestionnaireAdministrationService.setDefaultLanguage(activeQuestionnaireAdministrationService.getQuestionnaire().getLocales().get(0));
  }
  
}
