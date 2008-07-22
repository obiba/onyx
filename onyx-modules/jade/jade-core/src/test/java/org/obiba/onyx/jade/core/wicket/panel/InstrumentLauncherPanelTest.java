package org.obiba.onyx.jade.core.wicket.panel;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "")
public class InstrumentLauncherPanelTest {

  @Autowired
  WebApplication application;

  WicketTester tester;

  @Before
  public void setup() {
    tester = new WicketTester(application);
  }

  @Test
  public void testBlah() {

  }
}
