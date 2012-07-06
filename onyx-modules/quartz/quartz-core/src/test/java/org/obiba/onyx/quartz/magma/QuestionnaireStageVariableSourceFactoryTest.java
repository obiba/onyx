package org.obiba.onyx.quartz.magma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Attribute;
import org.obiba.magma.Category;
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

    qrb.inPage("P1").withQuestion("QARRAY");
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

    Map<String, Variable> mv = new HashMap<String, Variable>();
    Map<String, Category> mc = new HashMap<String, Category>();
    for(VariableValueSource vvs : qsvs.createSources()) {
      Variable variable = vvs.getVariable();
      mv.put(variable.getName(), variable);
      for(Category category : variable.getCategories()) {
        mc.put(variable.getName() + "." + category.getName(), category);
      }
    }

    //Variable
    // QLISTRADIO assertion
    assertEquals("attrQuestionValue", value(mv.get("QLISTRADIO").getAttribute("attrQuestionKey")));
    assertEquals("attrQuestionValue", value(mv.get("QLISTRADIO.C1").getAttribute("attrQuestionKey")));

    assertEquals("attrOAValue", value(mv.get("QLISTRADIO.C1.OAD1").getAttribute("attrOAKey")));
    assertEquals("attrQuestionValue", value(mv.get("QLISTRADIO.C2").getAttribute("attrQuestionKey")));

    //QARRAY assertion
    assertEquals("attrParentQuestionValue", value(mv.get("QARRAY").getAttribute("attrParentQuestionKey")));
    assertEquals("attrParentQuestionValue", value(mv.get("QARRAY.Q1").getAttribute("attrParentQuestionKey")));
    assertEquals("attrParentQuestionValue", value(mv.get("QARRAY.Q2").getAttribute("attrParentQuestionKey")));
    assertEquals("attrParentQuestionValue", value(mv.get("QARRAY.Q1.CAT3").getAttribute("attrParentQuestionKey")));
    assertEquals("attrParentQuestionValue", value(mv.get("QARRAY.Q1.CAT4").getAttribute("attrParentQuestionKey")));
    assertEquals("attrParentQuestionValue", value(mv.get("QARRAY.Q2.CAT4").getAttribute("attrParentQuestionKey")));
    assertEquals("attrParentQuestionValue", value(mv.get("QARRAY.Q2.CAT4").getAttribute("attrParentQuestionKey")));

    //Categories
    assertEquals("attrQuestionValue", value(mc.get("QLISTRADIO.C1").getAttribute("attrQuestionKey")));
    assertEquals("attrQuestionValue", value(mc.get("QLISTRADIO.C2").getAttribute("attrQuestionKey")));
    assertEquals("attrCategoryValue", value(mc.get("QLISTRADIO.C2").getAttribute("attrCategoryKey")));

    assertEquals("attrParentQuestionValue", value(mc.get("QARRAY.Q1.CAT3").getAttribute("attrParentQuestionKey")));
    assertEquals("attrParentQuestionValue", value(mc.get("QARRAY.Q1.CAT4").getAttribute("attrParentQuestionKey")));
    assertEquals("attrParentQuestionValue", value(mc.get("QARRAY.Q2.CAT3").getAttribute("attrParentQuestionKey")));
    assertEquals("attrParentQuestionValue", value(mc.get("QARRAY.Q2.CAT3").getAttribute("attrParentQuestionKey")));

  }

  private Object value(Attribute attribute) {
    return attribute.getValue().getValue();
  }
}
