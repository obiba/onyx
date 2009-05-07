/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * 
 */
@XStreamAlias("category")
public class Category extends Variable {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  private String alt;

  @XStreamAsAttribute
  private Boolean escape;

  public Category(String name) {
    super(name);
  }

  public Category(String name, String alternateName) {
    super(name);
    this.alt = alternateName;
  }

  public String getAlternateName() {
    return alt;
  }

  public void setAlternateName(String alternateName) {
    this.alt = alternateName;
  }

  public Boolean getEscape() {
    return escape;
  }

  public Category setEscape(Boolean escape) {
    if(escape == null || !escape) {
      this.escape = null;
    } else {
      this.escape = escape;
    }
    return this;
  }

}
