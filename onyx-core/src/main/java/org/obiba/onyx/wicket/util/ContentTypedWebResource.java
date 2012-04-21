/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.util;

import org.apache.wicket.markup.html.WebResource;

/**
 *
 */
public abstract class ContentTypedWebResource extends WebResource {

  private static final long serialVersionUID = 1L;

  private final String contentType;

  public ContentTypedWebResource(String contentType) {
    this.contentType = contentType;
  }

  public String getContentType() {
    return contentType;
  }

}
