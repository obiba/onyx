/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.wizard;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.obiba.onyx.wicket.wizard.WizardForm;

/**
 * Interface for listeners of WizardForm "Begin" and "End" operations.
 */
public interface BeginEndListener {

  public void onBegin(WizardForm form, AjaxRequestTarget target);

  public void onEnd(WizardForm form, AjaxRequestTarget target);
}
