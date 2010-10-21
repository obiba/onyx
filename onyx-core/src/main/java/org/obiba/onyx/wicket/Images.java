/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.image.Image;

/**
 *
 */
public class Images {

  public static Image getAddImage(String id) {
    return new Image(id, new ResourceReference(Images.class, "add.png"));
  }

  public static Image getDeleteImage(String id) {
    return new Image(id, new ResourceReference(Images.class, "delete.png"));
  }

  public static Image getEditImage(String id) {
    return new Image(id, new ResourceReference(Images.class, "edit.png"));
  }

}
