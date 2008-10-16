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
