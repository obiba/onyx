package org.obiba.onyx.opal;

import org.obiba.opal.rest.client.magma.OpalJavaClient;
import org.springframework.beans.factory.FactoryBean;

public class OpalClientFactoryBean implements FactoryBean<OpalJavaClient> {

  private String url;

  private String username;

  private String password;

  public void setUrl(String url) {
    this.url = url;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public OpalJavaClient getObject() throws Exception {
    if(url == null || url.isEmpty()) throw new IllegalStateException("Opal url cannot be empty.");

    String opalUrl = url;
    if(url.endsWith("/ws") == false || url.endsWith("/ws/") == false) {
      opalUrl = url + "/ws";
    }
    return new OpalJavaClient(opalUrl, username, password);
  }

  @Override
  public Class<OpalJavaClient> getObjectType() {
    return OpalJavaClient.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }
}
