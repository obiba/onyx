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

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

/**
 * 
 */
public class QuestionnaireStringModifierModel extends AbstractReadOnlyModel<String> {

  private static final long serialVersionUID = 1L;

  private IModel<?> model;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  public QuestionnaireStringModifierModel(IModel<?> model) {
    super();
    InjectorHolder.getInjector().inject(this);
    this.model = model;
  }

  public String getObject() {
    Object object = model.getObject();
    return (object != null) ? object.toString().replaceAll("Questionnaire\\[" + activeQuestionnaireAdministrationService.getQuestionnaire().getName() + "\\.", "\\[") : null;
  }

  @Override
  public void detach() {
    super.detach();
    this.model.detach();
  }
}