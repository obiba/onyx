package org.obiba.onyx.jade.instrument.holologic;

public class DicomSettings {

  private String aeTitle;

  private String hostname;

  private int port;

  private int stgCmtPort;

  public String getAeTitle() {
    return aeTitle;
  }

  public void setAeTitle(String aeTitle) {
    this.aeTitle = aeTitle;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public int getStgCmtPort() {
    return stgCmtPort;
  }

  public void setStgCmtPort(int stgCmtPort) {
    this.stgCmtPort = stgCmtPort;
  }

}
