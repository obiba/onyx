/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.wicket.wizard;

import java.util.Map;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.ruby.core.domain.ConditionalMessage;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;

public class TubeRegistrationPanel extends Panel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveTubeRegistrationService activeTubeRegistrationService;

  @SpringBean(name = "tubeRegistrationConfigurationMap")
  private Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigurationMap;

  //
  // Constructors
  //

  public TubeRegistrationPanel(String id) {
    super(id);

    setOutputMarkupId(true);

    ParticipantTubeRegistration participantTubeRegistration = activeTubeRegistrationService.getParticipantTubeRegistration();
    String tubeSetName = participantTubeRegistration.getTubeSetName();
    TubeRegistrationConfiguration tubeRegistrationConfiguration = tubeRegistrationConfigurationMap.get(tubeSetName);

    RepeatingView infoMessageRepeater = new RepeatingView("infoMessageRepeater");
    for(ConditionalMessage infoMessage : tubeRegistrationConfiguration.getInfoMessages()) {
      infoMessage.setActiveInterviewService(activeInterviewService);
      if(infoMessage.shouldDisplay()) {
        infoMessageRepeater.add(new Label(infoMessageRepeater.newChildId(), new MessageSourceResolvableStringModel(infoMessage)));
      }
    }

    add(infoMessageRepeater);
    add(new TubeBarcodePanel("tubeBarcodePanel"));
    add(new OnyxEntityList<RegisteredParticipantTube>("list", new RegisteredParticipantTubeProvider(), new RegisteredParticipantTubeColumnProvider(tubeRegistrationConfiguration), new Model("")));
  }
}