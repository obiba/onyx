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
import java.util.Locale;

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
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Attributable;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Attributes;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.wicket.markup.html.table.IColumnProvider;

public class AttributesPanel extends Panel {

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private OnyxEntityList<FactorizedAttribute> attributes;

  private IModel<? extends Attributable> attributable;

  private ModalWindow modalWindow;

  private List<Locale> locales;

  public AttributesPanel(String id, final IModel<? extends Attributable> attributable,
      final List<Locale> locales, final FeedbackPanel feedbackPanel,
      final FeedbackWindow feedbackWindow) {
    super(id);
    this.feedbackPanel = feedbackPanel;
    this.feedbackWindow = feedbackWindow;
    this.attributable = attributable;
    this.locales = locales;

    modalWindow = new ModalWindow("modalWindow");
    modalWindow.setTitle(new ResourceModel("Attribute"));
    modalWindow.setCssClassName("onyx");
    modalWindow.setInitialWidth(500);
    modalWindow.setInitialHeight(250);
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
            new Model<FactorizedAttribute>(new FactorizedAttribute(locales)), locales, feedbackPanel,
            feedbackWindow);
        modalWindow.setContent(content);
        modalWindow.show(target);
      }
    };
    attributes = new OnyxEntityList<FactorizedAttribute>("attributes", new AttributesDataProvider(),
        new AttributeColumnProvider(), new ResourceModel("Attributes"));

    ajaxAddLink.add(new Image("addImage", Images.ADD));
    add(ajaxAddLink);
    add(attributes);
    add(modalWindow);
  }

  private class AttributesDataProvider extends SortableDataProvider<FactorizedAttribute> {

    @Override
    public Iterator<FactorizedAttribute> iterator(int first, int count) {
      Attributable attributableObject = attributable.getObject();
      return Attributes.factorize(attributableObject.getAttributes(), locales).iterator();
    }

    @Override
    public int size() {
      Attributable attributableObject = attributable.getObject();
      return Attributes.factorize(attributableObject.getAttributes(), locales).size();
    }

    @Override
    public IModel<FactorizedAttribute> model(final FactorizedAttribute object) {
      return new Model<FactorizedAttribute>(object);
    }
  }

  private class AttributeColumnProvider implements IColumnProvider<FactorizedAttribute>, Serializable {

    private final List<IColumn<FactorizedAttribute>> columns = new ArrayList<IColumn<FactorizedAttribute>>();

    public AttributeColumnProvider() {
      columns.add(new AbstractColumn<FactorizedAttribute>(new Model<String>("Name")) {
        @Override
        public void populateItem(Item<ICellPopulator<FactorizedAttribute>> cellItem, String componentId,
            IModel<FactorizedAttribute> rowModel) {
          FactorizedAttribute factorizedAttribute = rowModel.getObject();
          String formattedKey = Attributes.formatName(factorizedAttribute);
          cellItem.add(new Label(componentId, formattedKey));
        }
      });

      columns.add(new AbstractColumn<FactorizedAttribute>(new Model<String>("Value")) {
        @Override
        public void populateItem(Item<ICellPopulator<FactorizedAttribute>> cellItem, String componentId,
            IModel<FactorizedAttribute> rowModel) {
          FactorizedAttribute factorizedAttribute = rowModel.getObject();
          String formattedValue = Attributes.formatValue(factorizedAttribute);
          cellItem.add(new Label(componentId, formattedValue));
        }
      });

      columns.add(new HeaderlessColumn<FactorizedAttribute>() {
        @Override
        public void populateItem(Item<ICellPopulator<FactorizedAttribute>> cellItem, String componentId,
            IModel<FactorizedAttribute> rowModel) {
          cellItem.add(new LinkFragment(componentId, rowModel));
        }
      });
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<FactorizedAttribute>> getRequiredColumns() {
      return columns;
    }

    @Override
    public List<IColumn<FactorizedAttribute>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<FactorizedAttribute>> getAdditionalColumns() {
      return null;
    }
  }

  private class LinkFragment extends Fragment {

    public LinkFragment(String id, final IModel<FactorizedAttribute> factorizedAttributeModel) {
      super(id, "linkFragment", AttributesPanel.this, factorizedAttributeModel);
      AjaxLink<FactorizedAttribute> ajaxEditLink = new AjaxLink<FactorizedAttribute>("editAttribute",
          factorizedAttributeModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          AttributesEditPanel content = new AttributesEditPanel("content", attributable, factorizedAttributeModel,
              locales, feedbackPanel,
              feedbackWindow);
          modalWindow.setContent(content);
          modalWindow.show(target);
        }
      };
      ajaxEditLink.add(new Image("editImage", Images.EDIT));
      add(ajaxEditLink);
      AjaxLink<FactorizedAttribute> ajaxDeleteLink = new AjaxLink<FactorizedAttribute>("deleteAttribute",
          factorizedAttributeModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          FactorizedAttribute faObject = factorizedAttributeModel.getObject();
          attributable.getObject().removeAttributes(faObject.getNamespace(), faObject.getName());
          target.addComponent(attributes);
        }
      };
      ajaxDeleteLink.add(new Image("deleteImage", Images.DELETE));
      add(ajaxDeleteLink);
    }

  }
}
