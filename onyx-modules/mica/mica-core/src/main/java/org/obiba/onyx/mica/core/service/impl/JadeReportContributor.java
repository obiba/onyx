package org.obiba.onyx.mica.core.service.impl;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentParameter;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.mica.core.service.ModuleReportContributor;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

public class JadeReportContributor implements Serializable, ModuleReportContributor {

  private static final long serialVersionUID = 1L;

  private ActiveInterviewService activeInterviewService;

  private InstrumentRunService instrumentRunService;

  private InstrumentService instrumentService;

  public JadeReportContributor() {
    super();
  }

  public String getName() {
    return ("jade");
  }

  public boolean isExists() {
    return true;
  }

  public InputStream getReport(String language) {

    URL resource = getClass().getResource("/report/ParticipantReport_" + language + ".pdf");
    if(resource == null) {
      resource = getClass().getResource("/report/ParticipantReport_en.pdf");
    }

    PdfReader pdfReader;
    try {
      pdfReader = new PdfReader(resource.openStream());
    } catch(Exception ex) {
      throw new RuntimeException("Report to participant template cannot be read", ex);
    }

    ByteArrayOutputStream output = new ByteArrayOutputStream();
    PdfStamper stamper = null;

    // Fill report
    try {
      stamper = new PdfStamper(pdfReader, output);
      AcroFields form = stamper.getAcroFields();

      // Fill participant information
      Participant participant = activeInterviewService.getParticipant();
      setPartcipantFields(participant, form);

      // Fill with Jade contribution
      setMeasurementFields(participant, form);

      // Fill date field
      SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
      String formattedDate = formatter.format(new Date());
      form.setField("DateInterview\\.date", formattedDate);

      stamper.close();
      output.close();
      pdfReader.close();

      return new ByteArrayInputStream(output.toByteArray());

    } catch(Exception ex) {
      throw new RuntimeException("An error occured while preparing the report to participant", ex);
    }
  }

  @SuppressWarnings("unchecked")
  private void setMeasurementFields(Participant participant, AcroFields form) {

    HashMap<String, String> fieldsList = form.getFields();

    try {

      for(Entry<String, String> oneField : fieldsList.entrySet()) {

        String fieldName = oneField.getKey();

        HashMap<String, String> instrumentList = splitData(fieldName);

        for(Entry<String, String> oneInstrument : instrumentList.entrySet()) {

          if(oneInstrument != null) {

            String wInstrumentType = oneInstrument.getKey();
            String wParameterName = oneInstrument.getValue();

            InstrumentType instrumentType = instrumentService.getInstrumentType(wInstrumentType);

            if(instrumentType != null) {

              InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValue(participant, instrumentType, wParameterName);

              if(runValue != null) {

                InstrumentParameter instrumentParameter = runValue.getInstrumentParameter();

                String parameterUnit = instrumentParameter.getMeasurementUnit();

                // Set data in pdf form
                try {
                  form.setField(oneField.getKey(), runValue.getData().getValueAsString().toString() + " " + parameterUnit);
                } catch(IOException e) {
                  throw new RuntimeException("Input/Output Exception", e);
                } catch(DocumentException e) {
                  throw new RuntimeException("Document Exception", e);
                }
              }
            }
          }
        }
      }
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @SuppressWarnings("unchecked")
  private void setPartcipantFields(Object object, AcroFields form) {
    Class bean = object.getClass();

    try {
      for(PropertyDescriptor pd : Introspector.getBeanInfo(bean).getPropertyDescriptors()) {
        Object value = pd.getReadMethod().invoke(object);
        String fieldName = bean.getSimpleName() + "\\." + pd.getName();

        if(value != null) {
          form.setField(fieldName, value.toString());
        }
      }
    } catch(Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private HashMap<String, String> splitData(String string) {

    HashMap<String, String> result = new HashMap<String, String>();
    String type;
    String parameter;

    String pattern = "([a-zA-Z0-9]+\\[+[0-9]+\\]+\\.){2}";
    String field = string.replaceFirst(pattern, "");

    int fstIndex = field.indexOf("\\.");

    if(fstIndex != -1) {

      String[] list = field.split("-");
      for(int i = 0; i < list.length; i++) {
        int scdIndex = list[i].indexOf("\\.");

        type = new String();
        type = list[i].substring(0, scdIndex);

        if(scdIndex != list[i].length()) {
          int quoteIndex = list[i].indexOf("[");
          parameter = new String();

          if(quoteIndex != -1) {
            parameter = list[i].substring(scdIndex + 2, quoteIndex);

          } else {
            parameter = list[i].substring(scdIndex + 2);

          }
          if(parameter != null) {
            result.put(type, parameter);
          }
        }
      }
      return result;
    }
    return null;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public void setInstrumentService(InstrumentService instrumentService) {
    this.instrumentService = instrumentService;
  }
}