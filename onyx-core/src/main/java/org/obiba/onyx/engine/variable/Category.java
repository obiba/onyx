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

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 
 */
@XStreamAlias("category")
public class Category implements Serializable {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  private String name;

  @XStreamAsAttribute
  private String alt;

  @XStreamAsAttribute
  private Boolean missing;

  @XStreamOmitField
  private Variable variable;

  public Category(String name) {
    super();
    this.name = name;
  }

  public Category(String name, String alternateName, Boolean missing) {
    super();
    this.name = name;
    this.alt = alternateName;
    if(missing) {
      this.missing = missing;
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAlternateName() {
    return alt;
  }

  public void setAlternateName(String alternateName) {
    this.alt = alternateName;
  }

  public Boolean getMissing() {
    return missing;
  }

  public void setMissing(Boolean missing) {
    if(missing == null || !missing) {
      this.missing = null;
    } else {
      this.missing = missing;
    }
  }

  public Variable getVariable() {
    return variable;
  }

  public void setVariable(Variable variable) {
    this.variable = variable;
  }

  @Override
  public String toString() {
    return getName();
  }

}
