/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.builder;

import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;

/**
 * Interface for writing the {@link Questionnaire} localization properties to different kind of medias.
 * @author Yannick Marcon
 * @see ILocalizable
 *
 */
public interface IPropertyKeyWriter {

  /**
   * Properties from which values can be fetched. 
   * @return
   */
  public Properties getReference();
  
  /**
   * Call for key value pair writing.
   * @param key
   * @param value
   */
  public void write(String key, String value);
  
  /**
   * Write property comments.
   * @param comments
   */
  public void writeComment(String... comments);
  
  /**
   * Questionnaire properties can be divided in several blocs, for fancy user reading.
   */
  public void endBloc();
  
  /**
   * Call when writing is finished.
   */
  public void end();
  
}
