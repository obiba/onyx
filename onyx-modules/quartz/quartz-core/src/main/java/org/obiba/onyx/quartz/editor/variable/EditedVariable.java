/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.variable;

import java.io.Serializable;

import org.obiba.magma.ValueType;

/**
 *
 */
public class EditedVariable implements Serializable {

  private static final long serialVersionUID = 1L;

  private String name;

  private String valueType;

  private String script;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ValueType getValueType() {
    if(valueType != null) {
      return ValueType.Factory.forName(valueType);
    }
    return null;
  }

  public void setValueType(ValueType valueType) {
    if(valueType != null) {
      this.valueType = valueType.getName();
    } else {
      this.valueType = null;
    }
  }

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

}
