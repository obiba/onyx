/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.resource.ByteArrayResource;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.string.Strings;
import org.obiba.magma.Value;
import org.obiba.magma.type.BinaryType;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.SupportedMedia;
import org.obiba.onyx.wicket.util.ContentTypedWebResource;
import org.obiba.onyx.wicket.util.FileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class QuestionnaireWebResourceModel extends QuestionnaireResourceModel<List<ContentTypedWebResource>> {

  private static final long serialVersionUID = 1L;

  private static Logger logger = LoggerFactory.getLogger(QuestionnaireWebResourceModel.class);

  /**
   * @param localizableModel
   * @param property
   * @param stringArgs
   */
  public QuestionnaireWebResourceModel(IModel<? extends IQuestionnaireElement> localizableModel, String property, String... stringArgs) {
    super(localizableModel, property, stringArgs);
  }

  /**
   * @param localizable
   * @param property
   * @param stringArgs
   */
  public QuestionnaireWebResourceModel(IQuestionnaireElement localizable, String property, String... stringArgs) {
    super(localizable, property, stringArgs);
  }

  @Override
  protected List<ContentTypedWebResource> load() {

    String message = resolveMessage();

    List<ContentTypedWebResource> resources = new ArrayList<ContentTypedWebResource>();
    if(StringUtils.isEmpty(message) || activeQuestionnaireAdministrationService.isQuestionnaireDevelopmentMode()) {
      return resources;
    }

    if(message.startsWith("$('")) {

      String variablePath = message;
      String mimeType = null;
      if(message.contains("|")) {
        variablePath = StringUtils.substringBefore(message, "|");
        mimeType = StringUtils.substringAfter(message, "|");
      }

      String path = message.substring(3, variablePath.indexOf("')"));
      VariableDataSource variableDataSource = new VariableDataSource(path);
      if(StringUtils.isEmpty(mimeType)) {
        mimeType = variableDataSource.getVariable().getMimeType();
      }

      final Value value = variableDataSource.getValue(activeQuestionnaireAdministrationService.getQuestionnaireParticipant().getParticipant());
      if(value.getValueType().equals(BinaryType.get())) {
        resources.add(new ContentTypedWebResource(mimeType) {
          private static final long serialVersionUID = 1L;

          @Override
          public IResourceStream getResourceStream() {
            return new ByteArrayResource(getContentType(), (byte[]) value.getValue()).getResourceStream();
          }
        });
      } else {
        logger.error("Variable '{0}' is not binary", path);
      }

    } else {

      String bundleName = activeQuestionnaireAdministrationService.getQuestionnaire().getName();
      QuestionnaireBundle bundle = bundleManager.getBundle(bundleName);
      SupportedMedia media = null;
      for(String mediaId : Strings.split(message, ',')) {
        SupportedMedia supportedMedia = SupportedMedia.resolveFromPath(mediaId);
        if(media == null) {
          media = supportedMedia;
        } else if(media.getType() != supportedMedia.getType()) {
          throw new InvalidMultipleMediaTypesException("Multiple media sources must be the same media type: " + message);
        }
        if(media != null) {
          String filePath = StringUtils.substringBefore(mediaId, "|");
          try {
            resources.add(new FileResource(bundle.getResource(filePath, media.getType()).getFile(), supportedMedia.getMimeType()));
          } catch(IOException ex) {
            logger.error("Resource not found for '{0}'", filePath, ex);
          }
        }
      }
    }
    return resources;
  }
}
