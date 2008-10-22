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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.IPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayout;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayoutFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;

public class PageStepPanel extends WizardStepPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private PageLayoutFactoryRegistry pageLayoutFactoryRegistry;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private Page questionnairePage;

  private PageLayout pageLayout;

  //
  // Constructors
  //

  public PageStepPanel(String id, Page questionnairePage) {
    super(id);

    setOutputMarkupId(true);

    this.questionnairePage = questionnairePage;

    add(new Label(getTitleId(), new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "label")));

    // Get the configured page layout factory.
    IPageLayoutFactory pageLayoutFactory = pageLayoutFactoryRegistry.getFactory(questionnairePage.getUIFactoryName());

    // Create the page layout component, using the configured factory.
    pageLayout = pageLayoutFactory.createLayout(getContentId(), questionnairePage);
    add(pageLayout);
  }

  //
  // WizardStepPanel Methods
  //

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setEnabled(true);
    form.getNextLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);

    if(target != null) {
      target.addComponent(form.getPreviousLink());
      target.addComponent(form.getNextLink());
    }
  }

  @Override
  public void onStepOutPrevious(WizardForm form, AjaxRequestTarget target) {
    QuestionnaireWizardForm questionnaireWizardForm = (QuestionnaireWizardForm) form;
    pageLayout.onPrevious(target);
    setPreviousStep(questionnaireWizardForm.getPreviousStep());
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    QuestionnaireWizardForm questionnaireWizardForm = (QuestionnaireWizardForm) form;
    pageLayout.onNext(target);
    setNextStep(questionnaireWizardForm.getNextStep());
  }

  //
  // Methods
  //

  public Page getQuestionnairePage() {
    return questionnairePage;
  }
}
