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
import java.util.Properties;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.obiba.magma.js.GlobalMethodProvider;
import org.obiba.magma.js.ScriptableValue;
import org.obiba.magma.type.TextType;
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

  public static final Set<String> GLOBAL_METHODS = ImmutableSet.of("onyx");

  private Resource onyxPropertyResource;

  private static Properties onyxProperties;

  public void setOnyxPropertyResource(Resource onyxPropertyResource) {
    this.onyxPropertyResource = onyxPropertyResource;
  }

  public void init() {
    try {
      onyxProperties = PropertiesLoaderUtils.loadProperties(onyxPropertyResource);
    } catch(IOException e) {
      throw new IllegalArgumentException("Could not read in the Resource [" + onyxPropertyResource + "]. ", e);
    }
  }

  /**
   * Allows access to the onyx configuration variables. Returns a {@code ScriptableValue}. Accessed as 'onyx' in
   * javascript.
   * 
   * <pre>
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
