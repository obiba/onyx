/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.spring.remoting;

import java.io.IOException;

import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;

/**
 * Request executer that adds credentials as cookies in the http request.
 * @author Yannick Marcon
 *
 */
public class CookieRequestExecutor extends CommonsHttpInvokerRequestExecutor {

  private String name;

  private String value;

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  protected PostMethod createPostMethod(HttpInvokerClientConfiguration config) throws IOException {
    PostMethod method = super.createPostMethod(config);
    method.setRequestHeader("Cookie", name + "=" + value);
    return method;
  }

}
