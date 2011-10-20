/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.CaseInsensitiveMap;

/**
 * 
 */
public class XmlParticipantInput implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<Attribute> attributes = new ArrayList<Attribute>();

  private Map<String, String> attributesMap;

  public List<Attribute> getAttributes() {
    return (this.attributes != null) ? attributes : new ArrayList<Attribute>();
  }

  public Map<String, String> getAttributesMap() {
    return attributesMap;
  }

  @SuppressWarnings("unchecked")
  public void setAttributesMap() {
    attributesMap = new CaseInsensitiveMap();
    for(Attribute attribute : getAttributes()) {
      String attributeKey = attribute.getKey();
      if(!attributesMap.containsKey(attributeKey)) {
        attributesMap.put(attributeKey, attribute.getValue());
      } else {
        throw new IllegalArgumentException("Duplicate tag for field: " + attributeKey);
      }
    }
  }

  public boolean containsWhitespaceOnly() {
    for(Attribute attribute : getAttributes()) {
      if(attribute.getValue() != null && attribute.getValue().trim().length() != 0) return false;
    }
    return true;
  }

  public class Attribute {

    private String key;

    private String value;

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }
  }
}
