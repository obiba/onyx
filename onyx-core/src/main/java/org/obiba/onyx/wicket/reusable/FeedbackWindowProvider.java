/*******************************************************************************
 * Copyright 2011(c) OBiBa. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.reusable;

/**
 * Implemented by classes that have an instance of {@code FeedbackWindow} and can provide it to other components. This
 * is usually implemented by a {@code Page}.
 */
public interface FeedbackWindowProvider {

  public FeedbackWindow getFeedbackWindow();

}
