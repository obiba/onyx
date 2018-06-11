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

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.editor.behavior.syntaxHighlighter.SyntaxHighlighterBehavior;

/**
 *
 */
@SuppressWarnings("serial")
public class VariablePreview extends Panel {

  public VariablePreview(String id, final IModel<Variable> model) {
    super(id, model);

    Variable variable = model.getObject();
    add(new Label("name", variable.getName()));
    add(new Label("type", WordUtils.capitalize(variable.getValueType().getName())));
    add(new Label("repeatable", WordUtils.capitalize(BooleanUtils.toStringTrueFalse(variable.isRepeatable()))));
    Label script = new Label("script", variable.getAttributeStringValue("script"));
    script.add(new SyntaxHighlighterBehavior());
    add(script);

  }

}
