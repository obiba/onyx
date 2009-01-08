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
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.util.data.DataType;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 
 */
@XStreamAlias("variable")
public class Variable implements Serializable {

  private static final long serialVersionUID = 1L;

  @XStreamAsAttribute
  private String name;

  @XStreamOmitField
  private Variable parent;

  @XStreamImplicit
  private List<Variable> variables;

  @XStreamAsAttribute
  private DataType dataType;

  @XStreamAsAttribute
  private String unit;

  @XStreamImplicit
  private List<Category> categories;

  public Variable(String name) {
    super();
    this.name = name;
  }

  /**
   * @param name
   * @param parent
   */
  public Variable(String name, Variable parent) {
    super();
    this.name = name;
    this.parent = parent;
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

  public List<Variable> getVariables() {
    return variables != null ? variables : (variables = new ArrayList<Variable>());
  }

  public Variable addVariable(Variable child) {
    if(child != null) {
      getVariables().add(child);
      child.setParent(this);
    }
    return child;
  }

  public Variable addVariable(String path, String separator) {
    String[] names = path.split(separator);
    Variable current = this;
    for(String name : names) {
      boolean found = false;
      for(Variable child : current.getVariables()) {
        if(child.getName().equals(name)) {
          current = child;
          found = true;
          break;
        }
      }
      if(!found) {
        current = current.addVariable(new Variable(name));
      }
    }
    return current;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Variable getParent() {
    return parent;
  }

  public void setParent(Variable parent) {
    this.parent = parent;
  }

  public List<Category> getCategories() {
    return categories != null ? categories : (categories = new ArrayList<Category>());
  }

  public Variable addCategory(Category category) {
    if(category != null) {
      getCategories().add(category);
      category.setVariable(this);
    }
    return this;
  }

  public Variable addCategory(String categoryName) {
    if(categoryName != null && categoryName.length() != 0) {
      Category category = new Category(categoryName);
      getCategories().add(category);
      category.setVariable(this);
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
