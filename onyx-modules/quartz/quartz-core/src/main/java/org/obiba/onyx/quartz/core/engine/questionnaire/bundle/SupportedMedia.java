/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.bundle;

import static org.obiba.onyx.quartz.core.engine.questionnaire.bundle.SupportedMedia.MediaType.AUDIO;
import static org.obiba.onyx.quartz.core.engine.questionnaire.bundle.SupportedMedia.MediaType.IMAGE;
import static org.obiba.onyx.quartz.core.engine.questionnaire.bundle.SupportedMedia.MediaType.VIDEO;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.util.string.Strings;

/**
 *
 */
public enum SupportedMedia {

  IMAGE_GIF(IMAGE, "image/gif", "gif"), //
  IMAGE_PNG(IMAGE, "image/png", "png"), //
  IMAGE_JPEG(IMAGE, "image/jpeg", "jpg, jpeg"), //

  AUDIO_WAVE(AUDIO, "audio/x-wav", "wav"), //
  AUDIO_OGG(AUDIO, "audio/ogg", "oga"), //
  AUDIO_MPEG(AUDIO, "audio/mpeg", "mp3"), //
  AUDIO_MP4(AUDIO, "audio/mp4", "m4a"), //

  VIDEO_WEBM(VIDEO, "video/webm", "webm"), //
  VIDEO_OGG(VIDEO, "video/ogg", "ogv"), //
  VIDEO_MP4(VIDEO, "video/mp4", "mp4");

  public enum MediaType {
    IMAGE, AUDIO, VIDEO;
  }

  private MediaType type;

  private String mimeType;

  private String[] extensions;

  /**
   * @param mimeType
   */
  private SupportedMedia(MediaType type, String mimeType, String... extensions) {
    this.type = type;
    this.mimeType = mimeType;
    this.extensions = extensions;
  }

  public MediaType getType() {
    return type;
  }

  public String getMimeType() {
    return mimeType;
  }

  public String[] getExtensions() {
    return extensions;
  }

  public static SupportedMedia resolveFromMimeType(String mimeType) {
    if(Strings.isEmpty(mimeType)) return null;
    for(SupportedMedia supportedMedia : SupportedMedia.values()) {
      if(StringUtils.equalsIgnoreCase(supportedMedia.getMimeType(), mimeType)) {
        return supportedMedia;
      }
    }
    return null;
  }

  public static SupportedMedia resolveFromPath(String media) {
    if(Strings.isEmpty(media)) return null;
    String[] splits = Strings.split(media, '|');
    if(splits.length == 1) {
      String[] fileNameSplits = Strings.split(splits[0], '.');
      String ext = fileNameSplits.length == 2 ? fileNameSplits[1] : null;
      if(ext != null) {
        for(SupportedMedia supportedMedia : SupportedMedia.values()) {
          if(supportedMedia.getExtensions() != null) {
            for(String e : supportedMedia.getExtensions()) {
              if(StringUtils.equalsIgnoreCase(e, ext)) return supportedMedia;
            }
          }
        }
      }
      return null;
    }
    return resolveFromMimeType(splits[1]);
  }
}
