/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 *
 */
public class DefaultCustomVariablesRegistry implements CustomVariablesRegistry {
  //
  // Instance Variables
  //

  private Map<String, Set<VariableValueSource>> sourceMap;

  private Resource resource;

  //
  // CustomVariablesRegistry Methods
  //

  @Override
  public Set<VariableValueSource> getVariables(String valueTableName) {
    if(sourceMap == null) {
      initSourceMap();
    }

    Set<VariableValueSource> variables = sourceMap.get(valueTableName);

    return variables != null ? variables : new HashSet<VariableValueSource>();
  }

  //
  // Methods
  //

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  private XStream getXStream() {
    return MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
  }

  private void initSourceMap() {
    sourceMap = new HashMap<String, Set<VariableValueSource>>();

    if(resource.exists()) {
      try {
        XStream xstream = getXStream();
        xstream.processAnnotations(CustomVariablesGroup.class);

        ObjectInputStream ois = xstream.createObjectInputStream(resource.getInputStream());
        try {
          while(true) {
            CustomVariablesGroup group = (CustomVariablesGroup) ois.readObject();

            Set<VariableValueSource> groupSources = new HashSet<VariableValueSource>();
            for(Variable variable : group.getVariables()) {
              groupSources.add(new JavascriptVariableValueSource(variable));
            }

            sourceMap.put(group.getName(), groupSources);
          }
        } catch(EOFException e) {
          // Reached the end of the file.
        } finally {
          ois.close();
        }
      } catch(ClassNotFoundException e) {
        throw new RuntimeException(e);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  //
  // Inner Classes
  //

  @XStreamAlias(value = "valueTable")
  private static class CustomVariablesGroup {

    @XStreamAsAttribute
    private String name;

    private List<Variable> variables;

    public String getName() {
      return name;
    }

    public List<Variable> getVariables() {
      return variables;
    }
  }
}
