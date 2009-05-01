/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.domain.user;

import java.util.Date;

/**
 * TODO Part of ONYX-235, which is unfinished. When finished this class must be annotated for hibernate, which will
 * change the database schema.
 */
class UserPassword {

  private final String password;

  private final Date issuedOn;

  private final User issuedBy;

  public UserPassword(String password, Date issuedOn, User issuedBy) {
    super();
    this.password = password;
    this.issuedOn = issuedOn;
    this.issuedBy = issuedBy;
  }

  String getPassword() {
    return password;
  }

  Date getIssuedOn() {
    return issuedOn;
  }

  User getIssuedBy() {
    return issuedBy;
  }

  @Override
  public String toString() {
    return "[UserPassword password=[" + password + "] issuedOn=[" + issuedOn + "] issuedBy=[" + issuedBy.getFullName() + "]]";
  }

}
