/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.instrument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.RedirectRequestTarget;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.interpolator.VariableInterpolator;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentLauncher implements Serializable {

  private static final long serialVersionUID = 7558426642051575327L;

  private static final Logger log = LoggerFactory.getLogger(InstrumentLauncher.class);

  private final Properties customProperties;

  private final String instrumentCodeBase;

  public InstrumentLauncher(InstrumentType instrument, String instrumentCodeBase) {
    super();
    this.customProperties = instrument.getProperties();
    this.instrumentCodeBase = instrumentCodeBase;
  }

  @SuppressWarnings("serial")
  public void launch() {
    if(instrumentCodeBase != null) {
      ServletContext context = ((WebApplication) RequestCycle.get().getApplication()).getServletContext();

      log.info("codeBase={}", instrumentCodeBase);
      final Properties props = new Properties();
      props.setProperty("org.obiba.onyx.remoting.url", makeUrl("remoting"));
      props.setProperty("codebaseUrl", makeUrl(instrumentCodeBase));
      props.setProperty("JSESSIONID", Session.get().getId());
      props.setProperty("jnlpPath", context.getRealPath(File.separatorChar + instrumentCodeBase + File.separatorChar + "launch.jnlp"));

      log.info("Current language is = {} getDisplayLanguage()", Session.get().getLocale().getDisplayLanguage());
      props.setProperty("locale", Session.get().getLocale().getLanguage());

      if(customProperties != null) {
        for(String key : customProperties.stringPropertyNames()) {
          props.setProperty(key, customProperties.getProperty(key));
        }
        log.info("properties={}", props);
      }

      ResourceReference jnlpReference = new ResourceReference(instrumentCodeBase + "_" + Session.get().getId() + "_" + Session.get().getLocale().getLanguage()) {

        protected Resource newResource() {
          return new JnlpResource(props);
        }
      };
      String url = RequestCycle.get().urlFor(jnlpReference).toString();
      log.info("url={}", url);
      RequestCycle.get().setRequestTarget(new RedirectRequestTarget(url));

    }
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
