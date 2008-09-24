package org.obiba.onyx.jade.core.wicket.panel;

import java.io.Serializable;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obiba.onyx.jade.core.service.InstrumentDescriptorService;
import org.obiba.onyx.jade.core.wicket.instrument.InstrumentLauncherPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@SuppressWarnings("serial")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "InstrumentLauncherPanelTest.xml")
public class InstrumentLauncherPanelTest implements Serializable {

  @Autowired
  transient WebApplication application;

  @Autowired
  transient private InstrumentDescriptorService instrumentDescriptorService;
  
  transient WicketTester tester;

  @Before
  public void setup() {
    tester = new WicketTester(application);
    instrumentDescriptorService.setCodeBase("1", "/instrument");
  }

  @SuppressWarnings("serial")
  @Test
  public void testBlah() {
    tester.startPanel(new TestPanelSource() {
      public Panel getTestPanel(String panelId) {
        return new InstrumentLauncherPanel(panelId) {

          @Override
          public void onInstrumentLaunch() {
            // TODO Auto-generated method stub
            
          }
          
        };
      }
    });
    tester.dumpPage();
//    FormTester formTester = tester.newFormTester("panel:form");
//    formTester.submit();
//    tester.assertNoErrorMessage();
  }
}
