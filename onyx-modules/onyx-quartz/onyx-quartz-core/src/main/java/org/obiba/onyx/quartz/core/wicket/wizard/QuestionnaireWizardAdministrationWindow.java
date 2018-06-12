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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.obiba.onyx.wicket.reusable.WizardAdministrationWindow;

/**
 * Administration window (dialog) for questionnaires.
 * 
 * Includes "Begin" and "End" buttons for jumping to the first question and the next question to be answered,
 * respectively.
 */
public class QuestionnaireWizardAdministrationWindow extends WizardAdministrationWindow {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  // private static final Logger log = LoggerFactory.getLogger(QuestionnaireWizardAdministrationWindow.class);

  //
  // Instance Variables
  //

  private AjaxLink<?> beginLink;

  private AjaxButton endLink;

  //
  // Constructors
  //

  public QuestionnaireWizardAdministrationWindow(String id, QuestionnaireWizardForm form) {
    super(id);

    setInitialWidth(550);

    createBeginAndEndButtons(form);
  }

  //
  // WizardAdministrationWindow Methods
  //

  @Override
  public void setOptionsToShow() {
    addOption("Begin", OptionSide.RIGHT, beginLink, "begin");
    addSubmitOption("End", OptionSide.RIGHT, endLink, "end");

    super.setOptionsToShow();
  }

  //
  // Methods
  //

  @SuppressWarnings("rawtypes")
  private void createBeginAndEndButtons(final QuestionnaireWizardForm form) {
    beginLink = new AjaxLink("begin") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        setStatus(Status.OTHER2);
        if(getCloseButtonCallback() == null || getCloseButtonCallback().onCloseButtonClicked(target, getStatus())) {
          QuestionnaireWizardAdministrationWindow.this.close(target);
        }
      }

    };
    beginLink.add(new AttributeAppender("class", new Model<String>("begin"), " "));

    endLink = new AjaxButton("end", form) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form<?> form1) {
        setStatus(Status.OTHER3);
        if(getCloseButtonCallback() == null || getCloseButtonCallback().onCloseButtonClicked(target, getStatus())) {
          QuestionnaireWizardAdministrationWindow.this.close(target);
        }
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form1) {
        setStatus(Status.ERROR);
        ((QuestionnaireWizardForm) form1).showFeedbackWindow(target);
      }
    };
    endLink.add(new AttributeAppender("class", new Model<String>("end"), " "));
  }

  public Component getBeginLink() {
    return beginLink;
  }

  public Component getEndLink() {
    return endLink;
  }
}
