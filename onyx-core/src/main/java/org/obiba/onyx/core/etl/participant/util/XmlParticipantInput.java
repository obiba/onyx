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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class XmlParticipantInput implements Serializable {

  private static final long serialVersionUID = 1L;

  private List<Attribute> attributes = new ArrayList<Attribute>();

  Map<String, String> attributesMap;

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public Map<String, String> getAttributesMap() {
    return attributesMap;
  }

  public void setAttributesMap() {
    attributesMap = new HashMap<String, String>();
    for(Attribute attribute : attributes) {
      attributesMap.put(attribute.getKey().toUpperCase(), attribute.getValue());
    }
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
