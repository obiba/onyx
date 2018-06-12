package org.obiba.onyx.quartz.core.wicket.layout.impl.array;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.tester.TestPanelSource;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

public class ArrayExampleTest {

  private WicketTester tester;

  @Before
  public void setUp() {
    tester = new WicketTester();
  }

  @Test
  public void testDataGridView() {
    tester.startPanel(new TestPanelSource() {

      private static final long serialVersionUID = 1L;

      public Panel getTestPanel(String panelId) {

        return new ArrayExample(panelId);
      }
    });

    dumpPage();
  }

  private void dumpPage() {
    tester.dumpPage();
    File dump = new File("target/" + getClass().getSimpleName() + ".html");
    try {
      if(!dump.exists()) dump.createNewFile();
      OutputStream out = new FileOutputStream(dump);
      out.write(tester.getServletResponse().getDocument().getBytes());
      out.flush();
      out.close();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
}
