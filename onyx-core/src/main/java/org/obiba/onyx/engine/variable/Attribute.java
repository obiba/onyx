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

import java.io.Serializable;
import java.util.Locale;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * 
 */
@XStreamAlias("attribute")
public class Attribute {

  @XStreamAsAttribute
  private String key;

  @XStreamAsAttribute
  private Locale locale;

  private Serializable value;

  public Attribute() {
    super();
  }

  public Attribute(String key, Serializable value) {
    super();
    this.key = key;
    this.value = value;
  }

  public Attribute(String key, Locale locale, Serializable value) {
    super();
    this.key = key;
    this.locale = locale;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public Locale getLocale() {
    return locale;
  }

  public Serializable getValue() {
    return value;
  }

}
