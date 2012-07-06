package org.obiba.onyx.quartz.magma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.QuestionBuilder;
import org.obiba.onyx.util.data.DataType;

import static junit.framework.Assert.assertEquals;

public class QuestionnaireStageVariableSourceFactoryTest {

  @Before
  public void start() {
    new MagmaEngine().extend(new MagmaJsExtension()).extend(new MagmaXStreamExtension());
  }

  @After
  public void stop() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testAttributes() {
    Stage stageMock = EasyMock.createMock(Stage.class);
    QuestionnaireBundle questionnaireBundleMock = EasyMock.createMock(QuestionnaireBundle.class);
    QuestionnaireBuilder qrb = QuestionnaireBuilder.createQuestionnaire("mockQuestionnaireName", "1.0");

    QuestionBuilder questionBuilder = qrb.withSection("S1").withPage("P1").withQuestion("QLISTRADIO");
    questionBuilder.addAttribute("", "attrQuestionKey", "attrQuestionValue", null);
    questionBuilder.withCategory("C1").withOpenAnswerDefinition("OAD1", DataType.TEXT)
        .addAttribute("", "attrOAKey", "attrOAValue", null);
    questionBuilder.withCategory("C2").addAttribute("", "attrCategoryKey", "attrCategoryValue", null);

    QuestionBuilder aqb = qrb.inPage("P1").withQuestion("QARRAY");
    qrb.inQuestion("QARRAY").withQuestion("Q1");
    qrb.inQuestion("QARRAY").withQuestion("Q2");
    qrb.inQuestion("QARRAY").withCategories("CAT3", "CAT4");
    qrb.inQuestion("QARRAY").addAttribute("", "attrParentQuestionKey", "attrParentQuestionValue", null);

    EasyMock.expect(stageMock.getName()).andStubReturn("mockNameStage");
    EasyMock.expect(questionnaireBundleMock.getAvailableLanguages()).andStubReturn(new ArrayList<Locale>());
    EasyMock.expect(questionnaireBundleMock.getQuestionnaire()).andStubReturn(qrb.getQuestionnaire());
    EasyMock.replay(stageMock, questionnaireBundleMock);

    QuestionnaireStageVariableSourceFactory qsvs = new QuestionnaireStageVariableSourceFactory(stageMock,
        questionnaireBundleMock);

    Map<String, Variable> map = new HashMap<String, Variable>();
    for(VariableValueSource vvs : qsvs.createSources()) {
      map.put(vvs.getVariable().getName(), vvs.getVariable());
      System.out.println(vvs.getVariable().getName());
    }

    // QLISTRADIO assertion
    assertEquals("attrQuestionValue", map.get("QLISTRADIO").getAttribute("attrQuestionKey").getValue().getValue());
    assertEquals("attrQuestionValue", map.get("QLISTRADIO.C1").getAttribute("attrQuestionKey").getValue().getValue());
    assertEquals("attrOAValue", map.get("QLISTRADIO.C1.OAD1").getAttribute("attrOAKey").getValue().getValue());
    assertEquals("attrQuestionValue", map.get("QLISTRADIO.C2").getAttribute("attrQuestionKey").getValue().getValue());

    //QARRAY assertion
    assertEquals("attrParentQuestionValue",
        map.get("QARRAY").getAttribute("attrParentQuestionKey").getValue().getValue());
    assertEquals("attrParentQuestionValue", map.get("QARRAY.Q1").getAttribute("attrParentQuestionKey").getValue()
        .getValue());
    assertEquals("attrParentQuestionValue", map.get("QARRAY.Q2").getAttribute("attrParentQuestionKey")
        .getValue().getValue());
    assertEquals("attrParentQuestionValue", map.get("QARRAY.Q1.CAT3").getAttribute("attrParentQuestionKey")
        .getValue().getValue());
    assertEquals("attrParentQuestionValue", map.get("QARRAY.Q1.CAT3").getAttribute("attrParentQuestionKey")
        .getValue().getValue());
    assertEquals("attrParentQuestionValue", map.get("QARRAY.Q1.CAT4").getAttribute("attrParentQuestionKey")
        .getValue().getValue());
    assertEquals("attrParentQuestionValue", map.get("QARRAY.Q1.CAT4").getAttribute("attrParentQuestionKey")
        .getValue().getValue());

  }
}
