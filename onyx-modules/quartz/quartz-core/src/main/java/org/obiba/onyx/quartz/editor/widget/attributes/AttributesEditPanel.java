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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.obiba.magma.Attribute;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;

public class AttributesEditPanel extends Panel {

  private Attribute attribute;

  public AttributesEditPanel(String id, Attribute attribute, final ModalWindow modalWindow) {
    super(id);
    this.attribute = attribute;
    Form<Attribute> form = new Form<Attribute>("form");

    TextField<String> namespace = new TextField<String>("namespace");
    TextField<String> name = new TextField<String>("name");

    namespace.setLabel(new ResourceModel("Namespace"));
    name.setLabel(new ResourceModel("Name"));

    form.add(namespace).add(new SimpleFormComponentLabel("namespaceLabel", namespace));
    form.add(name).add(new SimpleFormComponentLabel("nameLabel", name));

    add(form);
    form.add(new SaveCancelPanel("saveCancel", form) {
      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form) {

      }

      @Override
      protected void onCancel(AjaxRequestTarget target, Form<?> form) {
        modalWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form) {
        //To change body of implemented methods use File | Settings | File Templates.
      }
    });

  }

  public AttributesEditPanel(String id, final ModalWindow modalWindow) {
    this(id, null, modalWindow);
  }
}
