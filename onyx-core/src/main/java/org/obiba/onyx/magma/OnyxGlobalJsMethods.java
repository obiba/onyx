/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.js.GlobalMethodProvider;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.DateTimeType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.core.domain.statistics.ExportLog;
import org.obiba.onyx.core.service.ExportLogService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Provides Onyx properties and select Onyx variables to JavaScript code.
 */
public class OnyxGlobalJsMethods implements GlobalMethodProvider {

  private static final String LAST_EXPORT_DATE = "org.obiba.onyx.lastExportDate";

  private static final Set<String> GLOBAL_METHODS = ImmutableSet.of("onyx");

  private Resource onyxPropertyResource;

  private static ExportLogService exportLogService;

  private static Properties onyxProperties;

  public void setOnyxPropertyResource(Resource onyxPropertyResource) {
    this.onyxPropertyResource = onyxPropertyResource;
  }

  public void setExportLogService(ExportLogService exportLogService) {
    initialiseLogService(exportLogService);
  }

  private static void initialiseLogService(ExportLogService exportLogService) {
    OnyxGlobalJsMethods.exportLogService = exportLogService;
  }

  public void init() {
    try {
      setOnyxProperties(PropertiesLoaderUtils.loadProperties(onyxPropertyResource));
    } catch(IOException e) {
      throw new IllegalArgumentException("Could not read in the Resource [" + onyxPropertyResource + "]. ", e);
    }
  }

  private static void setOnyxProperties(Properties properties) {
    OnyxGlobalJsMethods.onyxProperties = properties;
  }

  public String getJavaScriptMethodName(Method method) {
    return method.getName();
  }

  /**
   * Allows access to the onyx configuration variables. Returns a {@code ScriptableValue}. Accessed as 'onyx' in
   * javascript.
   * 
   * <pre>
   *   onyx('org.obiba.onyx.lastExportDate')
   *   onyx('org.obiba.onyx.participant.purge')
   *   onyx('org.obiba.onyx.webapp.configurationType')
   * </pre>
   * @return an instance of {@code ScriptableValue}
   */
  public static Scriptable onyx(Context ctx, Scriptable thisObj, Object[] args, Function funObj) {
    if(args.length != 1) {
      throw new UnsupportedOperationException("onyx() expects exactly one argument: a property name.");
    }

    String propertyName = (String) args[0];
    if(propertyName.equals(LAST_EXPORT_DATE)) {
      return OnyxGlobalJsMethods.getLastExportDate(thisObj);
    } else {
      return OnyxGlobalJsMethods.getProperty(thisObj, propertyName);
    }
  }

  private static Scriptable getLastExportDate(Scriptable thisObj) {
    List<ExportLog> logs = exportLogService.getExportLogs("Participant", null, false);
    if(logs.size() > 0) {
      return new ScriptableValue(thisObj, DateTimeType.get().valueOf(logs.get(0).getExportDate()));
    } else {
      // No export has occurred. Will return the beginning of (unix) time.
      return new ScriptableValue(thisObj, DateTimeType.get().valueOf(new Date(0L)));
    }
  }

  private static Scriptable getProperty(Scriptable thisObj, String propertyName) {
    String property = onyxProperties.getProperty(propertyName);
    if(property == null) {
      return new ScriptableValue(thisObj, TextType.get().nullValue());
    } else {
      return new ScriptableValue(thisObj, TextType.get().valueOf(property));
    }
  }

  public Collection<Method> getJavaScriptExtensionMethods() {

    Iterable<Method> methods = Iterables.filter(Arrays.asList(this.getClass().getMethods()), new Predicate<Method>() {

      public boolean apply(Method input) {
        return OnyxGlobalJsMethods.GLOBAL_METHODS.contains(input.getName());
      }

    });

    return Lists.newArrayList(methods);
  }

}
