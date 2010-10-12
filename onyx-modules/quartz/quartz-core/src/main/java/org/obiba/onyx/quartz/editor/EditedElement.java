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
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;

/**
 *
 */
public class EditedElement<T extends IQuestionnaireElement> implements Serializable {

  private static final long serialVersionUID = 1L;

  protected T element;

  private List<LocaleProperties> localeProperties = new ArrayList<LocaleProperties>();

  public EditedElement() {
  }

  public EditedElement(T element) {
    this.element = element;
  }

  public T getElement() {
    return element;
  }

  public void setElement(T element) {
    this.element = element;
  }

  public List<LocaleProperties> getLocaleProperties() {
    return localeProperties;
  }

  public void setLocalePropertiesWithNamingStrategy(List<LocaleProperties> localeProperties) {
    this.localeProperties=localeProperties;
    DefaultPropertyKeyProviderImpl defaultPropertyKeyProviderImpl = new DefaultPropertyKeyProviderImpl();
    for(LocaleProperties localeProperty : this.localeProperties) {
      String[] keysWithNamingStrategy = new String[localeProperty.getKeys().length];
      for(int i = 0; i < localeProperty.getKeys().length; i++) {
        String key = localeProperty.getKeys()[i];
        keysWithNamingStrategy[i] = defaultPropertyKeyProviderImpl.getPropertyKey(element, key);
      }
      localeProperty.setKeys(keysWithNamingStrategy);
    }
  }

  public Map<Locale, Properties> getPropertiesByLocale() {
    Map<Locale, Properties> propertiesByLocale = new HashMap<Locale, Properties>();
    for(LocaleProperties localeProp : getLocaleProperties()) {
      Properties properties = new Properties();
      for(int i = 0; i < localeProp.getKeys().length; i++) {
        String key = localeProp.getKeys()[i];
        String value = localeProp.getValues()[i];
        properties.setProperty(key, value != null ? value : "");
      }
      propertiesByLocale.put(localeProp.getLocale(), properties);
    }
    return propertiesByLocale;
  }

}
