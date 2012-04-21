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

import java.io.File;

import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

public class FileResource extends ContentTypedWebResource {

  private static final long serialVersionUID = 1L;

  private final File file;

  public FileResource(File file) {
    this(file, null);
  }

  public FileResource(File file, String contentType) {
    super(contentType);
    this.file = file;
  }

  @Override
  public IResourceStream getResourceStream() {
    return new FileResourceStream(file) {

      private static final long serialVersionUID = -6064554737433421700L;

      @Override
      public String getContentType() {
        return FileResource.this.getContentType();
      }
    };
  }

}
