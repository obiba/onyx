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

import org.obiba.magma.Variable.Builder;
import org.obiba.magma.Variable.BuilderVisitor;

/**
 * Variable.BuilderVisitor for adding the "identifier" attribute to variables containing identifying information.
 */
public class IdentifierAttributeVisitor implements BuilderVisitor {
  //
  // Instance Variables
  //

  private String[] variableNames;

  //
  // Constructors
  //

  public IdentifierAttributeVisitor(String... variableNames) {
    this.variableNames = variableNames;
  }

  //
  // BuilderVisitor Methods
  //

  public void visit(Builder builder) {
    if(builder.isName(variableNames)) {
      OnyxAttributeHelper.addIdentifierAttribute(builder);
    }
  }

}
