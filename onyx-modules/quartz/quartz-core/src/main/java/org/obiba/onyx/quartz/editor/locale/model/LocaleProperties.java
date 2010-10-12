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
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;

/**
 * Class representing a locale and his list of properties (keys and values)
 */
public class LocaleProperties implements IClusterable {

  private static final long serialVersionUID = 1L;

  private Locale locale;

  private String[] keys;

  private String[] values;

  public LocaleProperties(Locale locale, IModel<? extends IQuestionnaireElement> questionnaireElementModel) {
    this.locale = locale;
    List<String> listSourceKeys = new DefaultPropertyKeyProviderImpl().getProperties(questionnaireElementModel.getObject());
    values = new String[listSourceKeys.size()];
    keys = listSourceKeys.toArray(new String[listSourceKeys.size()]);
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

  public void setKeys(String[] keys) {
    this.keys = keys;
  }

  public String[] getValues() {
    return values;
  }

  public void setValues(String[] values) {
    this.values = values;
  }

}
