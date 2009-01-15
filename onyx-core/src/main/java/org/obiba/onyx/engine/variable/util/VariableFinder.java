/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.util;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class VariableFinder {

  private static final Logger log = LoggerFactory.getLogger(VariableFinder.class);

  public static final String ALL_VARIABLES_XPATH = "//variable[@dataType]";

  public static final String ALL_ADMIN_VARIABLES_XPATH = "//variable[@name='Admin']/descendant::variable[@dataType]";

  private Variable parent;

  private IVariablePathNamingStrategy variablePathNamingStrategy;

  /**
   * Build a variable finder, from the given variable (search will be done from its root) and a variable naming startegy
   * (to solve variable path based queries).
   * @param parent
   * @param variablePathNamingStrategy
   * @return
   */
  public static VariableFinder getInstance(Variable parent, IVariablePathNamingStrategy variablePathNamingStrategy) {
    VariableFinder finder = new VariableFinder();
    finder.parent = parent;
    finder.variablePathNamingStrategy = variablePathNamingStrategy;

    return finder;
  }

  /**
   * Find the variable having the given path.
   * @param path
   * @return null if not found
   */
  public Variable findVariable(String path) {
    if(path == null) return null;

    Variable root = getVariableRoot();

    List<String> normalizedNames = variablePathNamingStrategy.getNormalizedNames(path);

    if(normalizedNames.size() == 0) return null;
    if(!variablePathNamingStrategy.normalizeName(root.getName()).equals(normalizedNames.get(0))) return null;

    Variable current = root;
    for(int i = 1; i < normalizedNames.size(); i++) {
      String entityName = normalizedNames.get(i);
      boolean found = false;
      for(Variable child : current.getVariables()) {
        if(variablePathNamingStrategy.normalizeName(child.getName()).equals(entityName)) {
          current = child;
          found = true;
          break;
        }
      }
      if(!found) {
        current = null;
        break;
      }
    }

    return current;
  }

  /**
   * Get all the variables having the attribute key.
   * @param key
   * @return
   */
  public List<Variable> filterKeyVariables(String key) {
    return filterVariables(getKeyQuery(key));
  }

  /**
   * Get all the variables having the attribute key.
   * @param key
   * @return
   */
  public List<String> filterKey(String key) {
    return filter(getKeyQuery(key));
  }

  private String getKeyQuery(String key) {
    return "//variable[@key='" + key + "']";
  }

  /**
   * Get all the variables having the key as a reference.
   * @param key
   * @return
   */
  public List<Variable> filterReferenceVariables(String key) {
    return filterReferenceVariables(null, key);
  }

  /**
   * Get the paths of all the variables having the key as a reference.
   * @param key
   * @return
   */
  public List<String> filterReference(String key) {
    return filterReference(null, key);
  }

  /**
   * Get the variables with the given name, having the key as a reference.
   * @param name
   * @param key
   * @return
   */
  public List<Variable> filterReferenceVariables(String name, String key) {
    List<Variable> variables = new ArrayList<Variable>();

    for(Variable refering : filterVariables(getReferenceNameQuery(name))) {
      if(refering.getReferences().contains(key)) {
        variables.add(refering);
      }
    }

    return variables;
  }

  /**
   * Get the paths to variables with the given name, having the key as a reference.
   * @param name
   * @param key
   * @return
   */
  public List<String> filterReference(String name, String key) {
    List<String> variablePaths = new ArrayList<String>();

    for(String path : filter(getReferenceNameQuery(name))) {
      Variable refering = findVariable(path);
      if(refering.getReferences().contains(key)) {
        variablePaths.add(path);
      }
    }

    return variablePaths;
  }

  private String getReferenceNameQuery(String name) {
    String query = "//reference/parent::variable";
    if(name != null) {
      query += "[@name='" + name + "']";
    }
    return query;
  }

  /**
   * Get all the variable paths matching the XPath query.
   * @param xpathQuery
   * @return
   */
  public List<String> filter(String xpathQuery) {
    List<String> variablePaths = new ArrayList<String>();

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true); // never forget this!
    try {
      DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
      Document doc = builder.parse(new ByteArrayInputStream(VariableStreamer.toXML(getVariableRoot()).getBytes()));

      XPathFactory factory = XPathFactory.newInstance();
      XPath xpath = factory.newXPath();
      XPathExpression expr = xpath.compile(xpathQuery);
      Object result = expr.evaluate(doc, XPathConstants.NODESET);
      NodeList nodes = (NodeList) result;
      for(int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        String path = variablePathNamingStrategy.getPath(node);
        log.debug("filter.path={}", path);
        variablePaths.add(path);
      }

    } catch(Exception e) {
      e.printStackTrace();
    }

    return variablePaths;
  }

  /**
   * Get all the variables matching the XPath query.
   * @param xpathQuery
   * @return
   */
  public List<Variable> filterVariables(String xpathQuery) {
    List<Variable> variables = new ArrayList<Variable>();

    for(String path : filter(xpathQuery)) {
      Variable variable = findVariable(path);
      if(variable != null) {
        variables.add(variable);
      }
    }
    log.debug("filter.variables={}", variables);

    return variables;
  }

  /**
   * Get the variable paths not matching the XPath query.
   * @param xpathQuery
   * @return
   */
  public List<String> filterComplement(String xpathQuery) {
    List<String> excluded = filter(xpathQuery);
    List<String> included = new ArrayList<String>();

    for(String path : filter(ALL_VARIABLES_XPATH)) {
      if(!excluded.contains(path)) {
        included.add(path);
      }
    }

    return included;
  }

  /**
   * Get the variables not matching the XPath query.
   * @param xpathQuery
   * @return
   */
  public List<Variable> filterComplementVariables(String xpathQuery) {
    List<Variable> included = new ArrayList<Variable>();

    for(String path : filterComplement(xpathQuery)) {
      Variable variable = findVariable(path);
      if(variable != null) {
        included.add(variable);
      }
    }

    return included;
  }

  /**
   * Get the variable paths that are in the two sets (for intersecting result of two filtering queries).
   * @param set1
   * @param set2
   * @return
   */
  public List<String> filterIntersection(List<String> set1, List<String> set2) {
    List<String> intersect = new ArrayList<String>();

    for(String path : set1) {
      if(set2.contains(path)) {
        intersect.add(path);
      }
    }

    return intersect;
  }

  /**
   * Get the variable that are in the two sets (for intersecting result of two filtering queries).
   * @param set1
   * @param set2
   * @return
   */
  public List<Variable> filterIntersectionVariables(List<Variable> set1, List<Variable> set2) {
    List<Variable> intersect = new ArrayList<Variable>();

    for(Variable variable : set1) {
      if(set2.contains(variable)) {
        intersect.add(variable);
      }
    }

    return intersect;
  }

  /**
   * Get the variable root.
   * @return
   */
  public Variable getVariableRoot() {
    Variable root = parent;

    while(root.getParent() != null) {
      root = root.getParent();
    }

    return root;
  }

}
