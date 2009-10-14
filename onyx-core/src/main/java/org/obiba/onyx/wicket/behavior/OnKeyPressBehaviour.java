package org.obiba.onyx.wicket.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;

public class OnKeyPressBehaviour extends AbstractBehavior {

  private static final long serialVersionUID = 166184606547790618L;

  private String sourceComponentId;

  private String targetComponentId;

  private int keyPressedCode;

  public OnKeyPressBehaviour(Component component, KeyPressed keyPressed) {
    targetComponentId = component.getMarkupId();
    keyPressedCode = keyPressed.code();
  }

  public void renderHead(IHeaderResponse response) {
    response.renderJavascriptReference(new JavascriptResourceReference(OnKeyPressBehaviour.class, "enterOnKeyPress.js"));
    response.renderOnLoadJavascript("$('#" + sourceComponentId + "').bind('keypress', function(e){clickOnComponentWhenKeyPressed(e, '" + targetComponentId + "','" + keyPressedCode + "')});");
  }

  @Override
  public void bind(Component component) {
    sourceComponentId = component.getMarkupId();
    super.bind(component);
  }

}
