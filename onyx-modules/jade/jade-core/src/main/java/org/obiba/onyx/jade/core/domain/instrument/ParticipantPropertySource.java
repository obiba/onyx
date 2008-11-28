/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.instrument;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.obiba.onyx.jade.core.service.InputSourceVisitor;

@Entity
@DiscriminatorValue("ParticipantPropertySource")
public class ParticipantPropertySource extends InputSource {

  private static final long serialVersionUID = -5505114802454360982L;
  
  @Column(length = 200)
  private String property;

  public ParticipantPropertySource() {
    super();
  }

  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }
  
  @Override
  public void accept(InputSourceVisitor visitor) {
    visitor.visit(this);
  }
}
