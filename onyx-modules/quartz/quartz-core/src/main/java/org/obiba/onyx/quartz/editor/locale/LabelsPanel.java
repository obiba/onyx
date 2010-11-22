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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.PanelCachingTab;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties.KeyValue;
import org.obiba.onyx.quartz.editor.utils.tab.AjaxSubmitTabbedPanel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

import com.google.common.collect.ListMultimap;

/**
 *
 */
@SuppressWarnings("serial")
public class LabelsPanel extends Panel {

  // private transient Logger logger = LoggerFactory.getLogger(getClass());

  private final AjaxSubmitTabbedPanel tabbedPanel;

  private final IModel<? extends IQuestionnaireElement> elementModel;

  private final Map<Locale, ITab> tabByLocale = new HashMap<Locale, ITab>();

  private WebMarkupContainer tabsContainer;

  public LabelsPanel(String id, IModel<LocaleProperties> model, IModel<? extends IQuestionnaireElement> elementModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);
    this.elementModel = elementModel;
    setOutputMarkupId(true);

    LocaleProperties localeProperties = (LocaleProperties) getDefaultModelObject();
    final ListMultimap<Locale, KeyValue> elementLabels = localeProperties.getElementLabels(elementModel.getObject());
    Locale userLocale = Session.get().getLocale();

    List<ITab> tabs = new ArrayList<ITab>();
    for(final Locale locale : localeProperties.getLocales()) {
      AbstractTab tab = new AbstractTab(new Model<String>(locale.getDisplayLanguage(userLocale))) {
        @Override
        public Panel getPanel(String panelId) {
          return new InputPanel(panelId, new ListModel<KeyValue>(elementLabels.get(locale)));
        }
      };
      PanelCachingTab panelCachingTab = new PanelCachingTab(tab);
      tabs.add(panelCachingTab);
      tabByLocale.put(locale, panelCachingTab);
    }

    tabbedPanel = new AjaxSubmitTabbedPanel("tabs", feedbackPanel, feedbackWindow, tabs);
    tabbedPanel.setVisible(tabs.size() > 0);

    tabsContainer = new WebMarkupContainer("tabsContainer");
    tabsContainer.setOutputMarkupId(true);
    tabsContainer.add(tabbedPanel);

    Form<LocaleProperties> form = new Form<LocaleProperties>("form", model);
    form.setOutputMarkupId(true);
    form.add(tabsContainer);
    add(form);
  }

  public void onModelChange(AjaxRequestTarget target) {
    LocaleProperties localeProperties = (LocaleProperties) getDefaultModelObject();
    final ListMultimap<Locale, KeyValue> elementLabels = localeProperties.getElementLabels(elementModel.getObject());
    Locale userLocale = Session.get().getLocale();

    @SuppressWarnings("unchecked")
    Collection<Locale> removedLocales = CollectionUtils.subtract(tabByLocale.keySet(), localeProperties.getLocales());
    List<ITab> tabs = tabbedPanel.getTabs();
    for(Locale locale : removedLocales) {
      ITab tabToRemove = tabByLocale.get(locale);
      int selectedTabIndex = tabbedPanel.getSelectedTab();
      ITab selectedTab = tabs.get(selectedTabIndex);
      tabs.remove(tabToRemove);
      tabByLocale.remove(locale);

      if(tabToRemove != selectedTab) {
        for(int i = 0; i < tabs.size(); i++) {
          ITab tab = tabs.get(i);
          if(selectedTab == tab) {
            tabbedPanel.setSelectedTab(i);
            break;
          }
        }
      } else {
        tabbedPanel.setSelectedTab(0);
      }
    }

    for(final Locale locale : localeProperties.getLocales()) {
      if(!tabByLocale.containsKey(locale)) {
        AbstractTab tab = new AbstractTab(new Model<String>(locale.getDisplayLanguage(userLocale))) {
          @Override
          public Panel getPanel(String panelId) {
            return new InputPanel(panelId, new ListModel<KeyValue>(elementLabels.get(locale)));
          }
        };
        PanelCachingTab panelCachingTab = new PanelCachingTab(tab);
        tabByLocale.put(locale, panelCachingTab);
        tabs.add(panelCachingTab);
      }
    }
    tabbedPanel.setVisible(tabs.size() > 0);
    target.addComponent(tabsContainer);
  }

  public class InputPanel extends Panel {

    public InputPanel(String id, ListModel<KeyValue> model) {
      super(id, model);

      add(new ListView<KeyValue>("item", model) {
        @Override
        protected void populateItem(ListItem<KeyValue> item) {
          TextArea<String> textArea = new TextArea<String>("textArea", new PropertyModel<String>(item.getModel(), "value"));
          textArea.setLabel(new Model<String>(item.getModelObject().getKey()));
          item.add(textArea);
          item.add(new SimpleFormComponentLabel("label", textArea));
        }
      });
    }
  }
}
