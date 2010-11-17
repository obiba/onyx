/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire.tree;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 */
public class JsonNode implements Serializable {

  private static final long serialVersionUID = 1L;

  private Data data;

  private String state = "open";

  private JsonNodeAttribute attr;

  private List<JsonNode> children = new ArrayList<JsonNode>();

  public JsonNodeAttribute getAttr() {
    return attr;
  }

  public void setAttr(JsonNodeAttribute attr) {
    this.attr = attr;
  }

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public List<JsonNode> getChildren() {
    return children;
  }

  public void setChildren(List<JsonNode> children) {
    this.children = children;
  }

  @Override
  public String toString() {
    try {
      StringWriter sw = new StringWriter();
      JsonGenerator gen = new JsonFactory().createJsonGenerator(sw);
      new ObjectMapper().writeValue(gen, this);
      return sw.toString();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static class Data {

    private String title;

    private String icon;

    public Data(String title, String icon) {
      this.title = title;
      this.icon = icon;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public String getIcon() {
      return icon;
    }

    public void setIcon(String icon) {
      this.icon = icon;
    }

  }

  @JsonIgnoreProperties(value = "clazz")
  public static class JsonNodeAttribute {

    private String id;

    @JsonProperty(value = "class")
    private String clazz = "";

    private String rel;

    private String title;

    public JsonNodeAttribute(String id, String rel, String title) {
      this.id = id;
      this.rel = rel;
      this.title = title;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

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
