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
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties2;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties2.KeyValue;

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
  public void mergeCategoriesPropertiesWithNamingStrategy(List<LocaleProperties2> localePropertiesArg) {
    DefaultPropertyKeyProviderImpl defaultPropertyKeyProviderImpl = new DefaultPropertyKeyProviderImpl();
    for(final LocaleProperties2 localeProperty : localePropertiesArg) {
      LocaleProperties2 find = Iterables.find(this.localeProperties, new Predicate<LocaleProperties2>() {

        @Override
        public boolean apply(LocaleProperties2 input) {
          return input.getLocale().equals(localeProperty.getLocale());
        }
      });

      for(int i = 0; i < localeProperty.getKeysValues().length; i++) {
        String key = localeProperty.getKeysValues()[i].getKey();
        localeProperty.getKeysValues()[i].setKey(defaultPropertyKeyProviderImpl.getPropertyKey(getElement().getCategory(), key));
      }
      find.setKeysValues((KeyValue[]) ArrayUtils.addAll(find.getKeysValues(), localeProperty.getKeysValues()));
    }
  }
}
