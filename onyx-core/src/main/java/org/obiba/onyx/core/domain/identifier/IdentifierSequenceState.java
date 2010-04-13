/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.identifier;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.obiba.core.domain.AbstractEntity;

/**
 * A POJO annotated with JPA annotations that can be persisted by the PersistenceManager. Used to persist the state of
 * the sequence, such that we can get it back between server restarts.
 */
@Entity
@Table(name = "identifier_sequence")
public class IdentifierSequenceState extends AbstractEntity {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //

  @Column(nullable = false)
  private String prefix;

  @Column(nullable = false)
  private long lastIdentifier;

  @Column(nullable = false)
  private Date lastUpdate;

  //
  // Methods
  //

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public long getLastIdentifier() {
    return lastIdentifier;
  }

  public void setLastIdentifier(long lastIdentifier) {
    this.lastIdentifier = lastIdentifier;
  }

  public Date getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(Date lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

}
