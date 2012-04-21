/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.stage.page;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.webapp.home.page.InternalErrorPage;
import org.obiba.onyx.webapp.stage.panel.StageMenuBar;

/**
 *
 */
public class InternalErrorStagePage extends InternalErrorPage {
  private StageMenuBar menuBar;

  public InternalErrorStagePage(Page returnPage, IModel<Stage> stageModel) {
    super();

    //
    // Modify header.
    //
    remove("header");
    add(new EmptyPanel("header"));

    //
    // Modify menu bar.
    //
    remove("menuBar");
    menuBar = new StageMenuBar("menuBar", stageModel);
    add(menuBar);

    replace(newLink("link", returnPage));
  }

}
