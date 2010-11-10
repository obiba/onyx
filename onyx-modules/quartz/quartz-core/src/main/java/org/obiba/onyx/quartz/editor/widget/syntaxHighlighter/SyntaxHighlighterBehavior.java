/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.widget.syntaxHighlighter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.CompressedResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * @See http://www.balupton.com/sandbox/jquery-syntaxhighlighter/demo/
 */
public class SyntaxHighlighterBehavior extends AttributeModifier {

  private static final long serialVersionUID = 1L;

  private Map<String, Object> config = new HashMap<String, Object>();

  public SyntaxHighlighterBehavior(IModel<String> language) {
    super("class", true, new Model<String>(language == null ? "highlight" : "language-" + language.getObject()));

    // set default config
    config.put("lineNumbers", false);
    config.put("highlight", false);
    RequestCycle requestCycle = RequestCycle.get();
    config.put("prettifyBaseUrl", requestCycle.urlFor(new ResourceReference(SyntaxHighlighterBehavior.class, "prettify")).toString());
    config.put("baseUrl", requestCycle.urlFor(new ResourceReference(SyntaxHighlighterBehavior.class, ".")).toString());
  }

  public SyntaxHighlighterBehavior() {
    this(null); // auto detect language
  }

  @Override
  public void renderHead(IHeaderResponse response) {
    super.renderHead(response);
    response.renderJavascriptReference(new CompressedResourceReference(SyntaxHighlighterBehavior.class, "scripts/jquery.syntaxhighlighter.min.js"));
    response.renderCSSReference(new CompressedResourceReference(SyntaxHighlighterBehavior.class, "syntaxHighlighter.css"));

    StringBuilder cfg = new StringBuilder();
    for(Entry<String, Object> entry : config.entrySet()) {
      if(cfg.length() > 0) cfg.append(", ");
      cfg.append("'" + entry.getKey() + "': ");
      if(entry.getValue() instanceof Boolean) {
        cfg.append(entry.getValue());
      } else {
        cfg.append("'" + entry.getValue() + "'");
      }
    }
    response.renderJavascript("$.SyntaxHighlighter.init({ " + cfg + " });", "SyntaxHighlighter");
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
    "  $(\"#" + component.getMarkupId(true) + "\").syntaxHighlight();\n" + //
    "});\n" + //
    "</script>");
  }

  public Map<String, Object> getConfig() {
    return config;
  }

}
