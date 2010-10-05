/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.locale.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.extensions.ajax.markup.html.tabs.AjaxTabbedPanel;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.predicate.LocalePredicateFactory;

import com.google.common.collect.Iterables;

/**
 * Tabs of Locales and Labels Locales
 * 
 */
public class LocalesPropertiesAjaxTabbedPanel extends AjaxTabbedPanel {

  private static final long serialVersionUID = 1L;

  // this model must only be read
  private ListModel<Locale> dependantModel;

  private IModel<? extends IQuestionnaireElement> questionnaireElementModel;

  private ListModel<LocaleProperties> localePropertiesModel;

  /**
   * @param abstractReadOnlyModel
   * @param id
   * @param tabs
   */
  public LocalesPropertiesAjaxTabbedPanel(String id, ListModel<Locale> dependantModel, IModel<? extends IQuestionnaireElement> questionnaireElementModel, ListModel<LocaleProperties> localePropertiesModel) {
    super(id, new ArrayList<ITab>());
    this.dependantModel = dependantModel;
    this.localePropertiesModel = localePropertiesModel;
    this.questionnaireElementModel = questionnaireElementModel;
    initUI();
  }

  public LocalesPropertiesAjaxTabbedPanel(String id, IModel<? extends IQuestionnaireElement> questionnaireElementModel, ListModel<LocaleProperties> localePropertiesModel) {
    super(id, new ArrayList<ITab>());
    this.questionnaireElementModel = questionnaireElementModel;
    this.localePropertiesModel = localePropertiesModel;
    initUI();
  }

  public void initUI() {
    getTabs().clear();
    if(localePropertiesModel.getObject().size() != 0) {
      for(LocaleProperties localeProperties : localePropertiesModel.getObject()) {
        LocalePropertiesTab localePropertiesTab = new LocalePropertiesTab(localeProperties);
        getTabs().add(localePropertiesTab);
      }
      setSelectedTab(getTabs().size() - 1);
    }
  }

  /**
   * Called to update UI (only for questionnaire) (maybe create 2 different classes for this)
   */
  public void dependantModelChanged() {
    final List<Locale> listSelectedLocale = dependantModel.getObject();
    int listSelectedLocaleSize = listSelectedLocale.size();
    int listLocalePropertiesSize = localePropertiesModel.getObject().size();

    // remove a tab
    if(listSelectedLocaleSize < listLocalePropertiesSize) {
      Iterable<LocaleProperties> localePropertiesToRemove = Iterables.filter(localePropertiesModel.getObject(), LocalePredicateFactory.newLocalePredicateFilter(listSelectedLocale));
      localePropertiesModel.getObject().removeAll(Arrays.asList(Iterables.toArray(localePropertiesToRemove, LocaleProperties.class)));
      Iterable<ITab> tabToDelete = Iterables.filter(getTabs(), LocalePredicateFactory.newLocalePredicateTabRemover(listSelectedLocale));
      getTabs().removeAll(Arrays.asList(Iterables.toArray(tabToDelete, ITab.class)));

      // not exactly this to do
      if(getTabs().size() != 0) {
        setSelectedTab(getTabs().size() - 1);
      }
    }
    // add a tab
    else if(listSelectedLocaleSize > listLocalePropertiesSize) {
      for(int i = listLocalePropertiesSize; i < listSelectedLocaleSize; i++) {
        final Locale locale = listSelectedLocale.get(i);
        LocaleProperties localeProperties = new LocaleProperties(locale, questionnaireElementModel);
        LocalePropertiesTab localePropertiesTab = new LocalePropertiesTab(localeProperties);
        localePropertiesModel.getObject().add(localeProperties);
        getTabs().add(localePropertiesTab);
      }
      setSelectedTab(getTabs().size() - 1);
    }
  }
}
