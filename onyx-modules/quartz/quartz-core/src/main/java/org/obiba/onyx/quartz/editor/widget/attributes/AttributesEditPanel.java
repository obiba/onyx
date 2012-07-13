/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.widget.attributes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.PanelCachingTab;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.magma.Attribute;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Attributable;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.quartz.editor.utils.tab.AjaxSubmitTabbedPanel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

public class AttributesEditPanel extends Panel {

  private TextField<String> namespaceField;

  private TextField<String> nameField;

  private final String initialNamespace;

  private final String initialName;

  public AttributesEditPanel(String id, final IModel<? extends Attributable> attributable,
      final IModel<FactorizedAttribute> fam, List<Locale> locales,
      final FeedbackPanel feedbackPanel,
      final FeedbackWindow feedbackWindow) {
    super(id);

    Form<Attribute> form = new Form<Attribute>("form");

    final FactorizedAttribute fao = fam.getObject();
    final Attributable ao = attributable.getObject();

    initialNamespace = fao.getNamespace() == null ? "" : fao.getNamespace();
    initialName = fao.getName() == null ? "" : fao.getName();

    namespaceField = new TextField<String>("namespace", new PropertyModel<String>(fam, "namespace"));
    nameField = new TextField<String>("name", new PropertyModel<String>(fam, "name"));

    namespaceField.setLabel(new ResourceModel("Namespace"));
    nameField.setLabel(new ResourceModel("Name"));

    form.add(namespaceField).add(new SimpleFormComponentLabel("namespaceLabel", namespaceField));
    form.add(nameField).add(new SimpleFormComponentLabel("nameLabel", nameField));

    Locale userLocale = Session.get().getLocale();
    List<ITab> tabs = new ArrayList<ITab>();
    AjaxSubmitTabbedPanel astp = new AjaxSubmitTabbedPanel("tabsLocale", feedbackPanel, feedbackWindow, tabs);

    AbstractTab nlTab = new AbstractTab(new ResourceModel("NoLocale")) {
      @Override
      public Panel getPanel(String panelId) {
        return new InputPanel(panelId, fao.getValues().get(null));
      }
    };
    tabs.add(new PanelCachingTab(nlTab));

    for(final Locale locale : locales) {
      AbstractTab tab = new AbstractTab(new Model<String>(locale.getDisplayLanguage(userLocale))) {
        @Override
        public Panel getPanel(String panelId) {
          return new InputPanel(panelId, fao.getValues().get(locale));
        }
      };
      tabs.add(new PanelCachingTab(tab));
    }

    add(form);
    form.add(astp);

    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form) {
        validate(form, ao, fao);
        if(form.hasError()) return;
        ao.removeAttributes(initialNamespace, initialName);
        for(Map.Entry<Locale, IModel<String>> entry : fao.getValues().entrySet()) {
          if(Strings.isNullOrEmpty(entry.getValue().getObject()) == false) {
            ao.addAttribute(fao.getNamespace(), fao.getName(), entry.getValue().getObject(), entry.getKey());
          }
        }
        Dialog.closeCurrent(target);
      }

      @Override
      protected void onCancel(AjaxRequestTarget target, Form<?> form) {
        Dialog.closeCurrent(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });
  }

  private void validate(Form<?> form, Attributable ao, FactorizedAttribute famo) {
    if(Strings.isNullOrEmpty(nameField.getValue())) {
      form.error(new StringResourceModel("FieldNameRequired", this, null).getObject());
    }

    boolean allEmpty = Iterables.all(famo.getValues().values(), new Predicate<IModel<String>>() {
      @Override
      public boolean apply(@Nullable IModel<String> input) {
        return Strings.isNullOrEmpty(input.getObject());
      }
    });
    if(allEmpty) {
      form.error(new StringResourceModel("MustProvideValue", this, null).getObject());
    }

    if((namespaceField.getValue().equals(initialNamespace)
        && nameField.getValue().equals(initialName)) == false) {
      if(ao.containsAttribute(namespaceField.getValue(), nameField.getValue())) {
        form.error(new StringResourceModel("AttributeAlreadyExists", this, null).getObject());
      }
    }
  }

  private class InputPanel extends Panel {

    public InputPanel(String id, IModel<String> valueModel) {
      super(id, valueModel);
      TextArea<String> valueField = new TextArea<String>("value", valueModel);
      add(valueField);
    }
  }
}
