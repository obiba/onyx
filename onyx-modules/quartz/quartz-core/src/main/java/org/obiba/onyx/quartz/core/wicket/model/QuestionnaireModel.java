/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.model;

import org.apache.wicket.spring.injection.annot.SpringBean;
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
import org.obiba.onyx.wicket.model.SpringDetachableModel;

public class QuestionnaireModel extends SpringDetachableModel implements IVisitor {

  private static final long serialVersionUID = -6997906325842949254L;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean
  private QuestionnaireBundleManager bundleManager;

  private transient QuestionnaireFinder finder;

  private String questionnaireName;

  private ILocalizable element;

  /**
   * Constructor for a questionnaire.
   * @param questionnaire
   */
  public QuestionnaireModel(Questionnaire questionnaire) {
    super();
    this.questionnaireName = questionnaire.getName();
    this.element = questionnaire;
  }

  /**
   * Constructor, for the given questionnaire.
   * @param questionnaire
   * @param element
   */
  public QuestionnaireModel(Questionnaire questionnaire, ILocalizable element) {
    this(questionnaire.getName(), element);
  }

  /**
   * Constructor, for the questionnaire given name.
   * @param questionnaireName
   * @param element
   */
  public QuestionnaireModel(String questionnaireName, ILocalizable element) {
    super();
    this.questionnaireName = questionnaireName;
    this.element = element;
  }

  /**
   * Constructor, refering to active questionnaire.
   * @param element
   * @see ActiveQuestionnaireAdministrationService
   */
  public QuestionnaireModel(ILocalizable element) {
    super();
    this.questionnaireName = activeQuestionnaireAdministrationService.getQuestionnaire().getName();
    this.element = element;
  }

  @Override
  protected Object load() {
    // Now use these services to get current questionnaire bundle.
    QuestionnaireBundle bundle = bundleManager.getBundle(questionnaireName);

    finder = QuestionnaireFinder.getInstance(bundle.getQuestionnaire());
    element.accept(this);

    return element;
  }

  @Override
  protected void onDetach() {
    finder = null;
  }

  public void visit(Questionnaire questionnaire) {
    this.element = finder.getQuestionnaire();
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
