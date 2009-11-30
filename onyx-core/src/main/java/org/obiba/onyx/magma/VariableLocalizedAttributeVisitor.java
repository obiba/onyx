/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import org.obiba.magma.Variable;

/**
 * BuilderVisitor for adding localized attributes to variables.
 */
public class VariableLocalizedAttributeVisitor implements Variable.BuilderVisitor {
  //
  // Instance Variables
  //

  private OnyxAttributeHelper attributeHelper;

  private String code;

  //
  // Constructors
  //

  public VariableLocalizedAttributeVisitor(OnyxAttributeHelper attributeHelper, String code) {
    this.attributeHelper = attributeHelper;
    this.code = code;
  }

  //
  // BuilderVisitor Methods
  //

  public void visit(Variable.Builder builder) {
    attributeHelper.addLocalizedAttributes(builder, code);
  }

}
