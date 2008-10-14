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
   * Questionnaire properties can be divided in several blocs, for fancy user reading.
   */
  public void endBloc();
  
  /**
   * Call when writing is finished.
   */
  public void end();
  
}
