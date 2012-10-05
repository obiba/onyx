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

import java.util.List;

import org.apache.wicket.IResourceListener;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.Strings;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.SupportedMedia;
import org.obiba.onyx.wicket.util.ContentTypedWebResource;

/**
 *
 */
@SuppressWarnings("serial")
public class MediaPanel extends Panel {

  // private static Logger logger = LoggerFactory.getLogger(MediaPanel.class);

  /**
   * @param id
   * @param model
   */
  public MediaPanel(String id, IModel<List<ContentTypedWebResource>> model) {
    super(id, model);

    List<ContentTypedWebResource> resources = model.getObject();
    if(resources == null || resources.isEmpty()) {
      add(new EmptyPanel("media"));
    } else {
      SupportedMedia media = SupportedMedia.resolveFromMimeType(resources.get(0).getContentType());
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

    private final ContentTypedWebResource resource;

    public AudioVideoComponent(final String id, final ContentTypedWebResource resource) {
      super(id);
      this.resource = resource;
      this.resource.setCacheable(false);
    }

    public ContentTypedWebResource getResource() {
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
      String url = (String) urlFor(INTERFACE).toString();
      tag.put("src", RequestCycle.get().getOriginalResponse().encodeURL(Strings.replaceAll(url, "&", "&amp;")));
      tag.put("type", getResource().getContentType());
    }
  }

  private class ImageFragment extends Fragment {

    public ImageFragment(String id, List<? extends Resource> resources) {
      super(id, "imageFragment", MediaPanel.this);
      add(new ListView<Resource>("images", resources) {
        @Override
        protected void populateItem(ListItem<Resource> item) {
          item.add(new Image("image", item.getModel()));
        }
      });
    }
  }

  private class AudioFragment extends Fragment {

    public AudioFragment(String id, List<ContentTypedWebResource> resources) {
      super(id, "audioFragment", MediaPanel.this);
      add(new ListView<ContentTypedWebResource>("audio-source-container", resources) {
        @Override
        protected void populateItem(ListItem<ContentTypedWebResource> item) {
          item.add(new AudioVideoComponent("audio-source", item.getModelObject()));
        }
      });
    }
  }

  private class VideoFragment extends Fragment {

    public VideoFragment(String id, List<ContentTypedWebResource> resources) {
      super(id, "videoFragment", MediaPanel.this);
      add(new ListView<ContentTypedWebResource>("video-source-container", resources) {
        @Override
        protected void populateItem(ListItem<ContentTypedWebResource> item) {
          item.add(new AudioVideoComponent("video-source", item.getModelObject()));
        }
      });
    }
  }

}
