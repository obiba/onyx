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

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * Strategy for naming the path to the variables, their root and parameters.
 */
public interface IVariablePathNamingStrategy {

  /**
   * Get the name of variable root.
   * @return
   */
  public String getRootName();

  /**
   * Get the path to the variable.
   * @param variable
   * @return
   */
  public String getPath(Variable variable);

  /**
   * Get the path to the variable, with parameters.
   * @param variable
   * @param key
   * @param value
   * @return
   */
  public String getPath(Variable variable, String key, String value);

  /**
   * Add parameter (key/value pair) to path.
   * @param path
   * @param key
   * @param value
   * @return
   */
  public String addParameter(String path, String key, String value);

  /**
   * Get the path from DOM representation of the variable (assuming 'name' attribute).
   * @param variableNode
   * @return
   */
  public String getPath(Node variableNode);

  /**
   * Get the separator of entity names.
   * @return
   */
  public String getPathSeparator();

  /**
   * Normalize the name: for instance change spaces to underscores, capitalize letters etc.
   * @param name
   * @return
   */
  public String normalizeName(String name);

  /**
   * Split the path, remove the parameters and return the path elements which are the normalized names of the variable
   * path.
   * @param path
   * @return
   */
  public List<String> getNormalizedNames(String path);

  /**
   * Get the parameters from the path.
   * @param path
   * @return
   */
  public Map<String, String> getParameters(String path);

}
