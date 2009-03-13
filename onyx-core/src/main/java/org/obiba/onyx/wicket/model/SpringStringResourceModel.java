/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.model;

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.SpringWebApplication;
import org.apache.wicket.util.string.interpolator.PropertyVariableInterpolator;
import org.obiba.wicket.application.ISpringWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;

/**
 * Localization using Spring messages, with the {@link Session} current locale.
 * @author Yannick Marcon
 * 
 */
public class SpringStringResourceModel extends LoadableDetachableModel {

  private static final long serialVersionUID = -1944585777700210245L;

  private transient ApplicationContext context;

  /** The locale to use. */
  private transient Locale locale;

  /** The key of message to get. */
  private final String resourceKey;

  /** The key message of message to get. */
  private final IModel resourceKeyModel;

  /** The wrapped model for property substitutions. */
  private final IModel model;

  /** Optional parameters. */
  private final Object[] parameters;

  /** The default value of the message. */
  private final String defaultValue;

  /**
   * Localize the given resource key.
   * @param resourceKey The resource key for this string resource
   */
  public SpringStringResourceModel(final String resourceKey) {
    this(resourceKey, null, null, resourceKey);
  }

  /**
   * Localize the given resource key.
   * @param resourceKeyModel The resource key model for this string resource
   */
  public SpringStringResourceModel(final IModel resourceKeyModel) {
    this(resourceKeyModel, null, null, null);
  }

  /**
   * Localize the given resource key.
   * @param resourceKey The resource key for this string resource
   * @param model The model to use for property substitutions
   */
  public SpringStringResourceModel(final String resourceKey, final IModel model) {
    this(resourceKey, model, null, resourceKey);
  }

  /**
   * Localize the given resource key.
   * @param resourceKeyModel The resource key model for this string resource
   * @param model The model to use for property substitutions
   */
  public SpringStringResourceModel(final IModel resourceKeyModel, final IModel model) {
    this(resourceKeyModel, model, null, null);
  }

  /**
   * Localize the given resource key, return default value if key not found.
   * @param resourceKey The resource key for this string resource
   * @param defaultValue The default value if the resource key is not found.
   */
  public SpringStringResourceModel(final String resourceKey, final String defaultValue) {
    this(resourceKey, null, null, defaultValue);
  }

  /**
   * Localize the given resource key, return default value if key not found.
   * @param resourceKeyModel The resource key model for this string resource
   * @param defaultValue The default value if the resource key is not found.
   */
  public SpringStringResourceModel(final IModel resourceKeyModel, final String defaultValue) {
    this(resourceKeyModel, null, null, defaultValue);
  }

  /**
   * Localize the given resource key, return default value if key not found.
   * @param resourceKey The resource key for this string resource
   * @param model The model to use for property substitutions
   * @param defaultValue The default value if the resource key is not found.
   */
  public SpringStringResourceModel(final String resourceKey, final IModel model, final String defaultValue) {
    this(resourceKey, model, null, defaultValue);
  }

  /**
   * Localize the given resource key, return default value if key not found.
   * @param resourceKeyModel The resource key model for this string resource
   * @param model The model to use for property substitutions
   * @param defaultValue The default value if the resource key is not found.
   */
  public SpringStringResourceModel(final IModel resourceKeyModel, final IModel model, final String defaultValue) {
    this(resourceKeyModel, model, null, defaultValue);
  }

  /**
   * Localize the given resource key, return default value if key not found.
   * @param resourceKey The resource key for this string resource
   * @param parameters Array of arguments that will be filled in for params within the message (params look like "{0}",
   * "{1,date}", "{2,time}" within a message), or <code>null</code> if none.
   * @param defaultValue The default value if the resource key is not found.
   */
  public SpringStringResourceModel(final String resourceKey, final Object[] parameters, final String defaultValue) {
    this(resourceKey, null, parameters, defaultValue);
  }

  /**
   * Localize the given resource key, return default value if key not found.
   * @param resourceKeyModel The resource key model for this string resource
   * @param parameters Array of arguments that will be filled in for params within the message (params look like "{0}",
   * "{1,date}", "{2,time}" within a message), or <code>null</code> if none.
   * @param defaultValue The default value if the resource key is not found.
   */
  public SpringStringResourceModel(final IModel resourceKeyModel, final Object[] parameters, final String defaultValue) {
    this(resourceKeyModel, null, parameters, defaultValue);
  }

  /**
   * Localize the given resource key, return default value if key not found.
   * @param resourceKey The resource key for this string resource
   * @param model The model to use for property substitutions
   * @param parameters Array of arguments that will be filled in for params within the message (params look like "{0}",
   * "{1,date}", "{2,time}" within a message), or <code>null</code> if none.
   * @param defaultValue The default value if the resource key is not found.
   */
  public SpringStringResourceModel(final String resourceKey, final IModel model, final Object[] parameters, final String defaultValue) {
    if(resourceKey == null) {
      throw new IllegalArgumentException("Resource key must not be null");
    }
    this.resourceKeyModel = null;
    this.resourceKey = resourceKey;
    this.defaultValue = defaultValue;
    this.parameters = parameters;
    this.model = model;
  }

  /**
   * Localize the given resource key, return default value if key not found.
   * @param resourceKeyModel The resource key model for this string resource
   * @param model The model to use for property substitutions
   * @param parameters Array of arguments that will be filled in for params within the message (params look like "{0}",
   * "{1,date}", "{2,time}" within a message), or <code>null</code> if none.
   * @param defaultValue The default value if the resource key is not found.
   */
  public SpringStringResourceModel(final IModel resourceKeyModel, final IModel model, final Object[] parameters, final String defaultValue) {
    this.resourceKeyModel = resourceKeyModel;
    this.resourceKey = null;
    this.defaultValue = defaultValue;
    this.parameters = parameters;
    this.model = model;
  }

  protected Object load() {
    // Initialize information that we need to work successfully

    Session session = Session.get();
    if(session != null) {
      locale = session.getLocale();
    }
    Application application = Application.get();
    if(application instanceof SpringWebApplication) {
      context = ((SpringWebApplication) application).getSpringContextLocator().getSpringContext();
    } else if(application instanceof ISpringWebApplication) {
      context = ((ISpringWebApplication) application).getSpringContextLocator().getSpringContext();
    }

    if(locale == null || context == null) {
      throw new WicketRuntimeException("Cannot attach a string resource model without a Session context or a valid Application context because that is required to get a Spring application Context");
    }
    return getStringResource();
  }

  protected void onDetach() {
    // Detach any model
    if(model != null) {
      model.detach();
    }
    if(resourceKeyModel != null) {
      resourceKeyModel.detach();
    }
    // nullification
    context = null;
    locale = null;
  }

  /**
   * Get the resource value from Spring {@link MessageSource}.
   * @return
   */
  private String getStringResource() {
    String key = getResourceKey();
    String value = context.getMessage(key, getParameters(), defaultValue, locale);

    return value != null ? value : key;
  }

  public final String getString() {
    if(context == null) {
      return (String) load();
    }
    return getStringResource();
  }

  /**
   * Gets the Java MessageFormat substitution parameters.
   * 
   * @return The substitution parameters
   */
  protected Object[] getParameters() {
    return parameters;
  }

  /**
   * Gets the resource key for this string resource. If the resource key contains property expressions and the model is
   * not null then the returned value is the actual resource key with all substitutions undertaken.
   * 
   * @return The (possibly substituted) resource key
   */
  protected final String getResourceKey() {
    String key = resourceKey;
    if(resourceKeyModel != null) {
      key = (String) resourceKeyModel.getObject();
    }

    if(model != null) {
      return PropertyVariableInterpolator.interpolate(key, model.getObject());
    } else {
      return key;
    }
  }

}
