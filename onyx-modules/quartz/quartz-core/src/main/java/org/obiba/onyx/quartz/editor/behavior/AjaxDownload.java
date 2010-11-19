/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.behavior;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.util.resource.IResourceStream;

/**
 *
 */
public abstract class AjaxDownload extends AbstractAjaxBehavior {

  private static final long serialVersionUID = 1L;

  /**
   * Call this method to initiate the download.
   */
  public void initiate(AjaxRequestTarget target) {
    target.appendJavascript("window.location.href='" + getCallbackUrl() + "';");

  }

  @Override
  public void onRequest() {
    getComponent().getRequestCycle().setRequestTarget(new ResourceStreamRequestTarget(getResourceStream(), getFileName()));
  }

  /**
   * @see ResourceStreamRequestTarget#getFileName()
   */
  protected String getFileName() {
    return null;
  }

  /**
   * Hook method providing the actual resource stream.
   */
  protected abstract IResourceStream getResourceStream();
}
