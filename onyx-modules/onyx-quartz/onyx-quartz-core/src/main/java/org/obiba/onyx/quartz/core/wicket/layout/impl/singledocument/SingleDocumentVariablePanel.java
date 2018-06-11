/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.singledocument;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.magma.Variable;

public class SingleDocumentVariablePanel extends Panel {

  private static final long serialVersionUID = 1L;

  public SingleDocumentVariablePanel(String id, IModel<Variable> questionModel) {
    super(id, questionModel);

    Variable variable = (Variable) getDefaultModelObject();
    add(new Label("label", variable.getName()));
    add(new Label("type", "[" + variable.getValueType().getName() + "]"));
    add(new Label("script", variable.getAttributeStringValue("script")));
  }
}
