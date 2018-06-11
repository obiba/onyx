/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;

import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.SupportedMedia;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition.OpenAnswerType;
import org.obiba.onyx.util.data.DataType;
import org.springframework.util.Assert;

/**
 *
 */
public class OpenAnswerDefinitionAudio implements Serializable {

  public static final String SAMPLING_RATE_KEY = "audio.samplingRate";

  public static final String MAX_DURATION_KEY = "audio.maxDuration";

  private static final int DEFAULT_MAX_DURATION = 1200;

  private final OpenAnswerDefinition openAnswer;

  public OpenAnswerDefinitionAudio(OpenAnswerDefinition openAnswer) {
    Assert.notNull(openAnswer);
    this.openAnswer = openAnswer;
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static OpenAnswerDefinitionAudio createOpenAnswerDefinitionAudio() {
    OpenAnswerDefinition openAnswerDefinition = new OpenAnswerDefinition();
    openAnswerDefinition.addUIArgument(OpenAnswerType.UI_ARGUMENT_KEY, OpenAnswerType.AUDIO_RECORDING.getUiArgument());
    return new OpenAnswerDefinitionAudio(openAnswerDefinition);
  }

  public void configureAudioOpenAnswerDefinition() {
    openAnswer.setDataType(DataType.DATA);
    openAnswer.setUnit(SupportedMedia.AUDIO_WAVE.getMimeType());
    openAnswer.clearUIArgument();
    openAnswer.addUIArgument(OpenAnswerType.UI_ARGUMENT_KEY, OpenAnswerType.AUDIO_RECORDING.getUiArgument());
  }

  public int getMaxDuration() {
    ValueMap valueMap = openAnswer.getUIArgumentsValueMap();
    return valueMap == null ? DEFAULT_MAX_DURATION : valueMap.getAsInteger(MAX_DURATION_KEY, DEFAULT_MAX_DURATION);
  }

  public void setMaxDuration(Integer maxDuration) {
    if(maxDuration == null) {
      openAnswer.removeUIArgument(MAX_DURATION_KEY);
    } else {
      openAnswer.replaceUIArgument(MAX_DURATION_KEY, String.valueOf(maxDuration));
    }
  }

  public OpenAnswerDefinition getOpenAnswer() {
    return openAnswer;
  }
}
