/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.panel;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.webapp.base.panel.MenuBar;
import org.obiba.onyx.webapp.participant.page.ParticipantSearchPage;

public class InterviewMenuBar extends MenuBar {

  private static final long serialVersionUID = 8805458043658346936L;

  @SpringBean
  private InterviewManager interviewManager;

  public InterviewMenuBar(String id) {
    super(id);

    AbstractLink link = new Link("exitLink") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick() {
        interviewManager.releaseInterview();
        setResponsePage(ParticipantSearchPage.class);
      }
    };
    add(link);
  }

  protected void buildMenus() {
  }
}
