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
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.wicket.wizard.WizardForm;
import org.obiba.onyx.wicket.wizard.WizardStepPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageStepPanel extends WizardStepPanel implements BeginEndListener {
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

  private long from = 0;

  //
  // Constructors
  //

  public PageStepPanel(String id, IModel pageModel) {
    super(id, pageModel);

    setOutputMarkupId(true);

    add(new Label(getTitleId(), new QuestionnaireStringResourceModel(activeQuestionnaireAdministrationService.getQuestionnaire(), "label")).setVisible(false));

    // Get the configured page layout factory.
    Page page = (Page) getDefaultModelObject();
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
    form.getPreviousLink().setVisible(previousEnabled);
    form.getNextLink().setVisible(true);
    ((QuestionnaireWizardForm) form).getInterruptLink().setEnabled(true);
    ((QuestionnaireWizardForm) form).getBeginLink().setVisible(true);
    ((QuestionnaireWizardForm) form).getEndLink().setVisible(true);
    form.getFinishLink().setVisible(false);

    if(target != null) {
      target.addComponent(form);
    }
  }

  @Override
  public void onStepOutPrevious(WizardForm form, AjaxRequestTarget target) {
    QuestionnaireWizardForm questionnaireWizardForm = (QuestionnaireWizardForm) form;
    pageLayout.onPrevious(target);
    setPreviousStep(questionnaireWizardForm.getPreviousStep());

    incrementTimeOnPage();
  }

  @Override
  public void onStepOutNext(WizardForm form, AjaxRequestTarget target) {
    QuestionnaireWizardForm questionnaireWizardForm = (QuestionnaireWizardForm) form;
    pageLayout.onNext(target);
    setNextStep(questionnaireWizardForm.getNextStep());

    incrementTimeOnPage();
  }

  @Override
  public void onStepOutNextError(WizardForm form, AjaxRequestTarget target) {
    target.addComponent(pageLayout);
  }

  @Override
  public void onStepInNext(WizardForm form, AjaxRequestTarget target) {
    super.onStepInNext(form, target);
    pageLayout.onStepInNext(target);

    from = System.currentTimeMillis();
  }

  @Override
  public void onStepInPrevious(WizardForm form, AjaxRequestTarget target) {
    super.onStepInPrevious(form, target);
    pageLayout.onStepInPrevious(target);

    from = System.currentTimeMillis();
  }

  //
  // BeginEndListener Methods
  //

  public void onBegin(WizardForm form, AjaxRequestTarget target) {
    pageLayout.onBegin(target);
  }

  public void onEnd(WizardForm form, AjaxRequestTarget target) {
    pageLayout.onEnd(target);
  }

  //
  // Methods
  //

  public void setPreviousEnabled(boolean previousEnabled) {
    this.previousEnabled = previousEnabled;
  }

  private void incrementTimeOnPage() {
    long durationMillis = System.currentTimeMillis() - from;
    activeQuestionnaireAdministrationService.incrementTimeOnPage((int) (durationMillis / 1000l));
  }
}
