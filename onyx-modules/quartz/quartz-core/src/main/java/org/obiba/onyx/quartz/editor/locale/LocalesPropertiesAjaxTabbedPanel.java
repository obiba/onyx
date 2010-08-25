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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.AbstractReadOnlyModel;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 */
public class LocalesPropertiesAjaxTabbedPanel extends AjaxTabbedPanel {

  private static final long serialVersionUID = 1L;

  private AbstractReadOnlyModel<List<Locale>> readOnlyModel;

  private Map<Locale, Properties> mapLocaleProperties;

  /**
   * @param abstractReadOnlyModel
   * @param id
   * @param tabs
   */
  public LocalesPropertiesAjaxTabbedPanel(String id, AbstractReadOnlyModel<List<Locale>> readOnlyModel) {
    super(id, new ArrayList<ITab>());
    mapLocaleProperties = new HashMap<Locale, Properties>();
    this.readOnlyModel = readOnlyModel;
  }

  public void fireModelChanged() {
    final List<Locale> listSelectedLocale = readOnlyModel.getObject();
    int readOnlyModelSize = listSelectedLocale.size();
    int mapSize = mapLocaleProperties.size();

    if(readOnlyModelSize < mapSize) {
      int nbDeleted = 0;
      for(ITab tab : getTabs()) {
        LocalePropertiesTab localePropertiesTab = (LocalePropertiesTab) tab;
        if(!listSelectedLocale.contains(localePropertiesTab.getLocale())) {
          mapLocaleProperties.remove(localePropertiesTab.getLocale());
          nbDeleted++;
        }
      }

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
        if(!mapLocaleProperties.containsKey(locale)) {
          mapLocaleProperties.put(locale, new Properties());
          LocalePropertiesTab localePropertiesTab = new LocalePropertiesTab(locale);
          getTabs().add(localePropertiesTab);
          setSelectedTab(getTabs().size() - 1);
        }
      }
    }
  }
}
