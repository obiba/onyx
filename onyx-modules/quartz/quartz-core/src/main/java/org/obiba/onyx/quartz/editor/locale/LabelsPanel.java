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

import com.google.common.collect.ListMultimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.PanelCachingTab;
import org.apache.wicket.markup.html.CSSPackageResource;
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
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties.KeyValue;
import org.obiba.onyx.quartz.editor.utils.tab.AjaxSubmitTabbedPanel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
@SuppressWarnings("serial")
public class LabelsPanel extends Panel {

  // private transient Logger logger = LoggerFactory.getLogger(getClass());

  private final AjaxSubmitTabbedPanel tabbedPanel;

  private final IModel<? extends IQuestionnaireElement> elementModel;

  private final Map<Locale, ITab> tabByLocale = new HashMap<Locale, ITab>();

  private final WebMarkupContainer tabsContainer;

  private final Map<String, IModel<String>> tooltips;

  public LabelsPanel(String id, IModel<LocaleProperties> model, IModel<? extends IQuestionnaireElement> elementModel,
      FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    this(id, model, elementModel, feedbackPanel, feedbackWindow, null, null);
  }

  /**
   * @param id
   * @param model
   * @param elementModel
   * @param feedbackPanel
   * @param feedbackWindow
   * @param tooltips
   * @param visibleStates  Map with label element as key and a boolean set to true to show it or false to hide it.
   *                       Set to null to display all labels.
   */
  public LabelsPanel(String id, IModel<LocaleProperties> model, IModel<? extends IQuestionnaireElement> elementModel,
      FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow, Map<String, IModel<String>> tooltips,
      final Map<String, Boolean> visibleStates) {
    super(id, model);
    this.elementModel = elementModel;
    this.tooltips = tooltips;
    setOutputMarkupId(true);

    add(CSSPackageResource.getHeaderContribution(LabelsPanel.class, "LabelsPanel.css"));

    LocaleProperties localeProperties = (LocaleProperties) getDefaultModelObject();
    final ListMultimap<Locale, KeyValue> elementLabels = localeProperties.getElementLabels(elementModel.getObject());
    Locale userLocale = Session.get().getLocale();

    List<ITab> tabs = new ArrayList<ITab>();
    for(final Locale locale : localeProperties.getLocales()) {
      AbstractTab tab = new AbstractTab(new Model<String>(locale.getDisplayLanguage(userLocale))) {
        @Override
        public Panel getPanel(String panelId) {
          return new InputPanel(panelId, new ListModel<KeyValue>(elementLabels.get(locale)), visibleStates);
        }
      };
      ITab panelCachingTab = new PanelCachingTab(tab);
      tabs.add(panelCachingTab);
      tabByLocale.put(locale, panelCachingTab);
    }

    tabbedPanel = new AjaxSubmitTabbedPanel("tabs", feedbackPanel, feedbackWindow, tabs);
    tabbedPanel.setVisible(tabs.size() > 0);

    tabsContainer = new WebMarkupContainer("tabsContainer");
    tabsContainer.setOutputMarkupId(true);
    tabsContainer.add(tabbedPanel);

    Form<LocaleProperties> form = new Form<LocaleProperties>("form", model);
    form.setMultiPart(false);
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
            return new InputPanel(panelId, new ListModel<KeyValue>(elementLabels.get(locale)), null);
          }
        };
        ITab panelCachingTab = new PanelCachingTab(tab);
        tabByLocale.put(locale, panelCachingTab);
        tabs.add(panelCachingTab);
        if(tabs.size() == 1) tabbedPanel.setSelectedTab(0);
      }
    }
    tabbedPanel.setVisible(tabs.size() > 0);
    target.addComponent(tabsContainer);
  }

  public class InputPanel extends Panel {

    private static final long serialVersionUID = -8514120793621286201L;

    public InputPanel(String id, ListModel<KeyValue> model, final Map<String, Boolean> visibleStates) {
      super(id, model);

      add(new ListView<KeyValue>("item", model) {
        @Override
        protected void populateItem(ListItem<KeyValue> item) {
          TextArea<String> textArea = new TextArea<String>("textArea",
              new PropertyModel<String>(item.getModel(), "value"));
          String label = item.getModelObject().getKey();
          textArea.setLabel(new Model<String>(label));
          SimpleFormComponentLabel textAreaLabel = new SimpleFormComponentLabel("label", textArea);
          textArea.add(new AjaxFormComponentUpdatingBehavior("onblur") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
          });

          boolean hasTooltip = tooltips != null && tooltips.containsKey(label);
          Component tooltip = hasTooltip ? new HelpTooltipPanel("tooltip",
              tooltips.get(label)) : new WebMarkupContainer("tooltip").setVisible(false);

          item.add(textArea);
          item.add(textAreaLabel);
          item.add(tooltip);

          boolean show = true;
          if(visibleStates != null && visibleStates.containsKey(label)) {
            show = visibleStates.get(label);
          }
          textArea.setVisible(show);
          textAreaLabel.setVisible(show);
          tooltip.setVisible(hasTooltip && show);
        }
      });
    }
  }
}
