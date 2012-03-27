/*
 * ***************************************************************************
 *  Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *  <p/>
 *  This program and the accompanying materials
 *  are made available under the terms of the GNU Public License v3.0.
 *  <p/>
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  ****************************************************************************
 */
package org.obiba.onyx.quartz.editor.openAnswer.autocomplete;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerSuggestionUtils;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties.KeyValue;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.quartz.editor.utils.SaveablePanel;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class SuggestionItemWindow extends Panel implements SaveablePanel {

  private final transient Logger logger = LoggerFactory.getLogger(getClass());

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  public SuggestionItemWindow(String id, final IModel<OpenAnswerDefinition> model, IModel<String> itemModel,
      IModel<Questionnaire> questionnaireModel, final ModalWindow modalWindow) {
    super(id, model);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    final Form<OpenAnswerDefinition> form = new Form<OpenAnswerDefinition>("form", model);
    form.setMultiPart(false);
    add(form);

    final TextField<String> name = new TextField<String>("name", itemModel);
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    form.add(name).add(new SimpleFormComponentLabel("nameLabel", name));

    final String itemOriginal = itemModel.getObject();
    final OpenAnswerDefinition openAnswerDefinition = model.getObject();
    final LocaleProperties localeProperties = new LocaleProperties();
    localeProperties.setLocales(new ArrayList<Locale>(questionnaireModel.getObject().getLocales()));
    for(Locale locale : localeProperties.getLocales()) {
      ListMultimap<Locale, KeyValue> map = ArrayListMultimap.create();
      String label = OpenAnswerSuggestionUtils.getSuggestionLabel(openAnswerDefinition, locale, itemOriginal);
      map.put(locale, new KeyValue("label", StringUtils.equals(itemOriginal, label) ? null : label));
      localeProperties.addElementLabel(openAnswerDefinition, map);
    }

    LabelsPanel labelsPanel = new LabelsPanel("labels", new Model<LocaleProperties>(localeProperties), model,
        feedbackPanel, feedbackWindow);
    form.add(labelsPanel);

    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form1) {
        logger.info("localeProperties: {}", localeProperties.getElementLabels());
        if(form.hasError()) return;

        String newItem = name.getModelObject();
        if(!StringUtils.equals(itemOriginal, newItem)) {
          OpenAnswerSuggestionUtils.removeSuggestionItem(openAnswerDefinition, itemOriginal);
          OpenAnswerSuggestionUtils.addSuggestionItem(openAnswerDefinition, newItem);
        }
        ListMultimap<Locale, KeyValue> elementLabels = localeProperties.getElementLabels(openAnswerDefinition);
        for(Map.Entry<Locale, KeyValue> entry : elementLabels.entries()) {
          OpenAnswerSuggestionUtils.setSuggestionLabel(openAnswerDefinition, entry.getKey(), newItem,
              entry.getValue().getValue());
        }
        logger.info("UIArguments: {}", openAnswerDefinition.getUIArgumentsValueMap());
        SuggestionItemWindow.this.onSave(target);
        modalWindow.close(target);
      }

      @Override
      protected void onCancel(AjaxRequestTarget target, Form<?> form1) {
        modalWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });
  }

}
