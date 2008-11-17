/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.answer.DataSource;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class OpenAnswerDefinition implements Serializable, ILocalizable {

  private static final long serialVersionUID = -7756577128502621726L;

  private String name;

  private DataType dataType;

  private String unit;

  private List<IDataValidator> validators;

  private List<Data> defaultValues;

  private DataSource dataSource;

  private OpenAnswerDefinition parentOpenAnswerDefinition;

  private List<OpenAnswerDefinition> openAnswerDefinitions;

  public OpenAnswerDefinition(String name, DataType dataType) {
    this.name = name;
    this.dataType = dataType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public List<IDataValidator> getValidators() {
    return validators != null ? validators : (validators = new ArrayList<IDataValidator>());
  }

  public void addValidator(IDataValidator validator) {
    getValidators().add(validator);
  }

  public List<Data> getDefaultValues() {
    return defaultValues != null ? defaultValues : (defaultValues = new ArrayList<Data>());
  }

  public void addDefaultValue(String value) {
    if(value != null && value.length() > 0) {
      getDefaultValues().add(DataBuilder.build(dataType, value));
    }
  }

  public void addDefaultValue(Data data) {
    if(data != null && data.getValue() != null) {
      if(!data.getType().equals(getDataType())) {
        throw new IllegalArgumentException("Wrong data type for default value: " + getDataType() + " expected, " + data.getType() + " found.");
      }
      getDefaultValues().add(data);
    }
  }

  public void accept(IVisitor visitor) {
    visitor.visit(this);
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public String toString() {
    return getName();
  }

  public OpenAnswerDefinition getParentOpenAnswerDefinition() {
    return parentOpenAnswerDefinition;
  }

  public void setParentOpenAnswerDefinition(OpenAnswerDefinition parentOpenAnswerDefinition) {
    this.parentOpenAnswerDefinition = parentOpenAnswerDefinition;
  }

  public List<OpenAnswerDefinition> getOpenAnswerDefinitions() {
    return openAnswerDefinitions != null ? openAnswerDefinitions : (openAnswerDefinitions = new ArrayList<OpenAnswerDefinition>());
  }

  public void addOpenAnswerDefinition(OpenAnswerDefinition openAnswerDefinition) {
    if(openAnswerDefinition != null) {
      getOpenAnswerDefinitions().add(openAnswerDefinition);
      openAnswerDefinition.setParentOpenAnswerDefinition(this);
    }
  }

  public OpenAnswerDefinition findOpenAnswerDefinition(String name) {
    for(OpenAnswerDefinition openAnswerDefinition : getOpenAnswerDefinitions()) {
      if(openAnswerDefinition.getName().equals(name)) {
        return openAnswerDefinition;
      }
      if(openAnswerDefinition.getOpenAnswerDefinitions().size() > 0) return findOpenAnswerDefinition(name);
    }
    return null;
  }

}
