/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.array;

import java.io.Serializable;

class Row implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String id;

  private String label;

  private String description;

  public Row(String id, String label, String description) {
    super();
    this.id = id;
    this.label = label;
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  public String getDescription() {
    return description;
  }

}