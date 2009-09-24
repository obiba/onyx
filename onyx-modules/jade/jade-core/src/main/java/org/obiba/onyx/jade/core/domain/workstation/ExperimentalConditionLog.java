/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.domain.workstation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.core.domain.Attribute;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("experimentalConditionLog")
public class ExperimentalConditionLog implements Serializable {

  private static final long serialVersionUID = 1L;

  private String name;

  @XStreamImplicit(itemFieldName = "attribute")
  private List<Attribute> attributes;

  private List<String> instructions;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Attribute> getAttributes() {
    return (attributes != null) ? attributes : (attributes = new ArrayList<Attribute>());
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  public List<String> getInstructions() {
    return (instructions != null) ? instructions : (instructions = new ArrayList<String>());
  }

  public void setInstructions(List<String> instructions) {
    this.instructions = instructions;
  }

}
