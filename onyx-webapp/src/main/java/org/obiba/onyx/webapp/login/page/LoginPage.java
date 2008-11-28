/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.login.page;


import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.login.panel.LoginPanel;


public class LoginPage extends BasePage {

  private static final long serialVersionUID = -3536960410263657341L;

  public LoginPage() {
    add(new LoginPanel("loginPanel"));
  }
  
 
}
