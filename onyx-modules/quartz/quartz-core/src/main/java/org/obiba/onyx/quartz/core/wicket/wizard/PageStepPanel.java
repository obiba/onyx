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
import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.IPageLayoutFactory;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayout;
import org.obiba.onyx.quartz.core.wicket.layout.PageLayoutFactoryRegistry;
import org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.ProgressBarPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageStepPanel extends QuestionnaireWizardStepPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(PageStepPanel.class);

  //
  // Instance Variables
  //

  @SpringBean
  private PageLayoutFactoryRegistry pageLayoutFactoryRegistry;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private PageLayout pageLayout;

  private boolean previousEnabled;

  //
  // Constructors
  //

  public PageStepPanel(String id, IModel pageModel) {
    super(id, pageModel);

    setOutputMarkupId(true);

    add(new Label(getTitleId(), new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "label")).setVisible(false));

    // Get the configured page layout factory.
    Page page = (Page) getModelObject();
    log.debug("page={}", page);
    IPageLayoutFactory pageLayoutFactory = pageLayoutFactoryRegistry.get(page.getUIFactoryName());

    // Create the page layout component, using the configured factory.
    pageLayout = pageLayoutFactory.createLayout(getContentId(), pageModel);
    add(pageLayout);

    previousEnabled = true; // default to true
  }

  //
  // WizardStepPanel Methods
  //

  @Override
  public void handleWizardState(WizardForm form, AjaxRequestTarget target) {
    form.getPreviousLink().setEnabled(previousEnabled);
    form.getNextLink().setEnabled(true);
    ((QuestionnaireWizardForm) form).getInterruptLink().setEnabled(true);
    form.getFinishLink().setEnabled(false);

    if(target != null) {
      target.addComponent(form);
    }
  }

  @Override
  public void onStepOutPrevious(WizardForm form, AjaxRequestTarget target) {
    QuestionnaireWizardForm questionnaireWizardForm = (QuestionnaireWizardForm) form;
    pageLayout.onPrevious(target);
    setPreviousStep(questionnaireWizardForm.getPreviousStep());
    onPageStep(target);
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    QuestionnaireWizardForm questionnaireWizardForm = (QuestionnaireWizardForm) form;
    pageLayout.onNext(target);
    setNextStep(questionnaireWizardForm.getNextStep());
    onPageStep(target);
  }

  @Override
  public void onStepOutNextError(WizardForm form, AjaxRequestTarget target) {
    target.addComponent(pageLayout);
    onPageStep(target);
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    updateProgressBar(form);
    pageLayout.onStepInNext(target);
  }

  @Override
  public void onStepInPrevious(WizardForm form, AjaxRequestTarget target) {
    updateProgressBar(form);
    pageLayout.onStepInPrevious(target);
  }

  //
  // Methods
  //

  public void setPreviousEnabled(boolean previousEnabled) {
    this.previousEnabled = previousEnabled;
  }

  /**
   * Updates the progress bar to display the percentage completed for this Questionnaire Wizard.
   * 
   * @param form
   */
  private void updateProgressBar(WizardForm form) {

    // Calculate the percentage
    int progressPercentage = calculatePercentageCompleted();

    // Update the progress bar to display the percentage
    ProgressBarPanel progressBar = ((QuestionnaireWizardForm) form).getProgressBar();
    progressBar.setProgressPercentage(progressPercentage);

    // Make sure the progress bar is visible
    progressBar.setVisible(true);
  }

  /**
   * Calculates the percentage completed for this Questionnaire Wizard.
   * 
   * @return The percentage completed.
   */
  private int calculatePercentageCompleted() {

    int pageTotalCount = activeQuestionnaireAdministrationService.getLastPageNumber();
    log.debug("Total number of pages {}", pageTotalCount);

    int lastCompletedPageNo = activeQuestionnaireAdministrationService.getCurrentPageNumber() - 1;
    log.debug("Last completed page number {}", lastCompletedPageNo);

    int progressPercentage = (int) (((float) lastCompletedPageNo / (float) pageTotalCount) * 100);
    log.debug("Progress percentage {}", progressPercentage);

    return progressPercentage;
  }

}
