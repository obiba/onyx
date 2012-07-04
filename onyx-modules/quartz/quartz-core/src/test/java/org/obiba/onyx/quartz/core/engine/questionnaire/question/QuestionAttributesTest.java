package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.util.Locale;
import java.util.NoSuchElementException;

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
  public void testQuestionMergeKeyAttributes() {
    Question q1 = new Question();
    q1.addAttribute(null, "attrKey", "value", null);
    Attribute attribute = q1.getAttribute(null, "attrKey", null);
    q1.updateAttribute(attribute, null, "newAttrKey", "newValue", null);
    Assert.assertEquals(1, q1.getAttributes().size());
    Assert.assertEquals("newValue", q1.getAttribute(null, "newAttrKey", null).getValue().getValue());
  }

  @Test
  public void testQuestionMergeNSKeyValueLocaleAttributes() {
    Question q1 = new Question();
    q1.addAttribute("ns", "attrKey", "value", new Locale("en"));
    Attribute attribute = q1.getAttribute("ns", "attrKey", new Locale("en"));
    q1.updateAttribute(attribute, "ns", "attrKey", "newValue", new Locale("en"));
    Assert.assertEquals(1, q1.getAttributes().size());
    Assert.assertEquals("newValue", q1.getAttribute("ns", "attrKey", new Locale("en")).getValue().getValue());
  }

  @Test
  public void testQuestionInvalidKeyAttributes() {
    Question q1 = new Question();
    q1.addAttribute(null, "attrKey", null, null);
    Attribute attribute = q1.getAttribute(null, "attrKey", null);
    q1.updateAttribute(attribute, null, "newAttrKey", null, null);
    Assert.assertEquals(1, q1.getAttributes().size());
    Assert.assertFalse(q1.containsAttribute(attribute));
    try {
      Assert.assertNull(q1.getAttribute(null, "invalidKey", null).getValue().getValue());
      Assert.fail("must throw NoSuchElementException");
    } catch(NoSuchElementException e) {

    }
  }
}
