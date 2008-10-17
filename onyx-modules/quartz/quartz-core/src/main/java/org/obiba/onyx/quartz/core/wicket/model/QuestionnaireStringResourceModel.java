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

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.StringReferenceCompatibleMessageFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

public class QuestionnaireStringResourceModel extends LoadableDetachableModel {

  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final int MAX_RESOLVE_STRING_REFERENCE_ATTEMPTS = 10;

  //
  // Instance Variables
  //

  private ApplicationContext context;

  private ILocalizable localizable;

  private String property;

  private Object[] stringArgs;

  //
  // Constructors
  //

  public QuestionnaireStringResourceModel(ILocalizable localizable, String property, Object... stringArgs) {
    this.localizable = localizable;
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
  protected Object load() {
    // Get the Spring application context.
    if(context == null) {
      if(Application.get() instanceof SpringWebApplication) {
        context = ((SpringWebApplication) Application.get()).getSpringContextLocator().getSpringContext();
      } else {
        throw new WicketRuntimeException("Cannot load QuestionnaireStringResourceModel's object (not running within a SpringWebApplication)");
      }
    }

    // From the context, get the services required to resolve the string resource.
    ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService = (ActiveQuestionnaireAdministrationService) context.getBean("activeQuestionnaireAdministrationService");
    QuestionnaireBundleManager bundleManager = (QuestionnaireBundleManager) context.getBean("questionnaireBundleManager");

    // Now use these services to get current questionnaire bundle.
    Locale locale = activeQuestionnaireAdministrationService.getLanguage();
    String bundleName = activeQuestionnaireAdministrationService.getQuestionnaire().getName();
    QuestionnaireBundle bundle = bundleManager.getBundle(bundleName);

    // Finally, resolve the string resource using the bundle's message source and the
    // property key.
    MessageSource messageSource = bundle.getMessageSource();
    String propertyKey = bundle.getPropertyKey(localizable, property);

    String stringResource = null;

    int resolveAttempts = 0;

    while(true) {
      stringResource = messageSource.getMessage(propertyKey, stringArgs, locale);

      if(StringReferenceCompatibleMessageFormat.isStringReference(stringResource)) {
        if(resolveAttempts == MAX_RESOLVE_STRING_REFERENCE_ATTEMPTS) {
          throw new RuntimeException("Exceeded maximum number of attempts to resolve string reference");
        }

        resolveAttempts++;
        propertyKey = extractKeyFromReference(stringResource);
      } else {
        break;
      }
    }

    return stringResource;
  }

  //
  // Methods
  //

  /**
   * Convenience method. Equivalent to <code>(String)getObject()</code>.
   * 
   * @return model object as string
   */
  public String getString() {
    return (String) getObject();
  }

  private String extractKeyFromReference(String stringReference) {
    return stringReference.substring(StringReferenceCompatibleMessageFormat.STRING_REFERENCE_PREFIX.length(), stringReference.length() - StringReferenceCompatibleMessageFormat.STRING_REFERENCE_SUFFIX.length());
  }
}
