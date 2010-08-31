/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.locale.model;

import java.util.List;
import java.util.Locale;

import org.apache.wicket.IClusterable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;

/**
 * Class representing a locale and his list of properties (listKeys and listValues)
 */
public class LocaleProperties implements IClusterable {

  private static final long serialVersionUID = 1L;

  private Locale locale;

  private String[] keys;

  private String[] values;

  /**
   * 
   * @param locale
   * @param questionnaireElement
   */
  public LocaleProperties(Locale locale, IQuestionnaireElement questionnaireElement) {
    this.locale = locale;
    List<String> listSourceKeys = new DefaultPropertyKeyProviderImpl().getProperties(questionnaireElement);
    values = new String[listSourceKeys.size()];
    keys = listSourceKeys.toArray(new String[0]);
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public String[] getKeys() {
    return keys;
  }

  public String[] getValues() {
    return values;
  }
}
