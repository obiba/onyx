/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.participant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Groups related {@code ParticipantAttribute}s.
 */
public class Group implements ParticipantElement, Serializable {

  private static final long serialVersionUID = -3365677950397361805L;

  public static final String DEFAULT_GROUP_NAME = "DEFAULT_GROUP";

  private final String name;

  private List<ParticipantAttribute> participantAttributes = new ArrayList<ParticipantAttribute>();

  public Group(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public List<ParticipantAttribute> getParticipantAttributes() {
    return Collections.unmodifiableList(participantAttributes);
  }

  void addParticipantAttribute(ParticipantAttribute participantAttribute) {
    participantAttributes.add(participantAttribute);
  }

  public boolean isDefaultGroup() {
    return (name == DEFAULT_GROUP_NAME);
  }
}
