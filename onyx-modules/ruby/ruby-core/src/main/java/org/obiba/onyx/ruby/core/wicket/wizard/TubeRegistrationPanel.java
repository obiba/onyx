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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.service.ActiveTubeRegistrationService;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.panel.OnyxEntityList;

public class TubeRegistrationPanel extends Panel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final int ROWS_PER_PAGE = 50;

  //
  // Instance Variables
  //

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveTubeRegistrationService activeTubeRegistrationService;

  @SpringBean
  TubeRegistrationConfiguration tubeRegistrationConfiguration;

  private OnyxEntityList<RegisteredParticipantTube> list;

  //
  // Constructors
  //

  public TubeRegistrationPanel(String id) {
    super(id);

    Participant participant = activeInterviewService.getParticipant();
    add(list = new OnyxEntityList<RegisteredParticipantTube>("list", new RegisteredParticipantTubeProvider(), new RegisteredParticipantTubeColumnProvider(tubeRegistrationConfiguration), new SpringStringResourceModel("Ruby.RegisteredParticipantTubeList", new Object[] { participant.getFullName() }, null)));
  }
}