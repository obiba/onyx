/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.provider;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page questions to be answered provider, performing the condition resolution for each question of the page.
 */
public abstract class AbstractQuestionnaireElementProvider<T extends IQuestionnaireElement, P extends IQuestionnaireElement> implements IDataProvider<T> {

  private static final long serialVersionUID = 227294946626164090L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(AbstractQuestionnaireElementProvider.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private IModel<P> model;

  public AbstractQuestionnaireElementProvider() {
    this(null);
  }

  public AbstractQuestionnaireElementProvider(IModel<P> model) {
    InjectorHolder.getInjector().inject(this);
    this.model = model;
  }

  public Iterator<T> iterator(int first, int count) {
    return getElementList().subList(first, first + count).iterator();
  }

  public int size() {
    return getElementList().size();
  }

  public IModel<T> model(T question) {
    return new QuestionnaireModel(question);
  }

  public void detach() {
    if(model != null) {
      model.detach();
    }
  }

  protected Questionnaire getQuestionnaire() {
    return activeQuestionnaireAdministrationService.getQuestionnaire();
  }

  protected P getProviderElement() {
    return model.getObject();
  }

  protected abstract List<T> getElementList();

}
