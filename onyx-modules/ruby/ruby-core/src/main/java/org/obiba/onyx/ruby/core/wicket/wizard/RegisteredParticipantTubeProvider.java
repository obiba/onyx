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

import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;

public class RegisteredParticipantTubeProvider extends SortableDataProvider {

  private static final long serialVersionUID = 1L;

  public Iterator<RegisteredParticipantTube> iterator(int first, int count) {
    // TODO: Using service, get all registered participant tubes for the current participant.
    return null;
  }

  public IModel model(Object object) {
    // TODO: Implement RegisteredParticipantTubeModel.
    // return new RegisteredParticipantTubeModel((RegisteredParticipantTubeModel)object);
    return null;
  }

  public int size() {
    // TODO: Using service, get the number of registered participant tubes for the current participant.
    return 0;
  }
}