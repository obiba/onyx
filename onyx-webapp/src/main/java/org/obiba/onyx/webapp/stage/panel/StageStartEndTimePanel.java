/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.stage.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.wicket.util.DateModelUtils;

public class StageStartEndTimePanel extends Panel {

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private static final long serialVersionUID = 1L;

  public StageStartEndTimePanel(String id, IModel stageModel) {
    super(id, stageModel);
    add(new Label("startTime", DateModelUtils.getShortDateTimeModel(new PropertyModel(this, "stageExecution.startTime"))));
    add(new Label("endTime", DateModelUtils.getShortDateTimeModel(new PropertyModel(this, "stageExecution.endTime"))));
    get("startTime").setRenderBodyOnly(true);
    get("endTime").setRenderBodyOnly(true);
  }

  @Override
  public boolean isVisible() {
    IStageExecution exec = getStageExecution();
    return exec.isCompleted() && (exec.getStartingActionType() == ActionType.EXECUTE);
  }

  public IStageExecution getStageExecution() {
    Stage stage = (Stage) getModelObject();
    return activeInterviewService.getStageExecution(stage);
  }

}
