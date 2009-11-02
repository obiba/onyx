/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Holds all {@link ActionDefinition} instances that drive the pop-up displays when an action is performed on a stage.
 * <p>
 * This implementations relies on the {@link ActionDefinitionReader} class to load instances from XML files. The files
 * are loaded in the following order (to allow the later to override the first):
 * <ul>
 * <li>classpath*:/META-INF/action-definitions.xml</li>
 * <li>onyxConfigPath/action-definitions.xml</li>
 * <li>onyxConfigPath/*\/action-definitions.xml</li>
 * </ul>
 */
public class ActionDefinitionConfiguration implements ResourceLoaderAware, InitializingBean {

  private static final Logger log = LoggerFactory.getLogger(ActionDefinitionConfiguration.class);

  private static final String ACTION_DEFINITION_FILENAME = "action-definitions.xml";

  private static final char SEPARATOR = '.';

  private static final String ACTION_PREFIX = "action";

  private String onyxConfigPath;

  private ResourceLoader resourceLoader;

  private Map<String, ActionDefinition> actionDefinitionCache = Collections.synchronizedMap(new HashMap<String, ActionDefinition>());

  public ActionDefinition getActionDefinition(ActionType type, String stateName, String module, String stage) {
    String[] codes = calculateCodes(type, stateName, module, stage);
    for(int i = 0; i < codes.length; i++) {
      String code = codes[i].toLowerCase();
      log.debug("Looking up ActionDefinition for code {}", code);
      ActionDefinition actionDefinition = this.actionDefinitionCache.get(code);
      if(actionDefinition != null) {
        log.debug("Found ActionDefinition with code {}", code);

        // treat the case of the top-level "template" ActionDefinition that is associated to all types.
        String newCode = buildCode(type, stateName, module, stage);
        if(code.equals(newCode) == false) {
          // copy the default ActionDefinition
          actionDefinition = new ActionDefinition(actionDefinition);
          // set the copy's type to the required type.
          actionDefinition.setType(type);
          // set the copy's code to the most specific.
          actionDefinition.setCode(buildCode(type, stateName, module, stage));
          actionDefinitionCache.put(actionDefinition.getCode().toLowerCase(), actionDefinition);
        }

        return actionDefinition;
      }
    }
    return null;
  }

  public ActionDefinition getActionDefinition(String code) {
    ActionDefinitionCode codeObj = new ActionDefinitionCode(code);
    return getActionDefinition(codeObj.getType(), codeObj.getState(), codeObj.getModule(), codeObj.getStage());
  }

  public void afterPropertiesSet() throws Exception {
    findDefinitions();
  }

  public void setResourceLoader(ResourceLoader resourceLoader) {
    this.resourceLoader = resourceLoader;
  }

  public void setOnyxConfigPath(String onyxConfigPath) {
    this.onyxConfigPath = onyxConfigPath;
  }

  /**
   * Returns an array of hierarchical codes that are used to find an associated {@code ActionDefinition} instance. This
   * method will build an array of codes with the first element being the most specific:
   * <ul>
   * <li>ACTION_PREFIX.type.stateName.module.stage</li>
   * <li>ACTION_PREFIX.type.stateName.module</li>
   * <li>ACTION_PREFIX.type.stateName</li>
   * <li>ACTION_PREFIX.type</li>
   * <li>ACTION_PREFIX
   * <li>
   * </ul>
   * @param type the {@link ActionType} of the {@code ActionDefinition}
   * @param stateName the name of the state on which the action is performed
   * @param module the module that contributed the stage
   * @param stage the stage on which the action is performed
   * @return an array of codes
   */
  static public String[] calculateCodes(ActionType type, String stateName, String module, String stage) {
    ArrayList<String> codes = new ArrayList<String>();
    StringBuilder sb = new StringBuilder(ACTION_PREFIX);
    codes.add(sb.toString());
    sb.append(SEPARATOR).append(type);
    codes.add(sb.toString());
    if(stateName != null) {
      sb.append(SEPARATOR).append(stateName);
      codes.add(sb.toString());
    }
    if(module != null) {
      sb.append(SEPARATOR).append(module);
      codes.add(sb.toString());
    }
    if(stage != null) {
      sb.append(SEPARATOR).append(stage);
      codes.add(sb.toString());
    }
    Collections.reverse(codes);
    return codes.toArray(new String[codes.size()]);
  }

  /**
   * Extracts an array of codes from the specified {@code ActionDefinition} that respects the algorithm defined in
   * {@link ActionDefinitionConfiguration#calculateCodes(ActionType, String, String, String)}. If the {@code
   * ActionDefinition}'s code is
   * 
   * <pre>
   * action.execute.skipped
   * </pre>
   * 
   * the array of codes returned will contain:
   * <ul>
   * <li>action.execute.skipped</li>
   * <li>action.execute</li>
   * <li>action</li>
   * </ul>
   * @param definition the instance from which to extract the codes.
   * @param suffix a suffix to append to all generated codes
   * @return an array of codes extracted from the definition.
   */
  static public String[] calculateCodes(ActionDefinition definition, String suffix) {
    ActionDefinitionCode code = new ActionDefinitionCode(definition.getCode());

    String[] codes = ActionDefinitionConfiguration.calculateCodes(code.getType(), code.getState(), code.getModule(), code.getStage());
    if(suffix != null) {
      for(int i = 0; i < codes.length; i++) {
        codes[i] = codes[i] += suffix;
      }
    }

    return codes;
  }

  static private String buildCode(ActionType type, String stateName, String module, String stage) {
    StringBuilder sb = new StringBuilder(ACTION_PREFIX).append(SEPARATOR).append(type);
    if(stateName != null) {
      sb.append(SEPARATOR).append(stateName);
    }
    if(module != null) {
      sb.append(SEPARATOR).append(module);
    }
    if(stage != null) {
      sb.append(SEPARATOR).append(stage);
    }
    return sb.toString();
  }

  /**
   * Finds and loads action-definition.xml to build the cache of {@code ActionDefinition} instances.
   * @throws IOException
   */
  protected void findDefinitions() throws IOException {
    ResourcePatternResolver resolver = (ResourcePatternResolver) this.resourceLoader;

    // Find definitions in onyx jar files (including module jar files)
    String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "META-INF/" + ACTION_DEFINITION_FILENAME;
    Resource[] resources = resolver.getResources(pattern);
    loadDefinitions(resources);

    Resource configPath = resolver.getResource(onyxConfigPath);
    if(configPath != null && configPath.exists()) {
      // Find definitions in the configuration directory
      resources = resolver.getResources(onyxConfigPath + "/" + ACTION_DEFINITION_FILENAME);
      loadDefinitions(resources);

      // Find definitions in the module configuration directory
      resources = resolver.getResources(onyxConfigPath + "/*/" + ACTION_DEFINITION_FILENAME);
      loadDefinitions(resources);
    }
  }

  /**
   * Delegates to an {@link ActionDefinitionReader} instance to deserialize a list of {@code ActionDefinition} instances
   * and caches the result.
   * @param resources
   * @throws IOException
   */
  protected void loadDefinitions(Resource[] resources) throws IOException {
    if(resources != null && resources.length > 0) {
      ActionDefinitionReader reader = new ActionDefinitionReader();
      reader.setResources(resources);
      List<ActionDefinition> definitions = reader.read();
      for(ActionDefinition actionDefinition : definitions) {
        this.actionDefinitionCache.put(actionDefinition.getCode().toLowerCase(), actionDefinition);
      }
    }
  }

  //
  // Inner Classes
  //

  private static class ActionDefinitionCode {
    private ActionType type;

    private String state;

    private String module;

    private String stage;

    public ActionDefinitionCode(String code) {
      String[] codeElements = code.split("\\.");

      type = (codeElements.length >= 2) ? ActionType.valueOf(codeElements[1]) : null;
      state = (codeElements.length >= 3) ? codeElements[2] : null;
      module = (codeElements.length >= 4) ? codeElements[3] : null;
      stage = (codeElements.length >= 5) ? codeElements[4] : null;
    }

    public ActionType getType() {
      return type;
    }

    public String getState() {
      return state;
    }

    public String getModule() {
      return module;
    }

    public String getStage() {
      return stage;
    }
  }
}
