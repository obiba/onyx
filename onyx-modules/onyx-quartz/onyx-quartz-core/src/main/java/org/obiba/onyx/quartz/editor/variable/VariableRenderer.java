/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.variable;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.obiba.magma.Variable;

/**
 *
 */
public class VariableRenderer implements IChoiceRenderer<Variable> {

  private static final long serialVersionUID = 1L;

  @Override
  public Object getDisplayValue(Variable object) {
    return object.getName();
  }

  @Override
  public String getIdValue(Variable object, int index) {
    return object.getName();
  }
}
