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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;

public class MissingInstrumentInfoPanel extends Panel {
  private static final long serialVersionUID = 1L;

  @SpringBean
  private InstrumentService instrumentService;

  private List<InstrumentType> missingInstrumentTypes;

  private WebMarkupContainer infoMessages;

  public MissingInstrumentInfoPanel(String id) {
    super(id);
    setOutputMarkupId(true);

    updateMessages();
  }

  public void updateMessages() {
    missingInstrumentTypes = new ArrayList<InstrumentType>();
    for(InstrumentType type : instrumentService.getInstrumentTypes().values()) {
      if(instrumentService.getInstruments(type).size() == 0) {
        missingInstrumentTypes.add(type);
      }
    }

    if(infoMessages != null) {
      remove(infoMessages);
    }
    infoMessages = new WebMarkupContainer("infoMessages");
    RepeatingView infoMessageRepeater = new RepeatingView("infoMessageRepeater");
    infoMessages.add(infoMessageRepeater);
    for(InstrumentType type : missingInstrumentTypes) {
      SpringStringResourceModel stageNameResource = new SpringStringResourceModel(type.getName() + ".description", type.getName());
      String stageName = stageNameResource.getString();
      StringResourceModel infoMessage = new StringResourceModel("MissingInstrumentMessage", MissingInstrumentInfoPanel.this, new Model<ValueMap>(new ValueMap("instrument=" + type.getName() + ",stage=" + stageName)));

      infoMessageRepeater.add(new Label(infoMessageRepeater.newChildId(), infoMessage));
    }
    add(infoMessages);

  }

  @Override
  public boolean isVisible() {
    return missingInstrumentTypes.size() != 0;
  }

}
