/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.core.wicket;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.obiba.onyx.marble.core.wicket.wizard.ConsentWizardPanel;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

public class MarblePanel extends Panel implements IEngineComponentAware {

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private ModuleRegistry moduleRegistry;

  @SpringBean
  private ActiveConsentService activeConsentService;

  private ConsentWizardPanel wizardPanel;

  //
  // Constructors
  //

  @SuppressWarnings("serial")
  public MarblePanel(String id, Stage stage) {
    super(id);
    add(wizardPanel = new ConsentWizardPanel("content", new Model(activeConsentService.getConsent(true)), new StageModel(moduleRegistry, stage.getName())));
  }

  //
  // IEngineComponentAware Methods
  //

  public void setActionWindow(ActionWindow window) {
    wizardPanel.setActionWindow(window);
  }

  public void setFeedbackWindow(FeedbackWindow feedbackWindow) {
    wizardPanel.setFeedbackWindow(feedbackWindow);
  }

  public FeedbackWindow getFeedbackWindow() {
    return wizardPanel.getFeedbackWindow();
  }
}
