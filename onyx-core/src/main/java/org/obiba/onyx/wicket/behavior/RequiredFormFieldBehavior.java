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
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;

public class RequiredFormFieldBehavior extends AttributeAppender {

  private static final long serialVersionUID = -547194106510259211L;

  public RequiredFormFieldBehavior() {
    super("class", new Model("required"), " ");
  }

  @Override
  public void bind(Component component) {
    FormComponent formComp = (FormComponent) component;
    formComp.setRequired(true);
    super.bind(component);
  }

};
