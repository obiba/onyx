/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.widget.sortable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.collections.MiniMap;
import org.apache.wicket.util.template.JavaScriptTemplate;
import org.apache.wicket.util.template.PackagedTextTemplate;
import org.obiba.onyx.wicket.Images;
import org.springframework.util.StringUtils;

@SuppressWarnings("serial")
public abstract class SortableList<T extends Serializable> extends Panel {

  private WebMarkupContainer listContainer;

  private final Map<String, T> itemByMarkupId = new HashMap<String, T>();

  private AbstractDefaultAjaxBehavior toArrayBehavior;

  public SortableList(String id, List<T> items) {
    this(id, Model.ofList(items));
  }

  public SortableList(String id, List<T> items, boolean hideEditButton) {
    this(id, Model.ofList(items), hideEditButton);
  }

  public SortableList(String id, IModel<? extends List<? extends T>> model) {
    this(id, model, false);
  }

  @SuppressWarnings("unchecked")
  public SortableList(String id, IModel<? extends List<? extends T>> model, final boolean hideEditButton) {
    super(id, model);

    add(CSSPackageResource.getHeaderContribution(SortableList.class, "SortableList.css"));

    ListView<T> listView = new ListView<T>("listView", model) {

      @Override
      protected void populateItem(ListItem<T> item) {
        item.setOutputMarkupId(true);
        final T t = item.getModelObject();

        item.add(getItemTitle("item", t));

        Image editImg = new Image("editImg", Images.EDIT);
        editImg.add(new AttributeModifier("title", true, new ResourceModel("Edit")));
        AjaxLink<Void> editAjaxLink = new AjaxLink<Void>("editItem") {
          @Override
          public void onClick(AjaxRequestTarget target) {
            editItem(t, target);
          }
        };
        editAjaxLink.setVisible(!hideEditButton);
        item.add(editAjaxLink.add(editImg));

        Image deleteImg = new Image("deleteImg", Images.DELETE);
        deleteImg.add(new AttributeModifier("title", true, new ResourceModel("Delete")));
        item.add(new AjaxLink<Void>("deleteItem") {
          @Override
          public void onClick(AjaxRequestTarget target) {
            deleteItem(t, target);
          }
        }.add(deleteImg));

        itemByMarkupId.put(item.getMarkupId(), t);
      }
    };

    listContainer = new WebMarkupContainer("listContainer") {
      @Override
      protected void onBeforeRender() {
        super.onBeforeRender();

        final IModel<Map<String, Object>> variablesModel = new AbstractReadOnlyModel<Map<String, Object>>() {
          private Map<String, Object> variables;

          @Override
          public Map<String, Object> getObject() {
            if(variables == null) {
              this.variables = new MiniMap<String, Object>(2);
              variables.put("listMarkupId", listContainer.getMarkupId());
              variables.put("callbackUrl", toArrayBehavior.getCallbackUrl());
            }
            return variables;
          }
        };
        PackagedTextTemplate jsTemplate = new PackagedTextTemplate(SortableList.class, "SortableList.js");
        final Label jsScript = new Label("sortableJs", new JavaScriptTemplate(jsTemplate).asString(variablesModel.getObject()));
        jsScript.setEscapeModelStrings(false);
        listContainer.addOrReplace(jsScript);
      }
    };
    listContainer.add(toArrayBehavior = new ToArrayBehavior());
    listContainer.setOutputMarkupId(true);
    listContainer.add(listView);
    add(listContainer);

    listContainer.add(new AbstractBehavior() {
      @Override
      public void renderHead(IHeaderResponse response) {
        response.renderOnLoadJavascript("Wicket.Sortable.create" + listContainer.getMarkupId() + "('" + listContainer.getMarkupId() + "')");
      }
    });

    add(new ListView<Button>("buttons", getButtons() == null ? Collections.EMPTY_LIST : Arrays.asList(getButtons())) {
      @Override
      protected void populateItem(ListItem<Button> item) {
        item.add(new ButtonFragment("button", item.getModel()));
      }
    });
    listContainer.addOrReplace(new WebMarkupContainer("sortableJs"));
  }

  public void refreshList(AjaxRequestTarget target) {
    target.addComponent(listContainer);
  }

  protected class ToArrayBehavior extends AbstractDefaultAjaxBehavior {
    @Override
    protected void respond(AjaxRequestTarget target) {
      Request request = RequestCycle.get().getRequest();
      String items = request.getParameter("items");
      @SuppressWarnings("unchecked")
      List<T> modelList = (List<T>) SortableList.this.getDefaultModelObject();
      modelList.clear();
      for(String markupId : StringUtils.commaDelimitedListToStringArray(items)) {
        if(org.apache.commons.lang.StringUtils.isNotBlank(markupId)) modelList.add(itemByMarkupId.get(markupId));
      }
    }
  }

  /**
   * 
   * @param t
   */
  public void onItemPopulation(T t) {

  }

  public abstract Component getItemTitle(String id, T t);

  public abstract void editItem(T t, AjaxRequestTarget target);

  public abstract void deleteItem(T t, AjaxRequestTarget target);

  public abstract Button[] getButtons();

  public abstract class Button implements Serializable {

    private IModel<String> title;

    private ResourceReference image;

    public Button(IModel<String> title, ResourceReference image) {
      this.title = title;
      this.image = image;
    }

    public Button(IModel<String> title) {
      this(title, null);
    }

    public abstract void callback(AjaxRequestTarget target);

    public IModel<String> getTitle() {
      return title;
    }

    public ResourceReference getImage() {
      return image;
    }

  }

  public class ButtonFragment extends Fragment {

    public ButtonFragment(String id, IModel<Button> model) {
      super(id, "buttonFragment", SortableList.this, model);
      final Button button = model.getObject();
      AjaxLink<Void> ajaxLink = new AjaxLink<Void>("button") {
        @Override
        public void onClick(AjaxRequestTarget target) {
          button.callback(target);
        }
      };
      Image image = new Image("buttonImg", button.getImage());
      image.setVisible(button.getImage() != null);
      ajaxLink.add(image);
      ajaxLink.add(new Label("buttonLabel", button.getTitle()));
      add(ajaxLink);
    }
  }
}
