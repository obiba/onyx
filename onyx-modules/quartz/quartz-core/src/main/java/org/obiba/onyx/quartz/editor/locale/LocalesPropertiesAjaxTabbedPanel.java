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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Tabs of Locales and Labels Locales
 */
public class LocalesPropertiesAjaxTabbedPanel extends AjaxTabbedPanel {

  private static final long serialVersionUID = 1L;

  private AbstractReadOnlyModel<List<Locale>> dependantModel;

  private IQuestionnaireElement questionnaireElement;

  private List<LocaleProperties> listLocaleProperties;

  /**
   * @param abstractReadOnlyModel
   * @param id
   * @param tabs
   */
  public LocalesPropertiesAjaxTabbedPanel(String id, AbstractReadOnlyModel<List<Locale>> dependantModel, IQuestionnaireElement questionnaireElement, List<LocaleProperties> listLocaleProperties) {
    super(id, new ArrayList<ITab>());
    this.questionnaireElement = questionnaireElement;
    this.dependantModel = dependantModel;
    this.listLocaleProperties = listLocaleProperties;
  }

  /**
   * Called when palette model changes to apply labels locales tabs (LocalesPropertiesAjaxTabbedPanel is based on
   * Palette model)
   */
  public void dependantModelChanged() {
    final List<Locale> listSelectedLocale = dependantModel.getObject();
    int readOnlyModelSize = listSelectedLocale.size();
    int mapSize = listLocaleProperties.size();

    if(readOnlyModelSize < mapSize) {
      Iterable<LocaleProperties> localePropertiesToRemove = Iterables.filter(listLocaleProperties, new Predicate<LocaleProperties>() {

        @Override
        public boolean apply(LocaleProperties input) {
          return !listSelectedLocale.contains(input.getLocale());
        }

      });

      listLocaleProperties.removeAll(Arrays.asList(Iterables.toArray(localePropertiesToRemove, LocaleProperties.class)));

      Iterable<ITab> tabToDelete = Iterables.filter(getTabs(), new Predicate<ITab>() {

        @Override
        public boolean apply(ITab tab) {
          LocalePropertiesTab localePropertiesTab = (LocalePropertiesTab) tab;
          return !listSelectedLocale.contains(localePropertiesTab.getLocale()) ? true : false;
        }
      });
      getTabs().removeAll(Arrays.asList(Iterables.toArray(tabToDelete, ITab.class)));

      // not exactly this to do
      if(getTabs().size() != 0) setSelectedTab(getTabs().size() - 1);
    } else if(readOnlyModelSize > mapSize) {
      for(int start = mapSize; start < readOnlyModelSize; start++) {
        final Locale locale = listSelectedLocale.get(start);
        LocalePropertiesTab localePropertiesTab = new LocalePropertiesTab(new LocaleProperties(locale, questionnaireElement), listLocaleProperties);
        getTabs().add(localePropertiesTab);
        setSelectedTab(getTabs().size() - 1);
      }
    }
  }
}
