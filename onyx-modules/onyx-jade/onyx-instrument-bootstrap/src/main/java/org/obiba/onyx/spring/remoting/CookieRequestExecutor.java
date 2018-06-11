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

import org.apache.http.client.methods.HttpPost;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;

/**
 * Request executer that adds credentials as cookies in the http request.
 * @author Yannick Marcon
 *
 */
public class CookieRequestExecutor extends HttpComponentsHttpInvokerRequestExecutor {

  private String name;

  private String value;

  public void setName(String name) {
    this.name = name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  protected HttpPost createHttpPost(HttpInvokerClientConfiguration config) throws IOException {
    HttpPost post = super.createHttpPost(config);
    post.setHeader("Cookie", name + "=" + value);
    return post;
  }

}
