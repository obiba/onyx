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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.springframework.util.StringUtils;

@SuppressWarnings("serial")
public abstract class SortableList<T extends Serializable> extends Panel {

  private WebMarkupContainer listContainer;

  private final Map<String, T> itemByMarkupId = new HashMap<String, T>();

  private AbstractDefaultAjaxBehavior toArrayBehavior;

  private Label toArrayCallback;

  private SortableListCallback<T> callback;

  public SortableList(String id, List<T> items) {
    this(id, Model.ofList(items));
  }

  public SortableList(String id, IModel<? extends List<? extends T>> model) {
    super(id);

    add(CSSPackageResource.getHeaderContribution(SortableList.class, "SortableList.css"));
    add(JavascriptPackageResource.getHeaderContribution(SortableList.class, "SortableList.js"));

    ListView<T> listView = new ListView<T>("listView", model) {
      @Override
      protected void populateItem(ListItem<T> item) {
        item.setOutputMarkupId(true);
        final T t = item.getModelObject();
        item.add(new Label("item", getItemLabel(t)));
        item.add(new AjaxLink<Void>("editItem") {
          @Override
          public void onClick(AjaxRequestTarget target) {
            editItem(t, target);
          }
        });
        item.add(new AjaxLink<Void>("deleteItem") {
          @Override
          public void onClick(AjaxRequestTarget target) {
            deleteItem(t, target);
          }
        });
        itemByMarkupId.put(item.getMarkupId(), t);
      }
    };

    listContainer = new WebMarkupContainer("listContainer");
    listContainer.setOutputMarkupId(true);
    listContainer.add(listView);
    add(listContainer);

    listContainer.add(new AbstractBehavior() {
      @Override
      public void renderHead(IHeaderResponse response) {
        response.renderOnLoadJavascript("Wicket.Sortable.create('" + listContainer.getMarkupId() + "')");
      }
    });

    add(new ListView<Button>("buttons", Arrays.asList(getButtons())) {
      protected void populateItem(ListItem<Button> item) {
        item.add(new ButtonFragment("button", item.getModel()));
      }
    });

    add(toArrayBehavior = new ToArrayBehavior());
    toArrayCallback = new Label("toArrayCallback", "");
    toArrayCallback.setOutputMarkupId(true);
    toArrayCallback.setEscapeModelStrings(false);
    add(toArrayCallback);
  }

  public void refreshList(AjaxRequestTarget target) {
    target.addComponent(listContainer);
  }

  @Override
  protected void onBeforeRender() {
    super.onBeforeRender();
    toArrayCallback.setDefaultModelObject("Wicket.Sortable.toStringArray = function(items) {\n" + //
    "  wicketAjaxGet('" + toArrayBehavior.getCallbackUrl(true) + "&items='+ items, function() { }, function() { alert('Cannot communicate with server...'); });" + //
    "\n}");
  }

  public void save(AjaxRequestTarget target, SortableListCallback<T> callback1) {
    this.callback = callback1;
    target.appendJavascript("Wicket.Sortable.toArray('" + listContainer.getMarkupId() + "')");
  }

  protected class ToArrayBehavior extends AbstractDefaultAjaxBehavior {
    @Override
    protected void respond(AjaxRequestTarget target) {
      Request request = RequestCycle.get().getRequest();
      String items = request.getParameter("items");
      List<T> orderedItems = new ArrayList<T>();
      for(String markupId : StringUtils.commaDelimitedListToStringArray(items)) {
        orderedItems.add(itemByMarkupId.get(markupId));
      }
      callback.onSave(orderedItems, target);
    }
  }

  public abstract String getItemLabel(T t);

  public abstract void editItem(T t, AjaxRequestTarget target);

  public abstract void deleteItem(T t, AjaxRequestTarget target);

  public abstract Button[] getButtons();

  public abstract class Button implements Serializable {

    private IModel<String> title;

    public Button(IModel<String> title) {
      this.title = title;
    }

    public abstract void callback(AjaxRequestTarget target);

    public IModel<String> getTitle() {
      return title;
    }

  }

  public class ButtonFragment extends Fragment {

    public ButtonFragment(String id, IModel<Button> model) {
      super(id, "buttonFragment", SortableList.this, model);
      final Button button = model.getObject();
      add(new AjaxLink<Void>("button") {
        @Override
        public void onClick(AjaxRequestTarget target) {
          button.callback(target);
        }
      }.add(new Label("buttonLabel", button.getTitle())));
    }

  }

}
