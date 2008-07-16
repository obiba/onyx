package org.obiba.onyx.jade.core.wicket.panel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.resource.AbstractResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.interpolator.VariableInterpolator;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentLauncherPanel extends Panel {

  private static final long serialVersionUID = 2397755629651961494L;

  private static final Logger log = LoggerFactory.getLogger(InstrumentLauncherPanel.class);

  public InstrumentLauncherPanel(String id, IModel instrumentTypeModel) {
    super(id, instrumentTypeModel);

    Form form = new Form("form");
    add(form);
    form.setOutputMarkupId(true);

    form.add(new AjaxButton("start", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        log.info("Start " + InstrumentLauncherPanel.this.getModelObject() + " !");
      }

    });

    final ServletContext context = ((WebApplication) RequestCycle.get().getApplication()).getServletContext();

    InstrumentType type = (InstrumentType) instrumentTypeModel.getObject();
    List<Instrument> instruments = type.getInstruments();
    if(instruments.size() > 0) {
      Instrument instrument = instruments.get(0);

      if(instrument.getZip() != null) {
        Properties props = new Properties();
        props.setProperty("baseServiceUrl", makeUrl("remoting"));
        props.setProperty("codebaseUrl", makeUrl("clients/" + instrument.getZip()));
        props.setProperty("jnlpPath", context.getRealPath("/clients/" + instrument.getZip() + "/launch.jnlp"));

        form.add(new ResourceLink("go", new JnlpResource(props)));
      } else {
        form.add(new EmptyPanel("go"));
      }
    } else {
      form.add(new EmptyPanel("go"));
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
