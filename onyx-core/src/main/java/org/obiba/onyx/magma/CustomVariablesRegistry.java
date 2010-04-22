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

import java.util.Set;

import org.obiba.magma.VariableValueSource;

/**
 *
 */
public interface CustomVariablesRegistry {

  /**
   * Returns custom variables belonging to the specified value table.
   * 
   * @param valueTableName value table name
   * @return custom variable in the specified value table (or an empty set if none)
   */
  public Set<VariableValueSource> getVariables(String valueTableName);
}
