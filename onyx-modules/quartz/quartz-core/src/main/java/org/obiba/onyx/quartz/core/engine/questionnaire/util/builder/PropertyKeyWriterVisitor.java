package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;

/**
 * Questionnaire visitor for building localization properties at each questionnaire element visit.
 * @author Yannick Marcon
 *
 */
public class PropertyKeyWriterVisitor implements IVisitor {

  private IPropertyKeyProvider propertyKeyProvider;

  private IPropertyKeyWriter writer;

  private List<String> propertyKeys = new ArrayList<String>();

  /**
   * Constructor, given property key provider and writer.
   * @param propertyKeyProvider
   * @param writer
   */
  public PropertyKeyWriterVisitor(IPropertyKeyProvider propertyKeyProvider, IPropertyKeyWriter writer) {
    this.propertyKeyProvider = propertyKeyProvider;
    this.writer = writer;
  }

  public void visit(Questionnaire questionnaire) {
    writePropertyKey(questionnaire);
    for(Category category : questionnaire.findSharedCategories()) {
      writePropertyKey(category);
    }
  }

  public void visit(Section section) {
    writePropertyKey(section);
  }

  public void visit(Page page) {
    writePropertyKey(page);
  }

  public void visit(Question question) {
    writePropertyKey(question);
  }

  public void visit(QuestionCategory questionCategory) {
    writePropertyKey(questionCategory, questionCategory.getCategory());
  }

  public void visit(Category category) {
    writePropertyKey(category);
  }

  public void visit(OpenAnswerDefinition openAnswerDefinition) {
    writePropertyKey(openAnswerDefinition);
  }

  /**
   * Shortcut method call.
   * @param localizable
   * @param properties
   */
  private void writePropertyKey(ILocalizable localizable) {
    writePropertyKey(localizable, null);
  }

  /**
   * For each of the localization keys declared by the {@link ILocalizable} add it to the properties object. Set the
   * value to null by default or to the localization interpolation key.
   * @param localizable
   * @param interpolationLocalizable
   * @param writer
   */
  private void writePropertyKey(ILocalizable localizable, ILocalizable interpolationLocalizable) {
    boolean written = false;
    for(String property : propertyKeyProvider.getProperties(localizable)) {
      String key = propertyKeyProvider.getPropertyKey(localizable, property);
      if(!propertyKeys.contains(key)) {
        Properties ref = writer.getReference();
        if(ref != null && ref.containsKey(key)) {
          writer.write(key, ref.getProperty(key));
        } else {
          writer.write(key, interpolationLocalizable == null ? "" : "${" + propertyKeyProvider.getPropertyKey(interpolationLocalizable, property) + "}");
        }
        propertyKeys.add(key);
        written = true;
      }
    }
    if(written) writer.endBloc();
  }

}
