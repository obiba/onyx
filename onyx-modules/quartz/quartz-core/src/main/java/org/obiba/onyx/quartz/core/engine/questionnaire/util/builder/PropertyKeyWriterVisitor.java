/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.IWalkerVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Questionnaire visitor for building localization properties at each questionnaire element visit.
 * @author Yannick Marcon
 * 
 */
public class PropertyKeyWriterVisitor implements IWalkerVisitor {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(PropertyKeyWriterVisitor.class);

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
    writer.writeComment("", "Questionnaire: " + questionnaire.getName() + ", version " + questionnaire.getVersion(), "");
    writePropertyKey(questionnaire);
    writer.writeComment("", "Shared categories", "");
    for(Category category : QuestionnaireFinder.getInstance(questionnaire).findSharedCategories()) {
      if(!category.hasDataSource()) writePropertyKey(category);
    }
  }

  public void visit(Section section) {
    writePropertyKey(section);
  }

  public void visit(Page page) {
    writePropertyKey(page);
  }

  public void visit(Question question) {
    // Questions with answer source should not be included in localization file, since they are not displayed on the UI.
    if(!question.hasDataSource()) {
      writer.writeComment("", "Question " + question.getName(), "");
      writePropertyKey(question);
    }
  }

  public void visit(QuestionCategory questionCategory) {
    if(!questionCategory.getCategory().hasDataSource()) {
      writePropertyKey(questionCategory, questionCategory.getCategory());
    }
  }

  public void visit(Category category) {
    // write category property keys only if it is a shared one
    // this is done when visiting questionnaire
  }

  public void visit(OpenAnswerDefinition openAnswerDefinition) {
    if(openAnswerDefinition.getDataSource() == null) {
      writePropertyKey(openAnswerDefinition);
    }
  }

  @Override
  public void visit(Variable variable) {
    // TODO Auto-generated method stub

  }

  /**
   * Shortcut method call.
   * @param localizable
   * @param properties
   */
  private void writePropertyKey(IQuestionnaireElement localizable) {
    writePropertyKey(localizable, null);
  }

  /**
   * For each of the localization keys declared by the {@link IQuestionnaireElement} add it to the properties object.
   * Set the value to null by default or to the localization interpolation key.
   * @param localizable
   * @param interpolationLocalizable
   * @param writer
   */
  private void writePropertyKey(IQuestionnaireElement localizable, IQuestionnaireElement interpolationLocalizable) {
    boolean written = false;
    for(String property : propertyKeyProvider.getProperties(localizable)) {
      String key = propertyKeyProvider.getPropertyKey(localizable, property);
      if(!propertyKeys.contains(key)) {
        Properties ref = writer.getReference();
        if(ref != null && ref.containsKey(key) && !ref.get(key).equals("")) {
          // property key value already defined
          writer.write(key, ref.getProperty(key));
        } else if(interpolationLocalizable != null) {
          String interpolationKey = propertyKeyProvider.getPropertyKey(interpolationLocalizable, property);
          if(propertyKeys.contains(interpolationKey)) {
            // interpolation already written, just refer to it
            writer.write(key, "${" + interpolationKey + "}");
          } else {
            // interpolation not already written, then ignored
            writer.write(key, "");
          }
        } else {
          writer.write(key, "");
        }
        propertyKeys.add(key);
        written = true;
      }
    }
    if(written) writer.endBloc();
  }

  public boolean visiteMore() {
    // no stop
    return true;
  }

}
