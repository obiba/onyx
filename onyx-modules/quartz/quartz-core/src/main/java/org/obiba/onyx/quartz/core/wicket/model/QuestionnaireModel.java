package org.obiba.onyx.quartz.core.wicket.model;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.springframework.context.ApplicationContext;

public class QuestionnaireModel extends LoadableDetachableModel implements IVisitor {

  private static final long serialVersionUID = -6997906325842949254L;

  private transient ApplicationContext context;

  private transient QuestionnaireFinder finder;

  private ILocalizable element;

  public QuestionnaireModel(ILocalizable element) {
    this.element = element;
  }

  @Override
  protected Object load() {

    // Get the Spring application context.
    if(context == null) {
      if(Application.get() instanceof SpringWebApplication) {
        context = ((SpringWebApplication) Application.get()).getSpringContextLocator().getSpringContext();
      } else {
        throw new WicketRuntimeException("Cannot load QuestionnaireStringResourceModel's object (not running within a SpringWebApplication)");
      }
    }

    // From the context, get the services required to resolve the string resource.
    ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService = (ActiveQuestionnaireAdministrationService) context.getBean("activeQuestionnaireAdministrationService");
    QuestionnaireBundleManager bundleManager = (QuestionnaireBundleManager) context.getBean("questionnaireBundleManager");

    // Now use these services to get current questionnaire bundle.
    String bundleName = activeQuestionnaireAdministrationService.getQuestionnaire().getName();
    QuestionnaireBundle bundle = bundleManager.getBundle(bundleName);

    finder = QuestionnaireFinder.getInstance(bundle.getQuestionnaire());
    element.accept(this);

    return element;
  }

  @Override
  protected void onDetach() {
    context = null;
    finder = null;
  }

  public void visit(Questionnaire questionnaire) {
    this.element = questionnaire;
  }

  public void visit(Section section) {
    this.element = finder.findSection(section.getName());
  }

  public void visit(Page page) {
    this.element = finder.findPage(page.getName());
  }

  public void visit(Question question) {
    this.element = finder.findQuestion(question.getName());
  }

  public void visit(QuestionCategory questionCategory) {
    Question question = finder.findQuestion(questionCategory.getQuestion().getName());
    for(QuestionCategory qcat : question.getQuestionCategories()) {
      if(qcat.getName().equals(questionCategory.getName())) {
        this.element = qcat;
        break;
      }
    }
  }

  public void visit(Category category) {
    this.element = category;
  }

  public void visit(OpenAnswerDefinition openAnswerDefinition) {
    this.element = openAnswerDefinition;
  }

}
