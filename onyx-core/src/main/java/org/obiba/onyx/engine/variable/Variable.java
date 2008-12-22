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

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.util.data.DataType;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 */
@XStreamAlias("variable")
public class Variable extends Entity {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  private DataType dataType;

  @XStreamAsAttribute
  private String unit;

  @XStreamImplicit(itemFieldName = "category")
  private List<String> categories;

  public Variable(String name) {
    super(name);
  }

  /**
   * @param name
   * @param parent
   */
  public Variable(String name, Entity parent) {
    super(name, parent);
  }

  public DataType getDataType() {
    return dataType;
  }

  public Variable setDataType(DataType type) {
    this.dataType = type;
    return this;
  }

  public String getUnit() {
    return unit;
  }

  public Variable setUnit(String unit) {
    this.unit = unit;
    return this;
  }

  public List<String> getCategories() {
    return categories != null ? categories : (categories = new ArrayList<String>());
  }

  public Variable addCategory(String category) {
    if(category != null && category.length() != 0) {
      getCategories().add(category);
    }
    return this;
  }

  public Variable addCategories(String... categories) {
    if(categories != null) {
      for(String category : categories) {
        addCategory(category);
      }
    }
    return this;
  }
}
