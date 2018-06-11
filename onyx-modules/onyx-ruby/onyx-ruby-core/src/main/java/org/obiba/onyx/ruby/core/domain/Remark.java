/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain;

import java.io.Serializable;

import org.springframework.context.MessageSourceResolvable;

public class Remark implements MessageSourceResolvable, Serializable {

  private static final long serialVersionUID = 1L;

  //
  // Instance Variables
  //
  private String code;

  //
  // Constructors
  //

  public Remark(String code) {
    this.code = code;
  }

  //
  // MessageSourceResolvable Methods
  //

  public Object[] getArguments() {
    return null;
  }

  public String[] getCodes() {
    return new String[] { "TubeRegistrationRemark." + code };
  }

  public String getDefaultMessage() {
    return code;
  }

  //
  // Methods
  //

  public String getCode() {
    return code;
  }
}