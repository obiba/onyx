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

import org.w3c.dom.Node;

/**
 * 
 */
public interface IEntityPathNamingStrategy {

  /**
   * Get the name of entity root.
   * @return
   */
  public String getRootName();

  /**
   * Get the path to the entity.
   * @param entity
   * @return
   */
  public String getPath(Entity entity);

  /**
   * Get the path from DOM representation of the entity (assuming 'name' attribute).
   * @param entityNode
   * @return
   */
  public String getPath(Node entityNode);

  /**
   * Get the separator of entity names.
   * @return
   */
  public String getPathSeparator();

  /**
   * Normalize the name: for instance change spaces to underscores, capitalize letters etc.
   * @param name
   * @return
   */
  public String normalizeName(String name);

  /**
   * Get the entity from the path and a parent entity (can be itself).
   * @param entity
   * @param path
   * @return
   */
  public Entity getEntity(Entity entity, String path);

}
