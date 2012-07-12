package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.util.Locale;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.Attribute;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.xstream.MagmaXStreamExtension;

public class QuestionAttributesTest {

  @Before
  public void start() {
    new MagmaEngine().extend(new MagmaJsExtension()).extend(new MagmaXStreamExtension());
  }

  @After
  public void stop() {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testEmptyQuestionAttributes() {
    Question q1 = new Question();
    Assert.assertNull(q1.getAttributes());
  }

  @Test
  public void testQuestionKeyAttributes() {
    Question q1 = new Question();
    q1.addAttribute(null, "attrKey", null, null);
    Attribute attribute = q1.getAttributes().get(0);
    Assert.assertEquals("attrKey", attribute.getName());
  }

  @Test
  public void testQuestionNSKeyAttributes() {
    Question q1 = new Question();
    q1.addAttribute("ns", "attrKey", null, null);
    Attribute attribute = q1.getAttributes().get(0);
    Assert.assertEquals("attrKey", attribute.getName());
    Assert.assertEquals("ns", attribute.getNamespace());
  }

  @Test
  public void testQuestionKeyValueAttributes() {
    Question q1 = new Question();
    q1.addAttribute(null, "attrKey", "attrValue", null);
    Attribute attribute = q1.getAttributes().get(0);
    Assert.assertEquals("attrKey", attribute.getName());
    Assert.assertEquals("attrValue", attribute.getValue().getValue());
  }

  @Test
  public void testQuestionKeyValueLocaleAttributes() {
    Question q1 = new Question();
    Locale en = new Locale("en");
    q1.addAttribute(null, "attrKey", "attrValue", en);
    Attribute attribute = q1.getAttributes().get(0);
    Assert.assertEquals("attrKey", attribute.getName());
    Assert.assertEquals("attrValue", attribute.getValue().getValue());
    Assert.assertEquals(en, attribute.getLocale());
    Assert.assertEquals(new Locale("en"), attribute.getLocale());
  }

  @Test
  public void testQuestionRemoveKeyAttributes() {
    Question q1 = new Question();
    q1.addAttribute(null, "attrKey", "value", null);
    q1.removeAttributes(null, "attrKey");
    Assert.assertEquals(0, q1.getAttributes().size());
  }

  @Test
  public void testQuestionRemoveNSKeyValueLocaleAttributes() {
    Question q1 = new Question();
    q1.addAttribute("ns", "attrKey", "value", new Locale("en"));
    q1.removeAttributes("ns", "attrKey");
    Assert.assertEquals(0, q1.getAttributes().size());
  }
}
