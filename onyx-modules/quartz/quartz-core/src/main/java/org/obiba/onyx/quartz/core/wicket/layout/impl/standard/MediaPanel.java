/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.Strings;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.SupportedMedia;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.wicket.util.FileResource;

/**
 *
 */
@SuppressWarnings("serial")
public class MediaPanel extends Panel {

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean
  private QuestionnaireBundleManager bundleManager;

  /**
   * @param id
   * @param model
   */
  public MediaPanel(String id, IModel<String> model) {
    super(id, model);

    String mediaIds = model.getObject();
    if(Strings.isEmpty(mediaIds)) {
      add(new EmptyPanel("media"));
      return;
    }

    String bundleName = activeQuestionnaireAdministrationService.getQuestionnaire().getName();
    QuestionnaireBundle bundle = bundleManager.getBundle(bundleName);

    SupportedMedia media = null;
    List<FileResource> resources = new ArrayList<FileResource>();
    for(String mediaId : Strings.split(mediaIds, ',')) {
      SupportedMedia supportedMedia = SupportedMedia.detect(mediaId);
      if(media == null) {
        media = supportedMedia;
      } else if(media.getType() != supportedMedia.getType()) {
        add(new Label("media", new ResourceModel("UnsupportedMultipleMediaTypes")));
        return;
      }
      if(media != null) {
        try {
          String filePath = StringUtils.substringBefore(mediaId, "|");
          resources.add(new FileResource(bundle.getResource(filePath, media.getType()).getFile(), supportedMedia.getMimeType()));
        } catch(IOException ex) {
          // resource not found, nothing to do
        }
      }
    }
    if(media == null || resources.isEmpty()) {
      add(new EmptyPanel("media"));
    } else {
      switch(media.getType()) {
      case IMAGE:
        add(new ImageFragment("media", resources));
        break;
      case AUDIO:
        add(new AudioFragment("media", resources));
        break;
      case VIDEO:
        add(new VideoFragment("media", resources));
        break;
      }
    }
  }

  public class AudioVideoComponent extends WebComponent implements IResourceListener {

    private final FileResource resource;

    public AudioVideoComponent(final String id, final FileResource resource) {
      super(id);
      this.resource = resource;
    }

    public FileResource getResource() {
      return resource;
    }

    @Override
    public void onResourceRequested() {
      resource.onResourceRequested();
    }

    /**
     * @see org.apache.wicket.Component#initModel()
     */
    @Override
    protected IModel<?> initModel() {
      // Images don't support Compound models. They either have a simple
      // model, explicitly set, or they use their tag's src or value
      // attribute to determine the image.
      return null;
    }

    /**
     * @see org.apache.wicket.Component#getStatelessHint()
     */
    @Override
    protected boolean getStatelessHint() {
      return false;
    }

    /**
     * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
     */
    @Override
    protected void onComponentTag(final ComponentTag tag) {
      checkComponentTag(tag, "source");
      super.onComponentTag(tag);
      String url = (String) urlFor(INTERFACE);
      tag.put("src", RequestCycle.get().getOriginalResponse().encodeURL(Strings.replaceAll(url, "&", "&amp;")));
      tag.put("type", getResource().getContentType());
    }
  }

  private class ImageFragment extends Fragment {

    public ImageFragment(String id, List<FileResource> resources) {
      super(id, "imageFragment", MediaPanel.this);
      add(new ListView<FileResource>("images", resources) {
        @Override
        protected void populateItem(ListItem<FileResource> item) {
          item.add(new Image("image", item.getModel()));
        }
      });
    }
  }

  private class AudioFragment extends Fragment {

    public AudioFragment(String id, List<FileResource> resources) {
      super(id, "audioFragment", MediaPanel.this);
      add(new ListView<FileResource>("audio-source-container", resources) {
        @Override
        protected void populateItem(ListItem<FileResource> item) {
          item.add(new AudioVideoComponent("audio-source", item.getModelObject()));
        }
      });
    }
  }

  private class VideoFragment extends Fragment {

    public VideoFragment(String id, List<FileResource> resources) {
      super(id, "videoFragment", MediaPanel.this);
      add(new ListView<FileResource>("video-source-container", resources) {
        @Override
        protected void populateItem(ListItem<FileResource> item) {
          item.add(new AudioVideoComponent("video-source", item.getModelObject()));
        }
      });
    }
  }

}
