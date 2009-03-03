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

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.obiba.onyx.webapp.base.page.BasePage;

/**
 * 
 */
public class InvalidStagePage extends BasePage {

  @SuppressWarnings("serial")
  public InvalidStagePage(final StagePage stagePage) {
    super();

    //
    // Modify header.
    //
    remove("header");
    add(new EmptyPanel("header"));

    add(new Link("link") {

      @Override
      public void onClick() {
        setResponsePage(stagePage);
      }

    });
  }

}
