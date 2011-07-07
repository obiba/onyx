package org.obiba.onyx.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.model.Model;

public class TabOnKeyPressBehaviour extends AttributeAppender {

  private static final long serialVersionUID = 166184606547790618L;

  public TabOnKeyPressBehaviour(Component component) {
    super("onkeydown", true, new Model("return tabSubmitForm(event, '" + component.getMarkupId() + "');"), " ");
  }

  public void renderHead(IHeaderResponse response) {
    response.renderJavascriptReference(new JavascriptResourceReference(TabOnKeyPressBehaviour.class, "tabOnKeyPress.js"));
  }
}
