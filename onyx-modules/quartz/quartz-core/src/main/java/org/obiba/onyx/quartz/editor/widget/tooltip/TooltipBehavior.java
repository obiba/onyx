/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.widget.tooltip;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.IModel;

/**
 * Show element title as tooltip
 */
public class TooltipBehavior extends AttributeModifier {

  private static final long serialVersionUID = 1L;

  public TooltipBehavior(IModel<String> tooltip) {
    super("title", true, tooltip);
  }

  @Override
  public void renderHead(IHeaderResponse response) {
    super.renderHead(response);
    response.renderJavascriptReference(new CompressedResourceReference(TooltipBehavior.class, "jquery.tooltip.js"));
    response.renderCSSReference(new CompressedResourceReference(TooltipBehavior.class, "jquery.tooltip.css"));
  }

  @Override
  public void bind(Component component) {
    super.bind(component);
    component.setOutputMarkupId(true);
  }

  @Override
  public void onRendered(Component component) {
    super.onRendered(component);
    component.getResponse().write("<script>\n" + //
    "$(document).ready(function() {\n" + //
    "  $(\"#" + component.getMarkupId(true) + "\").tooltip();\n" + //
    "});\n" + //
    "</script>");
  }

}
