/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.reusable;

/**
 * Implementers provide access to the reusable dialogs: {@link ConfirmationDialog} and printable reports dialog. This
 * interface is visible to all modules (quartz, jade, etc.) even though the "BasePage" location of the reusable dialogs
 * is not. This allows modules access to the reusable dialogs, even though the BasePage is not visible to them.
 */
public interface ReusableDialogProvider {

  /**
   * Returns the reusable confirmation dialog. Since the confirmation dialog is modal, this single instance can be
   * reused every time a confirmation dialog is required.
   * @return the reusable confirmation dialog.
   */
  public ConfirmationDialog getConfirmationDialog();

  /**
   * Returns the reusable printable reports dialog. Since the printable reports dialog is modal, this single instance
   * can be reused every time a confirmation dialog is required.
   * @return the reusable printable reports dialog.
   */
  public Dialog getPrintableReportsDialog();

}
