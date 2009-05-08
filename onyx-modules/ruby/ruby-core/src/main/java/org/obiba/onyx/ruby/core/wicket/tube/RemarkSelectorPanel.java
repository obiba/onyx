/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.tube;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;

public class RemarkSelectorPanel extends Panel {

  private static final long serialVersionUID = 8697634793115334614L;

  @SpringBean(name = "activeTubeRegistrationService")
  private ActiveTubeRegistrationService activeTubeRegistrationService;

  private List<Remark> selectedRemark = new ArrayList<Remark>();

  /**
   * Panel for the remark selection list
   * @param id
   * @param rowModel
   * @param tubeRegistrationConfiguration
   */
  public RemarkSelectorPanel(String id, IModel rowModel, TubeRegistrationConfiguration tubeRegistrationConfiguration) {
    super(id, rowModel);
    setOutputMarkupId(true);

    List<Remark> remarks = tubeRegistrationConfiguration.getAvailableRemarks();

    RegisteredParticipantTube registeredParticipantTube = (RegisteredParticipantTube) rowModel.getObject();
    Set<String> tubeRemarks = registeredParticipantTube.getRemarks();

    for(Remark remark : remarks) {
      if(tubeRemarks.contains(remark.getCode())) {
        selectedRemark.add(remark);
      }
    }

    ListMultipleChoice listRemarks = new ListMultipleChoice("remarkSelect", new PropertyModel(this, "selectedRemark"), remarks, new IChoiceRenderer() {
      private static final long serialVersionUID = 1L;

      public Object getDisplayValue(Object object) {
        Remark remark = (Remark) object;
        return (new SpringStringResourceModel(remark.getCode()).getString());
      }

      public String getIdValue(Object object, int index) {
        Remark remark = (Remark) object;
        return remark.getCode();
      }
    });

    listRemarks.add(new AjaxFormComponentUpdatingBehavior("onblur") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        RegisteredParticipantTube registeredParticipantTube = (RegisteredParticipantTube) getModelObject();
        activeTubeRegistrationService.setTubeRemark(registeredParticipantTube.getBarcode(), selectedRemark);

        // Update component
        target.addComponent(RemarkSelectorPanel.this);
      }

    });

    listRemarks.setMaxRows(4);

    add(listRemarks);
  }

  public List<Remark> getSelectedRemark() {
    return selectedRemark;
  }

  public void setSelectedRemark(List<Remark> selectedRemark) {
    this.selectedRemark = selectedRemark;
  }
}
