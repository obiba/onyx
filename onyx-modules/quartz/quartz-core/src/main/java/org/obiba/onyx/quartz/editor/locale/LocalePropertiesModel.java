/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.locale;

import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.wicket.IClusterable;

public class LocalePropertiesModel implements IClusterable {

  private static final long serialVersionUID = 1L;

  private Locale locale;

  private List<Properties> listProperties;

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public List<Properties> getListProperties() {
    return listProperties;
  }

  public void setListProperties(List<Properties> listProperties) {
    this.listProperties = listProperties;
  }

}
