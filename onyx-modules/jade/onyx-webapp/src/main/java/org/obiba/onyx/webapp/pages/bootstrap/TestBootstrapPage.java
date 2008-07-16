package org.obiba.onyx.webapp.pages.bootstrap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.interpolator.VariableInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestBootstrapPage extends WebPage {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(TestBootstrapPage.class);

  /**
   * Constructor that is invoked when page is invoked without a session.
   * 
   * @param parameters Page parameters
   */
  public TestBootstrapPage(final PageParameters parameters) {

    final ServletContext context = ((WebApplication) RequestCycle.get().getApplication()).getServletContext();

    Properties props = new Properties();
    props.setProperty("baseServiceUrl", makeUrl("remoting"));
    props.setProperty("codebaseUrl", makeUrl("clients/interface-bioimpedance-1.0-M1-SNAPSHOT-zip"));
    props.setProperty("jnlpPath", context.getRealPath("/clients/interface-bioimpedance-1.0-M1-SNAPSHOT-zip/launch.jnlp"));
    add(new ResourceLink("startTbf310", new JnlpResource(props)));

    props = new Properties();
    props.setProperty("baseServiceUrl", makeUrl("remoting"));
    props.setProperty("codebaseUrl", makeUrl("clients/interface-ecg-1.0-M1-SNAPSHOT-zip"));
    props.setProperty("jnlpPath", context.getRealPath("/clients/interface-ecg-1.0-M1-SNAPSHOT-zip/launch.jnlp"));
    add(new ResourceLink("startCardiosoft", new JnlpResource(props)));

  }

  private class JnlpResource extends WebResource {

    private static final long serialVersionUID = 5200797507230675946L;

    private Properties jnlpProps;

    public JnlpResource(Properties jnlpProps) {
      this.jnlpProps = jnlpProps;
      setCacheable(false);
    }

    @Override
    public IResourceStream getResourceStream() {

      String jnlpData;
      try {
        jnlpData = Streams.readString(new FileInputStream(jnlpProps.getProperty("jnlpPath")), "UTF-8");
        jnlpData = new PropertiesVariableInterpolator(jnlpData, jnlpProps).toString();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jnlpProps.storeToXML(baos, null, "UTF-8");
        jnlpData = jnlpData.replace("__config__", baos.toString("UTF-8"));
      } catch(Exception e) {
        throw new RuntimeException(e);
      }
      log.info("JNLP File: {}", jnlpData);
      return new Utf8ByteArrayResourceStream(jnlpData);
    }
  }

  private String makeUrl(String path) {
    WebRequest wr = (WebRequest) RequestCycle.get().getRequest();
    return wr.getHttpServletRequest().getRequestURL().append(path).toString();
  }

  private class PropertiesVariableInterpolator extends VariableInterpolator {
    Properties props;

    public PropertiesVariableInterpolator(String string, Properties props) {
      super(string);
      this.props = props;
    }

    @Override
    protected String getValue(String variableName) {
      return props.getProperty(variableName);
    }
  }

  private class Utf8ByteArrayResourceStream extends AbstractResourceStream {

    private static final long serialVersionUID = -4446535648749071250L;

    String data;

    InputStream in;

    public Utf8ByteArrayResourceStream(String s) {
      data = s;
    }

    public void close() throws IOException {
      if(in != null) in.close();
      in = null;
    }

    public InputStream getInputStream() throws ResourceStreamNotFoundException {
      try {
        return in = new ByteArrayInputStream(data.getBytes("UTF-8"));
      } catch(UnsupportedEncodingException e) {
        throw new ResourceStreamNotFoundException(e);
      }
    }

    @Override
    public String getContentType() {
      return "application/x-java-jnlp-file";
    }

  }
}
