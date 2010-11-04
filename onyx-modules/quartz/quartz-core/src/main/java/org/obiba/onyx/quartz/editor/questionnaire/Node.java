/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 */
public class Node implements Serializable {

  private static final long serialVersionUID = 1L;

  private String data;

  private String state = "open";

  private NodeAttribute attr;

  private List<Node> children = new ArrayList<Node>();

  public NodeAttribute getAttr() {
    return attr;
  }

  public void setAttr(NodeAttribute attr) {
    this.attr = attr;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public List<Node> getChildren() {
    return children;
  }

  public void setChildren(List<Node> children) {
    this.children = children;
  }

  @JsonIgnoreProperties(value = "clazz")
  public static class NodeAttribute {

    private String id;

    @JsonProperty(value = "class")
    private String clazz = "";

    private String rel;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getRel() {
      return rel;
    }

    public void setRel(String rel) {
      this.rel = rel;
    }

    public String getClazz() {
      return clazz;
    }

    public void setClazz(String clazz) {
      this.clazz = clazz;
    }
  }
}
