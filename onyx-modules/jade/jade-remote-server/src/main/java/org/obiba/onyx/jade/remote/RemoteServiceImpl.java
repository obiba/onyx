package org.obiba.onyx.jade.remote;


import org.obiba.onyx.jade.remote.RemoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteServiceImpl implements RemoteService {
  
  private static final Logger log = LoggerFactory.getLogger(RemoteServiceImpl.class);

  public String echo(String s) {
    log.info("Echoing '{}'", s);
    return s;
  }

}
