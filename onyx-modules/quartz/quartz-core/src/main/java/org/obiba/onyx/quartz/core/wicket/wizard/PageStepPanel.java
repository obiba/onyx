/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.wicket.layout.IPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayoutFactoryRegistry;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.springframework.beans.factory.InitializingBean;

public class PageStepPanel extends WizardStepPanel implements InitializingBean {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private PageLayoutFactoryRegistry pageLayoutFactoryRegistry;

  private Page questionnairePage;

  //
  // Constructors
  //

  public PageStepPanel(String id, Page questionnairePage) {
    super(id);

    setOutputMarkupId(true);

    this.questionnairePage = questionnairePage;
  }

  //
  // InitializingBean Methods
  //

  public void afterPropertiesSet() {
    // Get the configured page layout factory.
    IPageLayoutFactory pageLayoutFactory = pageLayoutFactoryRegistry.getFactory(questionnairePage.getUIFactoryName());

    // Create the page layout component, using the configured factory.
    add(pageLayoutFactory.createLayout("pageLayout", questionnairePage));
  }

  //
  // WizardStepPanel Methods
  //

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setEnabled(true);
    form.getNextLink().setEnabled(false);
    form.getFinishLink().setEnabled(false);

    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

  @Override
  public void onStepOutPrevious(WizardForm form, AjaxRequestTarget target) {
    QuestionnaireWizardForm questionnaireWizardForm = (QuestionnaireWizardForm) form;
    setPreviousStep(questionnaireWizardForm.getPreviousStep(this));
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    QuestionnaireWizardForm questionnaireWizardForm = (QuestionnaireWizardForm) form;
    setNextStep(questionnaireWizardForm.getNextStep(this));
  }

  //
  // Methods
  //

  public Page getQuestionnairePage() {
    return questionnairePage;
  }
}
