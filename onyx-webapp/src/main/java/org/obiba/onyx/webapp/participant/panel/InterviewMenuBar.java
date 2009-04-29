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

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.webapp.base.panel.MenuBar;

public class InterviewMenuBar extends MenuBar {

  private static final long serialVersionUID = 8805458043658346936L;

  @SpringBean
  private InterviewManager interviewManager;

  public InterviewMenuBar(String id) {
    super(id);
  }

  protected void buildMenus() {
  }
}
