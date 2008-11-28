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

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.ruby.core.domain.Remark;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;

public class RemarkSelectorPanelMock extends FormMock {

  private static final long serialVersionUID = 1L;

  public RemarkSelectorPanelMock(String id, IModel model) {
    super(id, model);
  }

  @Override
  public Component populateContent(String id, IModel model) {
    TubeRegistrationConfiguration tubeRegistrationConfiguration = initTubeRegistrationConfiguration();
    return new RemarkSelectorPanel(id, model, tubeRegistrationConfiguration);
  }

  private TubeRegistrationConfiguration initTubeRegistrationConfiguration() {
    TubeRegistrationConfiguration tubeRegistrationConfiguration = new TubeRegistrationConfiguration();

    List<Remark> remarks = new ArrayList<Remark>();
    remarks.add(new Remark("TubeRegistration.Remark.Inappropriate_labeling"));
    remarks.add(new Remark("TubeRegistration.Remark.Hemolyzed_sampling"));
    remarks.add(new Remark("TubeRegistration.Remark.Clotted_sample"));
    remarks.add(new Remark("TubeRegistration.Remark.Lipemic_sample"));

    tubeRegistrationConfiguration.setAvailableRemarks(remarks);
    return (tubeRegistrationConfiguration);
  }
}
