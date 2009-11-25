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
 * Variable.BuilderVisitor for adding the "occurrenceCount" attribute to repeatable variables.
 */
public class OccurrenceCountAttributeVisitor implements BuilderVisitor {
  //
  // Instance Variables
  //

  private Object source;

  //
  // Constructors
  //

  public OccurrenceCountAttributeVisitor(Object source) {
    this.source = source;
  }

  //
  // BuilderVisitor Methods
  //

  public void visit(Builder builder) {
    OnyxAttributeHelper.addOccurrenceCountAttribute(builder, source);
  }

}
