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
 * A variable is either simply a variable container, or a concrete variable (meaning it defines a data type). Other
 * information such as categories or unit can be provided. Variables can be link to each other in a soft way using key
 * and references.
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

  @XStreamAsAttribute
  private Boolean multiple;

  @XStreamAsAttribute
  private String key;

  @XStreamImplicit(itemFieldName = "reference")
  private List<String> references;

  /**
   * 
   * @param name
   */
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

  /**
   * Get the data type.
   * @return
   */
  public DataType getDataType() {
    return dataType;
  }

  /**
   * Set the data type.
   * @param type
   * @return
   */
  public Variable setDataType(DataType type) {
    this.dataType = type;
    return this;
  }

  /**
   * Get if multiple data are to be expected from this variable.
   * @return
   */
  public Boolean getMultiple() {
    return multiple;
  }

  /**
   * Get if multiple data are to be expected from this variable.
   * @return
   */
  public boolean isMultiple() {
    return multiple != null ? multiple : false;
  }

  /**
   * Set if multiple data are to be expected from this variable.
   * @param multiple
   * @return this for chaining
   */
  public Variable setMultiple(Boolean multiple) {
    this.multiple = multiple;
    return this;
  }

  /**
   * Get data unit.
   * @return
   */
  public String getUnit() {
    return unit;
  }

  /**
   * Set data unit.
   * @param unit
   * @return this for chaining
   */
  public Variable setUnit(String unit) {
    this.unit = unit;
    return this;
  }

  /**
   * Get the associated key, for use in key/values parameters of variable path.
   * @return
   */
  public String getKey() {
    return key;
  }

  /**
   * Set the associated key, for use in key/values parameters of variable path.
   * @param key
   * @return this for chaining
   */
  public Variable setKey(String key) {
    this.key = key;
    // if the variable represents a key, it is expected to have multiple data
    if(key != null && key.trim().length() > 0) {
      multiple = Boolean.TRUE;
    }
    return this;
  }

  /**
   * Get the variables keys the current variable is depending on.
   * @return
   */
  public List<String> getReferences() {
    return references != null ? references : (references = new ArrayList<String>());
  }

  /**
   * Add a variable key as a reference for the current variable.
   * @param key
   */
  public void addReference(String key) {
    if(key != null) {
      getReferences().add(key);
    }
  }

  /**
   * Add a list of variable keys as references for the current variable.
   * @param keys
   */
  public void addReferences(String... keys) {
    if(keys != null) {
      for(String key : keys) {
        getReferences().add(key);
      }
    }
  }

  /**
   * Get the children variables.
   * @return
   */
  public List<Variable> getVariables() {
    return variables != null ? variables : (variables = new ArrayList<Variable>());
  }

  /**
   * Add a variable as a child of the current variable.
   * @param child
   * @return child for chaining
   */
  public Variable addVariable(Variable child) {
    if(child != null) {
      if(getVariable(child.getName()) != null) {
        throw new IllegalArgumentException("There is already a child variable in " + toString() + " with name " + child.getName() + ".");
      }
      getVariables().add(child);
      child.setParent(this);
    }
    return child;
  }

  /**
   * Get the child variable with the given name.
   * @param name
   * @return null if not found
   */
  public Variable getVariable(String name) {
    for(Variable child : getVariables()) {
      if(child.getName().equals(name)) {
        return child;
      }
    }
    return null;
  }

  /**
   * Add recursively simple variables, splitting the path with the given separator to retrieve the names.
   * @param path
   * @param separator
   * @return last created variable
   */
  public Variable addVariable(String path, String separator) {
    String[] names = path.split(separator);
    Variable current = this;
    for(String name : names) {
      Variable found = getVariable(name);
      if(found == null) {
        current = current.addVariable(new Variable(name));
      } else {
        current = found;
      }
    }
    return current;
  }

  /**
   * Get variable name.
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Set the variable name.
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  public Variable getParent() {
    return parent;
  }

  public void setParent(Variable parent) {
    this.parent = parent;
  }

  /**
   * Get the categories: the expected data values of the current variable.
   * @return
   */
  public List<Category> getCategories() {
    List<Category> categories = new ArrayList<Category>();

    for(Variable var : getVariables()) {
      if(Category.class.isInstance(var)) {
        categories.add((Category) var);
      }
    }

    return categories;
  }

  /**
   * Get the category with the given name.
   * @param name
   * @return null if not found.
   */
  public Category getCategory(String name) {
    Variable var = getVariable(name);
    if(Category.class.isInstance(var)) {
      return (Category) var;
    }
    return null;
  }

  /**
   * True if one of its sub variable is a category.
   * @return
   */
  public boolean isCategorial() {
    for(Variable var : getVariables()) {
      if(Category.class.isInstance(var)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Add a simple {@link Category}, providing only its name.
   * @param categoryName
   * @return this for chaining
   */
  public Variable addCategory(String categoryName) {
    if(categoryName != null && categoryName.length() != 0) {
      Category category = new Category(categoryName);
      getCategories().add(category);
      category.setParent(this);
    }
    return this;
  }

  /**
   * Add a simple set of {@link Category}, providing only their names.
   * @param categories
   * @return this for chaining
   */
  public Variable addCategories(String... categories) {
    if(categories != null) {
      for(String category : categories) {
        addCategory(category);
      }
    }
    return this;
  }

  @Override
  public String toString() {
    return parent != null ? parent + "." + name : name;
  }
}
