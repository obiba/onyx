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

import org.apache.commons.lang.WordUtils;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.obiba.magma.ValueType;

/**
 *
 */
public class ValueTypeRenderer implements IChoiceRenderer<ValueType> {

  private static final long serialVersionUID = 1L;

  @Override
  public String getIdValue(ValueType valueType, int index) {
    return valueType.getName();
  }

  @Override
  public Object getDisplayValue(ValueType valueType) {
    return WordUtils.capitalize(valueType.getName());
  }
}