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

  public static final String DEFAULT_CONFIG = "{ 'lineNumbers':false, 'highlight': false, " + //
  "'prettifyBaseUrl': '" + RequestCycle.get().urlFor(new ResourceReference(SyntaxHighlighterBehavior.class, "prettify")) + "', " + //
  "'baseUrl': '" + RequestCycle.get().urlFor(new ResourceReference(SyntaxHighlighterBehavior.class, ".")) + "' }";

  public SyntaxHighlighterBehavior() {
    super("class", true, new Model<String>("highlight")); // auto detect language
  }

  public SyntaxHighlighterBehavior(IModel<String> language) {
    super("class", true, new Model<String>("language-" + language.getObject()));
  }

  @Override
  public void renderHead(IHeaderResponse response) {
    super.renderHead(response);
    response.renderJavascriptReference(new CompressedResourceReference(SyntaxHighlighterBehavior.class, "scripts/jquery.syntaxhighlighter.min.js"));
    response.renderCSSReference(new CompressedResourceReference(SyntaxHighlighterBehavior.class, "syntaxHighlighter.css"));
    response.renderJavascript("$.SyntaxHighlighter.init(" + DEFAULT_CONFIG + ");", "SyntaxHighlighter");
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

}
