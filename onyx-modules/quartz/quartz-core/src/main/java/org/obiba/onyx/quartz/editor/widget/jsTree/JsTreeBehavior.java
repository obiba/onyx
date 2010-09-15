/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.widget.jsTree;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;

/**
 *
 */
public class JsTreeBehavior extends AbstractDefaultAjaxBehavior {

  private static final long serialVersionUID = 1L;

  @Override
  protected void respond(AjaxRequestTarget target) {
  }

  @Override
  public void renderHead(IHeaderResponse response) {
    super.renderHead(response);
    response.renderJavascriptReference(new CompressedResourceReference(JsTreeBehavior.class, "jquery.js"));
    response.renderJavascriptReference(new CompressedResourceReference(JsTreeBehavior.class, "jquery.jstree.js"));
  }

}
