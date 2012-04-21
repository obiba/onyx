/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.utils;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.StringResourceModel;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;

public class JavascriptUtils {

  /**
   * compile script and alert error to user
   * @param script
   * @param source
   * @param component
   * @param form
   */
  public static void compile(final String script, final String source, Component component, Form<?> form) {
    try {
      ContextFactory.getGlobal().call(new ContextAction() {
        @Override
        public Object run(Context cx) {
          return cx.compileString(script, source, 1, null);
        }
      });
    } catch(EvaluatorException e) {
      String errorMsg;
      if(e.columnNumber() > 0) {
        errorMsg = new StringResourceModel("BadScript.withColumn", component, null, new Object[] { e.details(), e.lineNumber(), e.columnNumber() }).getObject();
      } else {
        errorMsg = new StringResourceModel("BadScript", component, null, new Object[] { e.details(), e.lineNumber() }).getObject();
      }
      form.error(errorMsg);
    }
  }
}
