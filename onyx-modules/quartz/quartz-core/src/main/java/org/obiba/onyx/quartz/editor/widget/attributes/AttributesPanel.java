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
import java.util.Map;

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

  private OnyxEntityList<FactorizedAttributeModel> attributes;

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
            new Model<FactorizedAttributeModel>(new FactorizedAttributeModel(locales)), locales, feedbackPanel,
            feedbackWindow);
        modalWindow.setContent(content);
        modalWindow.show(target);
      }
    };
    attributes = new OnyxEntityList<FactorizedAttributeModel>("attributes", new AttributesDataProvider(),
        new AttributeColumnProvider(), new ResourceModel("Attributes"));

    ajaxAddLink.add(new Image("addImage", Images.ADD));
    add(ajaxAddLink);
    add(attributes);
    add(modalWindow);
  }

  private class AttributesDataProvider extends SortableDataProvider<FactorizedAttributeModel> {

    @Override
    public Iterator<FactorizedAttributeModel> iterator(int first, int count) {
      Attributable attributableObject = attributable.getObject();
      if(attributableObject.hasAttributes()) {
        return Attributes.factorize(attributableObject.getAttributes(), locales).iterator();
      } else {
        return Iterators.emptyIterator();
      }
    }

    @Override
    public int size() {
      Attributable attributableObject = attributable.getObject();
      if(attributableObject.hasAttributes()) {
        return Attributes.factorize(attributableObject.getAttributes(), locales).size();
      } else {
        return 0;
      }
    }

    @Override
    public IModel<FactorizedAttributeModel> model(final FactorizedAttributeModel object) {
      return new Model<FactorizedAttributeModel>(object);
    }
  }

  private class AttributeColumnProvider implements IColumnProvider<FactorizedAttributeModel>, Serializable {

    private final List<IColumn<FactorizedAttributeModel>> columns = new ArrayList<IColumn<FactorizedAttributeModel>>();

    public AttributeColumnProvider() {
      columns.add(new AbstractColumn<FactorizedAttributeModel>(new Model<String>("Name")) {
        @Override
        public void populateItem(Item<ICellPopulator<FactorizedAttributeModel>> cellItem, String componentId,
            IModel<FactorizedAttributeModel> rowModel) {
          FactorizedAttributeModel attribute = rowModel.getObject();
          String formattedNS = "";
          if(Strings.isNullOrEmpty(attribute.getNamespace()) == false) {
            formattedNS = "{" + attribute.getNamespace() + "}";
          }
          cellItem.add(new Label(componentId, formattedNS + " " + attribute.getName()));
        }
      });

      columns.add(new AbstractColumn<FactorizedAttributeModel>(new Model<String>("Value")) {
        @Override
        public void populateItem(Item<ICellPopulator<FactorizedAttributeModel>> cellItem, String componentId,
            IModel<FactorizedAttributeModel> rowModel) {
          FactorizedAttributeModel factorizedAttributeModel = rowModel.getObject();
          String formattedValue = "";
          for(Map.Entry<Locale, IModel<String>> entry : factorizedAttributeModel.getValues().entrySet()) {
            String value = entry.getValue().getObject();
            if(Strings.isNullOrEmpty(value) == false) {
              String formattedLocale = entry.getKey() == null ? "" : " {" + entry.getKey() + "} ";
              formattedValue += (formattedLocale + value);
            }
          }
          cellItem.add(new Label(componentId, formattedValue));
        }
      });

      columns.add(new HeaderlessColumn<FactorizedAttributeModel>() {
        @Override
        public void populateItem(Item<ICellPopulator<FactorizedAttributeModel>> cellItem, String componentId,
            IModel<FactorizedAttributeModel> rowModel) {
          cellItem.add(new LinkFragment(componentId, rowModel));
        }
      });
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<FactorizedAttributeModel>> getRequiredColumns() {
      return columns;
    }

    @Override
    public List<IColumn<FactorizedAttributeModel>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<FactorizedAttributeModel>> getAdditionalColumns() {
      return null;
    }
  }

  private class LinkFragment extends Fragment {

    public LinkFragment(String id, final IModel<FactorizedAttributeModel> factorizedAttributeModel) {
      super(id, "linkFragment", AttributesPanel.this, factorizedAttributeModel);
      AjaxLink<FactorizedAttributeModel> ajaxEditLink = new AjaxLink<FactorizedAttributeModel>("editAttribute",
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
      AjaxLink<FactorizedAttributeModel> ajaxDeleteLink = new AjaxLink<FactorizedAttributeModel>("deleteAttribute",
          factorizedAttributeModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          FactorizedAttributeModel faObject = factorizedAttributeModel.getObject();
          attributable.getObject().removeAttributes(faObject.getNamespace(), faObject.getName());
          target.addComponent(attributes);
        }
      };
      ajaxDeleteLink.add(new Image("deleteImage", Images.DELETE));
      add(ajaxDeleteLink);
    }

  }
}
