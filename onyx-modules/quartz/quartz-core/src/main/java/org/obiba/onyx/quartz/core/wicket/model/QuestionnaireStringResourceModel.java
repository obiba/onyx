/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.model;

import java.util.Locale;

import org.apache.wicket.model.IModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.model.SpringDetachableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionnaireStringResourceModel extends SpringDetachableModel<String> {

  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(QuestionnaireStringResourceModel.class);

  //
  // Instance Variables
  //

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean
  private QuestionnaireBundleManager bundleManager;

  private IModel<? extends IQuestionnaireElement> localizableModel;

  private String property;

  private Object[] stringArgs;

  //
  // Constructors
  //

  /**
   * Constructor using a model for providing the {@link IQuestionnaireElement}.
   * @param localizableModel
   * @param property
   * @param stringArgs
   */
  public QuestionnaireStringResourceModel(IModel<? extends IQuestionnaireElement> localizableModel, String property, Object... stringArgs) {
    super();
    this.localizableModel = localizableModel;
    initialize(property, stringArgs);
  }

  /**
   * Constructor using directly the {@link IQuestionnaireElement}.
   * @param localizable
   * @param property
   * @param stringArgs
   */
  public QuestionnaireStringResourceModel(IQuestionnaireElement localizable, String property, Object... stringArgs) {
    super();
    if(localizable == null) throw new IllegalArgumentException("Localizable element cannot be null.");
    this.localizableModel = new QuestionnaireModel<IQuestionnaireElement>(localizable);
    initialize(property, stringArgs);
  }

  @SuppressWarnings("hiding")
  private void initialize(String property, Object... stringArgs) {
    this.property = property;

    // Make a copy of the string arguments.
    if(stringArgs != null && stringArgs.length != 0) {
      this.stringArgs = new Object[stringArgs.length];
      System.arraycopy(stringArgs, 0, this.stringArgs, 0, this.stringArgs.length);
    }
  }

  //
  // LoadableDetachableModel Methods
  //

  @Override
  protected String load() {

    // Now use these services to get current questionnaire bundle.
    Locale locale = activeQuestionnaireAdministrationService.getLanguage();
    String bundleName = activeQuestionnaireAdministrationService.getQuestionnaire().getName();
    QuestionnaireBundle bundle = bundleManager.getBundle(bundleName);

    // Finally, resolve the string resource using the bundle's message source and the
    // property key.
    String message = QuestionnaireStringResourceModelHelper.getMessage(bundle, getLocalizable(), property, stringArgs, locale);

    return resolveVariableValuesInMessage(message, bundleName);
  }

  private String resolveVariableValuesInMessage(String message, String tableContext) {
    if(activeQuestionnaireAdministrationService.isQuestionnaireDevelopmentMode()) return message;
    String msg = message;
    // Look for variable references and replace by the value as a string
    try {
      int refIndex = msg.indexOf("$('");
      while(refIndex != -1) {
        int refEndIndex = msg.indexOf("')", refIndex);
        String path = msg.substring(refIndex + 3, refEndIndex);
        if(!path.contains(":")) {
          path = tableContext + ":" + path;
        }
        VariableDataSource varDs = new VariableDataSource(path);
        Data data = varDs.getData(activeQuestionnaireAdministrationService.getQuestionnaireParticipant().getParticipant());
        String dataStr = "";
        if(data != null && data.getValue() != null) {
          dataStr = data.getValueAsString();
        }
        msg = msg.substring(0, refIndex) + dataStr + msg.substring(refEndIndex + 2, msg.length());
        refIndex = msg.indexOf("$('");
      }
    } catch(Exception e) {
      log.error("Error while resolving variable values in: " + message, e);
    }
    return msg;
  }

  //
  // Methods
  //

  /**
   * Get the localizable element directly or from a model.
   */
  private IQuestionnaireElement getLocalizable() {
    IQuestionnaireElement loc = null;
    if(localizableModel != null) {
      loc = localizableModel.getObject();
    }
    if(loc == null) throw new IllegalArgumentException("Localizable element cannot be null.");

    return loc;
  }

  /**
   * Convenience method. Equivalent to <code>(String)getObject()</code>.
   * 
   * @return model object as string
   */
  public String getString() {
    return getObject();
  }
}
