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

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.obiba.magma.Attribute;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;

public class Category implements Serializable, IQuestionnaireElement, Attributable {

  private static final long serialVersionUID = -1722883141794376906L;

  private String name;

  private boolean escape;

  private OpenAnswerDefinition openAnswerDefinition;

  private Map<String, String> variableNames;

  private boolean noAnswer;

  @Nullable
  private List<Attribute> attributes;

  public boolean isNoAnswer() {
    return noAnswer;
  }

  public void setNoAnswer(boolean noAnswer) {
    this.noAnswer = noAnswer;
  }

  public Category() {
  }

  public Category(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isEscape() {
    return escape;
  }

  public void setEscape(boolean escape) {
    this.escape = escape;
  }

  public OpenAnswerDefinition getOpenAnswerDefinition() {
    return openAnswerDefinition;
  }

  public void setOpenAnswerDefinition(OpenAnswerDefinition openAnswerDefinition) {
    this.openAnswerDefinition = openAnswerDefinition;
  }

  public Map<String, OpenAnswerDefinition> getOpenAnswerDefinitionsByName() {
    Map<String, OpenAnswerDefinition> map = new HashMap<String, OpenAnswerDefinition>();
    if(openAnswerDefinition != null) {
      map.put(openAnswerDefinition.getName(), openAnswerDefinition);
      for(OpenAnswerDefinition child : openAnswerDefinition.getOpenAnswerDefinitions()) {
        map.put(child.getName(), child);
      }
    }
    return map;
  }

  @Override
  public void accept(IVisitor visitor) {
    visitor.visit(this);
  }

  public boolean hasDataSource() {
    if(getOpenAnswerDefinition() != null) {
      if(getOpenAnswerDefinition().getDataSource() != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return getName();
  }

  public OpenAnswerDefinition findOpenAnswerDefinition(String name1) {
    if(getOpenAnswerDefinition() == null) return null;
    if(getOpenAnswerDefinition().getName().equals(name1)) return getOpenAnswerDefinition();

    return getOpenAnswerDefinition().findOpenAnswerDefinition(name1);
  }

  public void addVariableName(String questionName, String variableName) {
    getVariableNames().put(questionName, variableName);
  }

  public String getVariableName(String questionName) {
    return getVariableNames().get(questionName);
  }

  public Map<String, String> getVariableNames() {
    return variableNames != null ? variableNames : (variableNames = new HashMap<String, String>());
  }

  public void clearVariableNames() {
    if(variableNames != null) variableNames.clear();
  }

  @Override
  public List<Attribute> getAttributes() {
    if(attributes == null) {
      attributes = new ArrayList<Attribute>();
    }
    return attributes;
  }

  @Override
  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  @Override
  public void addAttribute(String namespace, String name, String value, Locale locale) {
    Attributes.addAttribute(getAttributes(), namespace, name, value, locale);
  }

  @Override
  public boolean containsAttribute(String namespace, String name) {
    return Attributes.containsAttribute(getAttributes(), namespace, name);
  }

  @Override
  public void removeAttributes(String namespace, String name) {
    Attributes.removeAttributes(getAttributes(), namespace, name);
  }
}
