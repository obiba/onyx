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
  public List<Variable> filterKey(String key) {
    return filter("//variable[@key='" + key + "']");
  }

  /**
   * Get all the variables having the key as a reference.
   * @param key
   * @return
   */
  public List<Variable> filterReference(String key) {
    return filterReference(null, key);
  }

  /**
   * Get the variables with the given name, having the key as a reference.
   * @param name
   * @param key
   * @return
   */
  public List<Variable> filterReference(String name, String key) {
    List<Variable> variables = new ArrayList<Variable>();

    String query = "//reference/parent::variable";
    if(name != null) {
      query += "[@name='" + name + "']";
    }
    for(Variable refering : filter(query)) {
      if(refering.getReferences().contains(key)) {
        variables.add(refering);
      }
    }

    return variables;
  }

  /**
   * Get all the variables matching the XPath query.
   * @param xpathQuery
   * @return
   */
  public List<Variable> filter(String xpathQuery) {
    List<Variable> variables = new ArrayList<Variable>();

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

        Variable variable = findVariable(path);
        if(variable != null) {
          variables.add(variable);
        }
      }

    } catch(Exception e) {
      e.printStackTrace();
    }

    log.debug("filter.variables={}", variables);

    return variables;
  }

  public List<Variable> filterExcluding(String xpathQuery) {
    List<Variable> excluded = filter(xpathQuery);
    List<Variable> included = new ArrayList<Variable>();

    for(Variable variable : filter(ALL_VARIABLES_XPATH)) {
      if(!excluded.contains(variable)) {
        included.add(variable);
      }
    }

    return included;
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
