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
import org.obiba.magma.Variable.Builder;

/**
 * Variable.BuilderVisitor for adding the common "stage" attribute to variables.
 */
public class StageAttributeVisitor implements Variable.BuilderVisitor {
  //
  // Constants
  //

  public static final String STAGE_ATTRIBUTE = "stage";

  //
  // Instance Variables
  //

  private String stageName;

  //
  // Constructors
  //

  public StageAttributeVisitor(String stageName) {
    this.stageName = stageName;
  }

  //
  // Variable.BuilderVisitor Methods
  //

  public void visit(Builder builder) {
    builder.addAttribute(STAGE_ATTRIBUTE, stageName);
  }

}
