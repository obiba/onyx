/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.editor;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation("SYSTEM_ADMINISTRATOR")
public class EditorPage extends BasePage {

  public static final String EDITOR_COMPONENT = "editor-component";

  private final Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private ModuleRegistry moduleRegistry;

  public EditorPage() {

    log.info("moduleRegistry: " + moduleRegistry);

    for(Module module : moduleRegistry.getModules()) {
      Component editorComponent = module.getEditorPanel(EDITOR_COMPONENT);
      if(editorComponent != null) {
        log.info("editorComponent: " + editorComponent);
        add(editorComponent);
        break;
      }
    }
  }

}
