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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.obiba.magma.Attribute;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Attributable;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

public class AttributesEditPanel extends Panel {

  private TextField<String> namespaceField;

  private TextField<String> nameField;

  public AttributesEditPanel(String id, final IModel<? extends Attributable> attributable,
      final IModel<Attribute> attribute, final FeedbackPanel feedbackPanel, final FeedbackWindow feedbackWindow) {
    super(id);

    Form<Attribute> form = new Form<Attribute>("form");

    namespaceField = new TextField<String>("namespace", new Model<String>(attribute.getObject().getNamespace()));
    nameField = new TextField<String>("name", new Model<String>(attribute.getObject().getName()));
    nameField.setRequired(true);

    namespaceField.setLabel(new ResourceModel("Namespace"));
    nameField.setLabel(new ResourceModel("Name"));

    form.add(namespaceField).add(new SimpleFormComponentLabel("namespaceLabel", namespaceField));
    form.add(nameField).add(new SimpleFormComponentLabel("nameLabel", nameField));

    add(form);
    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form) {
        attributable.getObject().updateAttribute(
            attribute.getObject(),
            namespaceField.getValue(),
            nameField.getValue(),
            null,
            null);
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
}
