/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.FormComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class InvalidFormFieldBehavior extends AbstractBehavior {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(InvalidFormFieldBehavior.class);

  private static final String FIELD_INVALID_CSS_CLASS = "field-invalid";

  @Override
  public void onComponentTag(Component component, ComponentTag tag) {
    super.onRendered(component);

    String cssClass = null;
    if(component instanceof FormComponent && !((FormComponent<?>) component).isValid()) {
      cssClass = FIELD_INVALID_CSS_CLASS;
    } else if(component instanceof MarkupContainer) {

      FormComponentVisitor componentVisitor = new FormComponentVisitor();
      ((MarkupContainer) component).visitChildren(FormComponent.class, componentVisitor);

      if(componentVisitor.hasFoundErrorMessages()) {
        cssClass = FIELD_INVALID_CSS_CLASS;
      }
    }

    if(cssClass != null) {
      if(tag.getAttributes().containsKey("class")) {
        cssClass += " " + tag.getAttributes().getString("class");
      }
      tag.getAttributes().put("class", cssClass);
    }

  }

  private class FormComponentVisitor implements Component.IVisitor {

    private Boolean foundErrorMessages = false;

    @Override
    public Object component(Component component) {

      FormComponent<?> formComponent = (FormComponent<?>) component;

      if(!formComponent.isValid()) {
        foundErrorMessages = true;
        return STOP_TRAVERSAL;
      }
      foundErrorMessages = false;
      return CONTINUE_TRAVERSAL;
    }

    public boolean hasFoundErrorMessages() {
      return foundErrorMessages;
    }

  }
}
