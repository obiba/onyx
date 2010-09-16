/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.openAnswerDefinition;

import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DefaultOpenAnswerDefinitionPanel;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

@SuppressWarnings("serial")
public class OpenAnswerDefinitionPropertiesPanel extends Panel {

  private final FeedbackWindow feedbackWindow;

  private final FeedbackPanel feedbackPanel;

  private final ModalWindow modalWindow;

  public OpenAnswerDefinitionPropertiesPanel(String id, IModel<OpenAnswerDefinition> model, ModalWindow modalWindow) {
    super(id, model);
    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    this.modalWindow = modalWindow;

    add(feedbackWindow);

    add(new OpenAnswerDefinitionForm("openAnswerDefinitionForm", model));
  }

  public class OpenAnswerDefinitionForm extends Form<OpenAnswerDefinition> {

    public OpenAnswerDefinitionForm(String id, IModel<OpenAnswerDefinition> model) {
      super(id, model);

      TextField<String> name = new TextField<String>("name", new PropertyModel<String>(getModel(), "name"));
      name.add(new RequiredFormFieldBehavior());
      name.add(new StringValidator.MaximumLengthValidator(20));
      add(name);

      add(new DropDownChoice<DataType>("dataTypeDropDownChoice", new PropertyModel<DataType>(getModel(), "dataType"), Arrays.asList(DataType.values()), new ChoiceRenderer<DataType>()));

      TextField<String> unit = new TextField<String>("unit", new PropertyModel<String>(getModel(), "unit"));
      unit.add(new RequiredFormFieldBehavior());
      add(unit);

      final TextField<String> sizeTextFieldForUIArguments = new TextField<String>("size", new Model<String>());
      sizeTextFieldForUIArguments.setOutputMarkupPlaceholderTag(true);
      sizeTextFieldForUIArguments.setVisible(false);
      AjaxCheckBox specifySize = new AjaxCheckBox("wantSpecifySize", new Model<Boolean>(getModelObject().getUIArgumentsValueMap().get(DefaultOpenAnswerDefinitionPanel.INPUT_NB_ROWS) != null)) {

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          sizeTextFieldForUIArguments.setVisible(this.getModelObject());
          target.addComponent(sizeTextFieldForUIArguments);
        }
      };
      add(specifySize, sizeTextFieldForUIArguments);

      add(new AjaxButton("save", this) {

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          modalWindow.close(target);
        }

        @Override
        protected void onError(AjaxRequestTarget target, Form<?> form) {
          feedbackWindow.setContent(feedbackPanel);
          feedbackWindow.show(target);
        }
      });

      add(new AjaxButton("cancel", this) {

        @Override
        public void onSubmit(AjaxRequestTarget target, Form<?> form) {
          modalWindow.close(target);
        }
      }.setDefaultFormProcessing(false));
    }
  }
}
