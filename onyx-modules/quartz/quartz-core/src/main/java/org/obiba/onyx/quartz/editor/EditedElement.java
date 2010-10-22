/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties2;

public class EditedElement<T extends IQuestionnaireElement> implements Serializable {

  private static final long serialVersionUID = 1L;

  private T element;

  protected List<LocaleProperties2> localeProperties = new ArrayList<LocaleProperties2>();

  public EditedElement(T element) {
    this.element = element;
  }

  public T getElement() {
    return element;
  }

  public void setElement(T element) {
    this.element = element;
  }

  public void setLocalePropertiesWithNamingStrategy(List<LocaleProperties2> localeProperties) {
    this.localeProperties = localeProperties;
    DefaultPropertyKeyProviderImpl defaultPropertyKeyProviderImpl = new DefaultPropertyKeyProviderImpl();
    for(LocaleProperties2 localeProperty : this.localeProperties) {
      String[] keysWithNamingStrategy = new String[localeProperty.getKeysValues().length];
      for(int i = 0; i < localeProperty.getKeysValues().length; i++) {
        String key = localeProperty.getKeysValues()[i].getKey();
        keysWithNamingStrategy[i] = defaultPropertyKeyProviderImpl.getPropertyKey(element, key);
        localeProperty.getKeysValues()[i].setFullKey(keysWithNamingStrategy[i]);
      }
    }
  }

  public Map<Locale, Properties> getPropertiesByLocale() {
    Map<Locale, Properties> propertiesByLocale = new HashMap<Locale, Properties>();
    for(LocaleProperties2 localeProp : localeProperties) {
      Properties properties = new Properties();
      for(int i = 0; i < localeProp.getKeysValues().length; i++) {
        String fullKey = localeProp.getKeysValues()[i].getFullKey();
        String value = localeProp.getKeysValues()[i].getValue();
        properties.setProperty(fullKey, value != null ? value : "");
      }
      propertiesByLocale.put(localeProp.getLocale(), properties);
    }
    return propertiesByLocale;
  }
}
