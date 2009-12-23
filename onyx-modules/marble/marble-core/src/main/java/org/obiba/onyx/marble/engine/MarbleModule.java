/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.engine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.wicket.Component;
import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageExecutionContext;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.IVariableProvider;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.marble.core.service.ConsentService;
import org.obiba.onyx.marble.core.wicket.consent.ElectronicConsentUploadPage;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.magma.ConsentVariableValueSourceFactory;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.ImmutableSet;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;

public class MarbleModule implements Module, IVariableProvider, VariableValueSourceFactory, ApplicationContextAware {

  private static final Logger log = LoggerFactory.getLogger(MarbleModule.class);

  private static final String MODE_ATTRIBUTE = "mode";

  private static final String ACCEPTED_ATTRIBUTE = "accepted";

  private static final String LOCALE_ATTRIBUTE = "locale";

  private static final String PDF_ATTRIBUTE = "pdfForm";

  private static final String TIME_START_ATTRIBUTE = "timeStart";

  private static final String TIME_END_ATTRIBUTE = "timeEnd";

  private ApplicationContext applicationContext;

  private ConsentService consentService;

  private List<Stage> stages;

  private Map<String, String> variableToFieldMap = new HashMap<String, String>();

  public IStageExecution createStageExecution(Interview interview, Stage stage) {
    StageExecutionContext exec = (StageExecutionContext) applicationContext.getBean("stageExecutionContext");
    exec.setStage(stage);
    exec.setInterview(interview);

    AbstractStageState ready = (AbstractStageState) applicationContext.getBean("marbleReadyState");
    AbstractStageState inProgress = (AbstractStageState) applicationContext.getBean("marbleInProgressState");
    AbstractStageState completed = (AbstractStageState) applicationContext.getBean("marbleCompletedState");

    exec.addEdge(ready, TransitionEvent.START, inProgress);
    exec.addEdge(inProgress, TransitionEvent.CANCEL, ready);
    exec.addEdge(inProgress, TransitionEvent.COMPLETE, completed);
    exec.addEdge(completed, TransitionEvent.CANCEL, ready);

    exec.setInitialState(ready);

    return exec;
  }

  public String getName() {
    return "marble";
  }

  public void initialize(WebApplication application) {
    // Mount page to specific URL so it can be called from <embed> tag (submit form button).
    application.mountBookmarkablePage("/uploadConsent", ElectronicConsentUploadPage.class);
  }

  public void shutdown(WebApplication application) {
  }

  public List<Stage> getStages() {
    return stages;
  }

  public void setStages(List<Stage> stages) {
    this.stages = stages;
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setConsentService(ConsentService consentService) {
    this.consentService = consentService;
  }

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {

    VariableData varData = new VariableData(variablePathNamingStrategy.getPath(variable));

    // get participant's consent
    Consent consent = consentService.getConsent(participant.getInterview());

    if(consent != null) {
      String varName = variable.getName();
      if(varName.equals(ACCEPTED_ATTRIBUTE) && consent.isAccepted() != null) {
        varData.addData(DataBuilder.buildBoolean(consent.isAccepted()));
      } else if(varName.equals(LOCALE_ATTRIBUTE) && consent.getLocale() != null) {
        varData.addData(DataBuilder.buildText(consent.getLocale().toString()));
      } else if(varName.equals(MODE_ATTRIBUTE) && consent.getMode() != null) {
        varData.addData(DataBuilder.buildText(consent.getMode().toString()));
      } else if(varName.equals(PDF_ATTRIBUTE) && consent.getPdfForm() != null) {
        varData.addData(DataBuilder.buildBinary(new ByteArrayInputStream(consent.getPdfForm())));
      } else if(varName.equals(TIME_START_ATTRIBUTE) && consent.getTimeStart() != null) {
        varData.addData(DataBuilder.buildDate(consent.getTimeStart()));
      } else if(varName.equals(TIME_END_ATTRIBUTE) && consent.getTimeEnd() != null) {
        varData.addData(DataBuilder.buildDate(consent.getTimeEnd()));
      } else if(consent.getPdfForm() != null) {
        String key = variableToFieldMap.get(varName);
        if(key != null && getConsentField(consent, key) != null) varData.addData(DataBuilder.buildText(getConsentField(consent, key)));
      }
    }

    return varData;
  }

  public String getConsentField(Consent consent, String fieldName) {

    byte[] pdfForm = consent.getPdfForm();
    // Access PDF content with IText library.
    PdfReader reader;
    try {
      reader = new PdfReader(pdfForm);
    } catch(IOException ex) {
      throw new RuntimeException(ex);
    }

    // Get the PDF form fields.
    AcroFields form = reader.getAcroFields();
    return form.getField(fieldName);
  }

  public List<Variable> getVariables() {
    List<Variable> variables = new ArrayList<Variable>();

    for(Stage stage : stages) {
      Variable stageVariable = new Variable(stage.getName());
      variables.add(stageVariable);

      stageVariable.addVariable(new Variable(MODE_ATTRIBUTE).setDataType(DataType.TEXT));
      stageVariable.addVariable(new Variable(LOCALE_ATTRIBUTE).setDataType(DataType.TEXT));
      stageVariable.addVariable(new Variable(ACCEPTED_ATTRIBUTE).setDataType(DataType.BOOLEAN));
      stageVariable.addVariable(new Variable(PDF_ATTRIBUTE).setDataType(DataType.DATA).setMimeType("application/pdf"));
      stageVariable.addVariable(new Variable(TIME_START_ATTRIBUTE).setDataType(DataType.DATE));
      stageVariable.addVariable(new Variable(TIME_END_ATTRIBUTE).setDataType(DataType.DATE));

      for(String key : variableToFieldMap.keySet()) {
        stageVariable.addVariable(new Variable(key).setDataType(DataType.TEXT));
      }

    }

    return variables;
  }

  public void setVariableToFieldMap(String keyValuePairs) {
    variableToFieldMap.clear();
    // Get list of strings separated by the delimiter
    StringTokenizer tokenizer = new StringTokenizer(keyValuePairs, ",");
    while(tokenizer.hasMoreElements()) {
      String token = tokenizer.nextToken();
      String[] entry = token.split("=");
      if(entry.length == 2) {
        variableToFieldMap.put(entry[0].trim(), entry[1].trim());
      } else {
        log.error("Could not identify PDF field name to variable path mapping: " + token);
      }
    }
  }

  public List<Variable> getContributedVariables(Variable root, IVariablePathNamingStrategy variablePathNamingStrategy) {
    return null;
  }

  public Component getWidget(String id) {
    return null;
  }

  public boolean isInteractive() {
    return false;
  }

  public void delete(Participant participant) {
    consentService.purgeConsent(participant.getInterview());
  }

  //
  // VariableValueSourceFactory Methods
  //

  public Set<VariableValueSource> createSources() {
    ImmutableSet.Builder<VariableValueSource> sources = new ImmutableSet.Builder<VariableValueSource>();
    for(Stage stage : stages) {
      ConsentVariableValueSourceFactory factory = new ConsentVariableValueSourceFactory(stage.getName());
      factory.setVariableToFieldMap(variableToFieldMap);
      sources.addAll(factory.createSources());
    }
    return sources.build();
  }
}
