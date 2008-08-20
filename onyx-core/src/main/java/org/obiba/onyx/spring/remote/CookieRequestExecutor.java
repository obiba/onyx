package org.obiba.onyx.spring.remote;

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
