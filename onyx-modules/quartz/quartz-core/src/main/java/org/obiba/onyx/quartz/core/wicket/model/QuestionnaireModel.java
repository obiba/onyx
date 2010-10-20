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
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionnaireModel<T extends IQuestionnaireElement> extends SpringDetachableModel<T> {

  private static final long serialVersionUID = -6997906325842949254L;

  private static final Logger log = LoggerFactory.getLogger(QuestionnaireModel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean
  private QuestionnaireBundleManager bundleManager;

  private String questionnaireName;

  private Class<? extends IQuestionnaireElement> elementClass;

  private String elementName;

  /**
   * Constructor for a questionnaire.
   * @param questionnaire
   */
  public QuestionnaireModel(Questionnaire questionnaire) {
    super();
    this.questionnaireName = questionnaire.getName();

    this.elementClass = Questionnaire.class;
    this.elementName = this.questionnaireName;
  }

  /**
   * Constructor, for the given questionnaire.
   * @param questionnaire
   * @param element
   */
  public QuestionnaireModel(Questionnaire questionnaire, IQuestionnaireElement element) {
    this(questionnaire.getName(), element);
  }

  /**
   * Constructor, for the questionnaire given name.
   * @param questionnaireName
   * @param element
   */
  public QuestionnaireModel(String questionnaireName, IQuestionnaireElement element) {
    super();
    this.questionnaireName = questionnaireName;

    initialize(element);
  }

  /**
   * Constructor, refering to active questionnaire.
   * @param element
   * @see ActiveQuestionnaireAdministrationService
   */
  public QuestionnaireModel(IQuestionnaireElement element) {
    super();
    this.questionnaireName = activeQuestionnaireAdministrationService.getQuestionnaire().getName();

    initialize(element);
  }

  private void initialize(IQuestionnaireElement element) {
    if(element.getClass().equals(Category.class)) throw new IllegalArgumentException("Category name is not unique, Use QuestionCategory instead.");
    this.elementClass = element.getClass();
    this.elementName = element.getName();
  }

  public String getElementName() {
    return elementName;
  }

  public Class<? extends IQuestionnaireElement> getElementClass() {
    return elementClass;
  }

  public String getQuestionnaireName() {
    return questionnaireName;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected T load() {
    // Now use these services to get current questionnaire bundle.
    QuestionnaireBundle bundle = bundleManager.getBundle(questionnaireName);

    Questionnaire questionnaire = bundle.getQuestionnaire();
    QuestionnaireFinder finder = QuestionnaireFinder.getInstance(questionnaire);
    // enable questionnaire elements caching
    if(questionnaire.getQuestionnaireCache() == null) {
      finder.buildQuestionnaireCache();
    }

    T element = null;
    if(elementClass.equals(Questionnaire.class)) {
      element = (T) questionnaire;
    } else if(elementClass.equals(Section.class)) {
      element = (T) finder.findSection(elementName);
    } else if(elementClass.equals(Page.class)) {
      element = (T) finder.findPage(elementName);
    } else if(elementClass.equals(Question.class)) {
      element = (T) finder.findQuestion(elementName);
    } else if(elementClass.equals(QuestionCategory.class)) {
      element = (T) finder.findQuestionCategory(QuestionCategory.getQuestionName(elementName), QuestionCategory.getCategoryName(elementName));
    } else if(elementClass.equals(OpenAnswerDefinition.class)) {
      element = (T) finder.findOpenAnswerDefinition(elementName);
    }

    return element;
  }

  @Override
  public int hashCode() {
    return questionnaireName.hashCode() + elementClass.hashCode() + elementName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    log.debug("equals ? {} == {}", this, obj);
    if(obj == this) {
      return true;
    } else if(obj instanceof QuestionnaireModel) {
      @SuppressWarnings("unchecked")
      QuestionnaireModel<T> model = (QuestionnaireModel<T>) obj;
      return (this.questionnaireName.equals(model.questionnaireName) && this.elementClass.equals(model.elementClass) && this.elementName.equals(model.elementName));
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer(super.toString());
    sb.append(":questionnaireName=").append(questionnaireName).append(":element=[").append(elementClass.getSimpleName()).append(":").append(elementName).append("]");
    return sb.toString();
  }
}
