package org.obiba.onyx.wicket.util;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class JavascriptEventAlert extends AttributeModifier {

  private static final long serialVersionUID = -863765127280937942L;
  
  public JavascriptEventAlert(String event, String msg) {
    super(event, true, new Model(msg));
  }
  
  public JavascriptEventAlert(String event, IModel model) {
    super(event, true, model);
  }
  
  protected String newValue(final String currentValue, final String replacementValue) {
    String result = "return alert('" + replacementValue + "')";
    if (currentValue != null) {       
      result = currentValue + "; " + result;
    }
    return result;
  }
}
