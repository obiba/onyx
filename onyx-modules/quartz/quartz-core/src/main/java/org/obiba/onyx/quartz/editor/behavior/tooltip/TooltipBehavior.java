/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.behavior.tooltip;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

  private final String configuration;

  public TooltipBehavior(IModel<String> tooltip) {
    this(tooltip, null);
  }

  public TooltipBehavior(IModel<String> tooltip, Map<String, Object> config) {
    super("title", true, tooltip);

    if(config == null) {
      config = new HashMap<String, Object>();
    }

    if(config.isEmpty()) { // set default config
      config.put("delay", 100);
      config.put("showURL", false);
    }

    StringBuilder cfg = new StringBuilder();
    for(Entry<String, Object> entry : config.entrySet()) {
      if(cfg.length() > 0) cfg.append(", ");
      String key = entry.getKey();
      Object value = entry.getValue();
      cfg.append(key + ": ");
      if("bodyHandler".equalsIgnoreCase(key) || value instanceof Boolean || value instanceof Integer || value instanceof Long || value instanceof Double) {
        cfg.append(value);
      } else {
        cfg.append("'" + value + "'");
      }
    }
    configuration = cfg.toString();
  }

  @Override
  public void renderHead(IHeaderResponse response) {
    super.renderHead(response);
    response.renderJavascriptReference(new CompressedResourceReference(TooltipBehavior.class, "jquery.tooltip.min.js"));
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
    "  $('#" + component.getMarkupId(true) + "').tooltip({ " + configuration + " });\n" + //
    "});\n" + //
    "</script>");
  }

}
