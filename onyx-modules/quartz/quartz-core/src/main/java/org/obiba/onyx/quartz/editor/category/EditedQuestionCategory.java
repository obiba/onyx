/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.category;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.editor.EditedElement;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class EditedQuestionCategory extends EditedElement<QuestionCategory> {

  private static final long serialVersionUID = 1L;

  public EditedQuestionCategory(QuestionCategory element) {
    super(element);
  }

  /**
   * add category properties to question category properties
   * @param localePropertiesArg
   */
  public void mergeCategoriesPropertiesWithNamingStrategy(List<LocaleProperties> localePropertiesArg) {
    DefaultPropertyKeyProviderImpl defaultPropertyKeyProviderImpl = new DefaultPropertyKeyProviderImpl();
    for(final LocaleProperties localeProperty : localePropertiesArg) {
      LocaleProperties find = Iterables.find(this.localeProperties, new Predicate<LocaleProperties>() {

        @Override
        public boolean apply(LocaleProperties input) {
          return input.getLocale().equals(localeProperty.getLocale());
        }
      });

      String[] keysWithNamingStrategy = new String[localeProperty.getKeys().length];
      for(int i = 0; i < localeProperty.getKeys().length; i++) {
        String key = localeProperty.getKeys()[i];
        keysWithNamingStrategy[i] = defaultPropertyKeyProviderImpl.getPropertyKey(element.getCategory(), key);
      }
      localeProperty.setKeys(keysWithNamingStrategy);
      find.setKeys((String[]) ArrayUtils.addAll(find.getKeys(), localeProperty.getKeys()));
      find.setValues((String[]) ArrayUtils.addAll(find.getValues(), localeProperty.getValues()));
    }
  }
}
