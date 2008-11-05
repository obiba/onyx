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

import org.springframework.context.MessageSourceResolvable;

/**
 * 
 */
public class BarcodePart {
  //
  // Instance Variables
  //

  private String part;

  private String title;

  //
  // Constructors
  //

  public BarcodePart(String part) {
    this.part = part;
  }

  //
  // Methods
  //

  public void setTitle(String title) {
    this.title = title;
  }

  public MessageSourceResolvable getPartTitle() {
    // TODO
    return null;
  }

  public MessageSourceResolvable getPartLabel() {
    // TODO
    return null;
  }
}
