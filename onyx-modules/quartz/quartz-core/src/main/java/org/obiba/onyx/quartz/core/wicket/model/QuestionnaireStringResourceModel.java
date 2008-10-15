package org.obiba.onyx.quartz.core.wicket.model;

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.util.StringReferenceCompatibleMessageFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

public class QuestionnaireStringResourceModel extends AbstractReadOnlyModel {

  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  private static final int MAX_RESOLVE_STRING_REFERENCE_ATTEMPTS = 10;
  
  //
  // Instance Variables
  //

  private ILocalizable localizable;

  private String property;

  private Object[] stringArgs;

  //
  // Constructors
  //

  public QuestionnaireStringResourceModel(ILocalizable localizable, String property, Object[] stringArgs) {
    this.localizable = localizable;
    this.property = property;

    // Make a copy of the string arguments.
    if(stringArgs != null && stringArgs.length != 0) {
      this.stringArgs = new Object[stringArgs.length];
      System.arraycopy(stringArgs, 0, this.stringArgs, 0, this.stringArgs.length);
    }
  }

  //
  // AbstractReadOnlyModel Methods
  //

  @Override
  public Object getObject() {
    // Get the Spring application context.
    ApplicationContext context = ((SpringWebApplication) Application.get()).getSpringContextLocator().getSpringContext();

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

      if(isStringReference(stringResource)) {
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
   * Indicates whether the specified string resource is in fact a string reference (i.e., a reference to another string
   * resource).
   * 
   * References have the format: <code>${string}</code>
   * 
   * @param stringResource string resource
   * @return <code>true</code> if the resource is a reference
   */
  private boolean isStringReference(String stringResource) {
    return (stringResource != null && stringResource.startsWith(StringReferenceCompatibleMessageFormat.STRING_REFERENCE_PREFIX) && stringResource.endsWith(StringReferenceCompatibleMessageFormat.STRING_REFERENCE_SUFFIX));
  }

  private String extractKeyFromReference(String stringReference) {
    return stringReference.substring(StringReferenceCompatibleMessageFormat.STRING_REFERENCE_PREFIX.length(), stringReference.length() - StringReferenceCompatibleMessageFormat.STRING_REFERENCE_SUFFIX.length());
  }
}
