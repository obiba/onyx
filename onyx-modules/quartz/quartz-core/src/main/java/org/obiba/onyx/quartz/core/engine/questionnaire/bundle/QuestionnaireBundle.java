package org.obiba.onyx.quartz.core.engine.questionnaire.bundle;

import java.util.Locale;
import java.util.Set;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.springframework.context.MessageSource;

public interface QuestionnaireBundle {
  
  /**
   * Returns the name of the bundle.
   * 
   * This is identical with the name of the questionnaire.
   * 
   * @return bundle name
   */
  public String getName();
 
  /**
   * Returns the bundle's questionnaire.
   * 
   * Exactly one questionnaire exists per bundle.
   *  
   * @return questionnaire
   */
  public Questionnaire getQuestionnaire();
  
  /**
   * Returns the path to the specified resource (i.e., the part of the URI <i>after</i> 
   * the context path).
   * 
   * @param name resource name
   * @return resource path (beginning with '/')
   */
  public String getResourcePath(String name);
  
  /**
   * Returns the Spring <code>MessageSource</code> associated with the bundle.
   * 
   * The <code>MessageSource</code> can be used to localize questionnaire text
   * to any of the languages returned by <code>getAvailableLanguages()</code>.
   * 
   * @return bundle message source
   */
  public MessageSource getMessageSource();
  
  /**
   * Returns the languages in which the bundle's questionnaire is available, 
   * as a set of locales.
   * 
   * The languages returned are determined by the presence of corresponding 
   * resource bundles in the questionnaire bundle. For example, if the following
   * resource bundles exist
   * 
   * <ul>
   *   <li>questionnaire_en.properties</li>
   *   <li>questionnaire_fr.properties</li>
   * </ul>
   * 
   * a set containing two locales shall be returned -- <code>Locale("en")</code>
   * and <code>Locale("fr")</code>.
   * 
   * @return
   */
  public Set<Locale> getAvailableLanguages();
}