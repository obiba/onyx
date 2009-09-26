/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.workstation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.wicket.reusable.Dialog.Status;
import org.obiba.onyx.wicket.reusable.Dialog.WindowClosedCallback;
import org.obiba.wicket.markup.html.border.SeparatorMarkupComponentBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionsPanel extends ExperimentalConditionDialog {

  private static final long serialVersionUID = 5855667390712874428L;

  private static final Logger log = LoggerFactory.getLogger(ActionsPanel.class);

  @SpringBean
  private ExperimentalConditionService experimentalConditionService;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  public ActionsPanel(String id, IModel stageModel) {
    super(id, stageModel);
    setOutputMarkupId(true);

    RepeatingView repeating = new RepeatingView("link");
    add(repeating);
    SeparatorMarkupComponentBorder border = new SeparatorMarkupComponentBorder();

    for(LinkInfo linkInfo : getListOfLinkInfo((Instrument) stageModel.getObject())) {
      AjaxLink<LinkInfo> link = new AjaxLink<LinkInfo>(repeating.newChildId(), new Model<LinkInfo>(linkInfo)) {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          // TODO Auto-generated method stub
          System.out.println("***** clicke on calibrate " + getModelObject().name);
          getModelObject().onClick(target);
          // if(actionDefinition != null) {
          // modal.show(target, ActionsPanel.this.getDefaultModel(), actionDefinition);
          // } else {
          // log.warn("Concurrent interview administration. Session {} tried to execute ActionDefinition {} on stage {}, yet that ActionDefinition is not available for the current stage's state.",
          // new Object[] { WebSession.get().getId(), getModelObject(), getStage() });
          // setResponsePage(InterviewPage.class);
          // }
        }

      };
      link.add(new Label("action", new StringResourceModel(linkInfo.name, null)).setRenderBodyOnly(true));
      link.setComponentBorder(border);
      link.setVisible(linkInfo.isVisible());
      repeating.add(link);
    }

    // for(ActionDefinition actionDef : getStageExecution().getActionDefinitions()) {
    //
    // AjaxLink link = new AjaxLink(repeating.newChildId(), new Model(actionDef.getCode())) {
    //
    // private static final long serialVersionUID = 1L;
    //
    // @Override
    // public void onClick(AjaxRequestTarget target) {
    // // Before allowing the action, make sure our actionDefinition is sill available
    // // in the IStageExecution. This protects concurrent interview administration.
    // // See ONYX-154
    // IStageExecution stageExec = getStageExecution();
    // String code = (String) getModelObject();
    // ActionDefinition actionDefinition = null;
    // for(ActionDefinition definition : stageExec.getActionDefinitions()) {
    // if(code.equals(definition.getCode())) {
    // actionDefinition = definition;
    // break;
    // }
    // }
    // if(actionDefinition != null) {
    // modal.show(target, ActionsPanel.this.getDefaultModel(), actionDefinition);
    // } else {
    // log.warn("Concurrent interview administration. Session {} tried to execute ActionDefinition {} on stage {}, yet that ActionDefinition is not available for the current stage's state.",
    // new Object[] { WebSession.get().getId(), getModelObject(), getStage() });
    // setResponsePage(InterviewPage.class);
    // }
    // }
    //
    // };
    // link.add(new Label("action", new
    // MessageSourceResolvableStringModel(actionDef.getLabel())).setRenderBodyOnly(true));
    // link.setComponentBorder(border);
    // repeating.add(link);
    // }
  }

  private List<LinkInfo> getListOfLinkInfo(Instrument instrument) {
    List<LinkInfo> linkInfoList = new ArrayList<LinkInfo>();
    linkInfoList.add(new CalibrateLinkInfo("Calibrate", instrument));
    linkInfoList.add(new ReleaseLinkInfo("Release", instrument));
    linkInfoList.add(new InactivateLinkInfo("Inactivate", instrument));
    linkInfoList.add(new ActivateLinkInfo("Activate", instrument));
    return linkInfoList;
  }

  private abstract class LinkInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    protected Instrument instrument;

    public LinkInfo(String name, Instrument instrument) {
      this.name = name;
      this.instrument = instrument;
    }

    public boolean isVisible() {
      return true;
    }

    public String getName() {
      return name;
    }

    public Instrument getInstrument() {
      return instrument;
    }

    public abstract void onClick(AjaxRequestTarget target);
  }

  private class CalibrateLinkInfo extends LinkInfo {
    private static final long serialVersionUID = 1L;

    public CalibrateLinkInfo(String name, Instrument instrument) {
      super(name, instrument);
    }

    @Override
    public boolean isVisible() {
      return instrument.getStatus().equals(InstrumentStatus.ACTIVE) && experimentalConditionService.instrumentCalibrationExists(instrument.getType());
    }

    private InstrumentCalibration getInstrumentCalibration() {
      InstrumentCalibration instrumentCalibration = null;
      System.out.println("*** Instrument type is " + instrument.getType());
      System.out.println("*** Instrument name is " + instrument.getName());
      if(experimentalConditionService.instrumentCalibrationExists(instrument.getType())) {
        System.out.println("**True!!!!!! ");
        instrumentCalibration = experimentalConditionService.getInstrumentCalibrationByType(instrument.getType());
      }
      return instrumentCalibration;
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
      setExperimentalConditionLog(experimentalConditionService.getInstrumentCalibrationByType(instrument.getType()));

      getExperimentalConditionDialog().setWindowClosedCallback(new WindowClosedCallback() {
        private static final long serialVersionUID = 1L;

        public void onClose(AjaxRequestTarget target, Status status) {
          // TODO Refresh the instrument table.
          System.out.println("** Refresh the instrument table.");
          // ExperimentalConditionHistoryPanel newExperimentalConditionHistoryPanel =
          // getExperimentalConditionHistoryPanel();
          // experimentalConditionHistoryPanel.replaceWith(newExperimentalConditionHistoryPanel);
          // experimentalConditionHistoryPanel = newExperimentalConditionHistoryPanel;
          // if(experimentalConditionLogs.size() == 0) experimentalConditionHistoryPanel.setVisible(false);
          // target.addComponent(experimentalConditionHistoryPanel);
        }

      });
      StringResourceModel experimentalConditionNameResource = new StringResourceModel(instrument.getType(), ActionsPanel.this, null);
      String experimentalConditionName = experimentalConditionNameResource.getObject();
      getExperimentalConditionDialog().setTitle(new StringResourceModel("ExperimentalConditionDialogTitle", ActionsPanel.this, new Model<ValueMap>(new ValueMap("experimentalConditionName=" + experimentalConditionName))));
      getExperimentalConditionDialog().show(target);
    }
  }

  private class ReleaseLinkInfo extends LinkInfo {
    private static final long serialVersionUID = 1L;

    public ReleaseLinkInfo(String name, Instrument instrument) {
      super(name, instrument);
    }

    @Override
    public boolean isVisible() {
      return instrument.getStatus().equals(InstrumentStatus.ACTIVE);
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
      // TODO Auto-generated method stub

    }
  }

  private class InactivateLinkInfo extends LinkInfo {
    private static final long serialVersionUID = 1L;

    public InactivateLinkInfo(String name, Instrument instrument) {
      super(name, instrument);
    }

    @Override
    public boolean isVisible() {
      return instrument.getStatus().equals(InstrumentStatus.ACTIVE);
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
      // TODO Auto-generated method stub

    }
  }

  private class ActivateLinkInfo extends LinkInfo {
    private static final long serialVersionUID = 1L;

    public ActivateLinkInfo(String name, Instrument instrument) {
      super(name, instrument);
    }

    @Override
    public boolean isVisible() {
      return !instrument.getStatus().equals(InstrumentStatus.ACTIVE);
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
      // TODO Auto-generated method stub

    }
  }

}
