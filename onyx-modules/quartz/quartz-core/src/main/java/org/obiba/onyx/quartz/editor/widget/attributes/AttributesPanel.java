/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.widget.attributes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Strings;
import com.google.common.collect.Iterators;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.obiba.magma.Attribute;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Attributable;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.wicket.markup.html.table.IColumnProvider;

public class AttributesPanel extends Panel {

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private OnyxEntityList<Attribute> attributes;

  private IModel<? extends Attributable> attributable;

  private ModalWindow modalWindow;

  public AttributesPanel(String id, final IModel<? extends Attributable> attributable,
      final FeedbackPanel feedbackPanel,
      final FeedbackWindow feedbackWindow) {
    super(id);
    this.feedbackPanel = feedbackPanel;
    this.feedbackWindow = feedbackWindow;
    this.attributable = attributable;
    modalWindow = new ModalWindow("modalWindow");
    modalWindow.setCssClassName("onyx");
    modalWindow.setInitialWidth(500);
    modalWindow.setInitialHeight(500);
    modalWindow.setResizable(true);
    modalWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      @Override
      public void onClose(AjaxRequestTarget target) {
        target.addComponent(attributes);
      }
    });

    AjaxLink<Serializable> ajaxAddLink = new AjaxLink<Serializable>("addAttribute", new Model<Serializable>()) {
      @Override
      public void onClick(AjaxRequestTarget target) {
        AttributesEditPanel content = new AttributesEditPanel("content", attributable,
            new LoadableDetachableModel<Attribute>() {
              @Override
              protected Attribute load() {
                return Attribute.Builder.newAttribute().build();
              }
            }, feedbackPanel, feedbackWindow);
        modalWindow.setContent(content);
        modalWindow.show(target);
      }
    };
    attributes = new OnyxEntityList<Attribute>("attributes", new AttributesDataProvider(),
        new AttributeColumnProvider(), new Model<String>("Attributes"));

    ajaxAddLink.add(new Image("addImage", Images.ADD));
    add(ajaxAddLink);
    add(attributes);
    add(modalWindow);
  }

  private class AttributesDataProvider extends SortableDataProvider<Attribute> {

    @Override
    public Iterator<? extends Attribute> iterator(int first, int count) {
      Attributable attributableObject = attributable.getObject();
      if(attributableObject.hasAttributes()) {
        return attributableObject.getAttributes().iterator();
      } else {
        return Iterators.emptyIterator();
      }
    }

    @Override
    public int size() {
      Attributable attributableObject = attributable.getObject();
      if(attributableObject.hasAttributes()) {
        return attributableObject.getAttributes().size();
      } else {
        return 0;
      }
    }

    @Override
    public IModel<Attribute> model(final Attribute object) {
      return new LoadableDetachableModel<Attribute>() {
        @Override
        protected Attribute load() {
          return object;
        }
      };
    }
  }

  private class AttributeColumnProvider implements IColumnProvider<Attribute>, Serializable {

    private final List<IColumn<Attribute>> columns = new ArrayList<IColumn<Attribute>>();

    public AttributeColumnProvider() {
      columns.add(new AbstractColumn<Attribute>(new Model<String>("Name")) {
        @Override
        public void populateItem(Item<ICellPopulator<Attribute>> cellItem, String componentId,
            IModel<Attribute> rowModel) {
          Attribute attribute = rowModel.getObject();
          String formattedNS = "";
          if(Strings.isNullOrEmpty(attribute.getNamespace()) == false) {
            formattedNS = "{" + attribute.getNamespace() + "}";
          }
          cellItem.add(new Label(componentId, formattedNS + " " + attribute.getName()));
        }
      });

      columns.add(new AbstractColumn<Attribute>(new Model<String>("Value")) {
        @Override
        public void populateItem(Item<ICellPopulator<Attribute>> cellItem, String componentId,
            IModel<Attribute> rowModel) {
          Attribute attribute = rowModel.getObject();
          String formattedLocale = "";
          if(attribute.getLocale() != null) {
            formattedLocale = "{" + attribute.getLocale().toString() + "}";
          }
          cellItem.add(new Label(componentId, formattedLocale + " " + attribute.getValue().getValue()));
        }
      });

      columns.add(new HeaderlessColumn<Attribute>() {
        @Override
        public void populateItem(Item<ICellPopulator<Attribute>> cellItem, String componentId,
            IModel<Attribute> rowModel) {
          cellItem.add(new LinkFragment(componentId, rowModel));
        }
      });
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<Attribute>> getRequiredColumns() {
      return columns;
    }

    @Override
    public List<IColumn<Attribute>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<Attribute>> getAdditionalColumns() {
      return null;
    }
  }

  private class LinkFragment extends Fragment {

    public LinkFragment(String id, final IModel<Attribute> model) {
      super(id, "linkFragment", AttributesPanel.this, model);
      AjaxLink<Attribute> ajaxEditLink = new AjaxLink<Attribute>("editAttribute", model) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          AttributesEditPanel content = new AttributesEditPanel("content", attributable, model, feedbackPanel,
              feedbackWindow);
          modalWindow.setContent(content);
          modalWindow.show(target);
        }
      };
      ajaxEditLink.add(new Image("editImage", Images.EDIT));
      add(ajaxEditLink);
      AjaxLink<Attribute> ajaxDeleteLink = new AjaxLink<Attribute>("deleteAttribute", model) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          attributable.getObject().removeAttribute(model.getObject());
          target.addComponent(attributes);
        }
      };
      ajaxDeleteLink.add(new Image("deleteImage", Images.DELETE));
      add(ajaxDeleteLink);
    }

  }
}
