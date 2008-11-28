/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.ruby.core.wicket.wizard.RubyWizardPanel;
import org.obiba.onyx.wicket.IEngineComponentAware;
import org.obiba.onyx.wicket.StageModel;
import org.obiba.onyx.wicket.action.ActionWindow;

/**
 * Ruby widget entry point.
 */
public class RubyPanel extends Panel implements IEngineComponentAware {
  //
  // Constants

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean
  private ModuleRegistry moduleRegistry;

  private RubyWizardPanel wizardPanel;

  //
  // Constructors
  //

  public RubyPanel(String id, Stage stage, boolean resuming) {
    super(id);

    add(wizardPanel = new RubyWizardPanel("content", new Model(), new StageModel(moduleRegistry, stage.getName()), resuming));
  }

  //
  // IEngineComponentAware Methods
  //

  public void setActionWindwon(ActionWindow window) {
    wizardPanel.setActionWindow(window);
  }

  public void setFeedbackPanel(FeedbackPanel feedbackPanel) {
    wizardPanel.setFeedbackPanel(feedbackPanel);
  }

  public FeedbackPanel getFeedbackPanel() {
    return wizardPanel.getFeedbackPanel();
  }
}
