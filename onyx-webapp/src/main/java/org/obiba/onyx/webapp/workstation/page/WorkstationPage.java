/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.workstation.page;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.home.page.HomePage;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class WorkstationPage extends BasePage {

  @SpringBean
  private ModuleRegistry moduleRegistry;

  private final String JADE_MODULE_NAME = "jade";

  public WorkstationPage() {
    super();

    Module jadeModule = moduleRegistry.getModule(JADE_MODULE_NAME);
    if(jadeModule != null) {
      add(jadeModule.getWidget("workstationContent"));
    } else {
      setResponsePage(HomePage.class);
    }
  }

}