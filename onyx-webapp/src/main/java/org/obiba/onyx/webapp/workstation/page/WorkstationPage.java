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
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.engine.Module;
import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.webapp.base.page.BasePage;

@AuthorizeInstantiation({ "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class WorkstationPage extends BasePage {

  public static final String WORKSTATION_CONTENT = "workstationContent";

  @SpringBean
  private ModuleRegistry moduleRegistry;

  public WorkstationPage() {
    super();
    addOrReplace(new WorkstationFragment("workstationPageContent"));
  }

  private class WorkstationFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public WorkstationFragment(String id) {
      super(id, "workstationFragment", WorkstationPage.this);
      RepeatingView repeater = new RepeatingView(WORKSTATION_CONTENT);

      for(Module module : moduleRegistry.getModules()) {
        if(module.getWorkstationPanel(WORKSTATION_CONTENT) != null) {
          repeater.add(module.getWorkstationPanel(WORKSTATION_CONTENT));
        }
      }

      add(repeater);
    }
  }

  @Override
  public void renderHead(IHeaderResponse response) {
    super.renderHead(response);
    response.renderOnLoadJavascript("styleWorkstationNavigationBar();");
  }

}