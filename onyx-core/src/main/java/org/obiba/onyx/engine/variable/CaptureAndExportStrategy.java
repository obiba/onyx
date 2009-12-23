/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import java.util.Date;

/**
 * Strategy for determining an entity's capture date range (i.e., its "capture start date" and "capture end date").
 */
public interface CaptureAndExportStrategy {

  /**
   * Returns the entity type to which this strategy applies.
   * 
   * @return applicable entity type
   */
  public String getEntityType();

  /**
   * Returns the specified entity's capture start date.
   * 
   * @param entityIdentifier entity identifier
   * @return the entity's capture start date
   * @throws IllegalArgumentException if the entity does not exist
   */
  public Date getCaptureStartDate(String entityIdentifier);

  /**
   * Returns the specified entity's capture end date.
   * 
   * @param entityIdentifier entity identifier
   * @return the entity's capture end date
   * @throws IllegalArgumentException if the entity does not exist
   */
  public Date getCaptureEndDate(String entityIdentifier);

  /**
   * Indicates whether the specified entity has been exported or not (to <em>any</em> specified destination).
   * 
   * @param entityIdentifier entity identifier
   * @return <code>true</code> if exported
   */
  public boolean isExported(String entityIdentifier);

  /**
   * Indicates whether the specified entity has been exported or not to the specified destination.
   * 
   * @param entityIdentifier entity identifier
   * @param destinationName destination name
   * @return <code>true</code> if exported
   */
  public boolean isExported(String entityIdentifier, String destinationName);
}
