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

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.odlabs.wiquery.ui.sortable.SortableAjaxBehavior;

@SuppressWarnings("serial")
public abstract class SortableList<T> extends Panel {

  private WebMarkupContainer listContainer;

  public SortableList(String id, List<T> items) {
    super(id);

    add(CSSPackageResource.getHeaderContribution(SortableList.class, "SortableList.css"));

    listContainer = new WebMarkupContainer("listContainer");
    listContainer.setOutputMarkupId(true);
    SortableAjaxBehavior sortableAjaxBehavior = new SortableAjaxBehavior() {

      @Override
      public void onUpdate(Component sortedComponent, int index, AjaxRequestTarget ajaxRequestTarget) {
        SortableList.this.onUpdate(sortedComponent, index, ajaxRequestTarget);
      }

      @Override
      public void onReceive(Component sortedComponent, int index, Component parentSortedComponent, AjaxRequestTarget ajaxRequestTarget) {
        SortableList.this.onReceive(sortedComponent, index, parentSortedComponent, ajaxRequestTarget);
      }

      @Override
      public void onRemove(Component sortedComponent, AjaxRequestTarget ajaxRequestTarget) {
        SortableList.this.onRemove(sortedComponent, ajaxRequestTarget);
      }
    };
    sortableAjaxBehavior.getSortableBehavior().setPlaceholder("ui-state-highlight");
    listContainer.add(sortableAjaxBehavior);

    ListView<T> listView = new ListView<T>("listView", items) {
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
      }
    };
    listContainer.add(listView);
    add(listContainer);

    add(new AjaxLink<Void>("addItem") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        addItem(target);
      }
    });
  }

  public void refreshList(AjaxRequestTarget target) {
    target.addComponent(listContainer);
  }

  /**
   * On add via drag n drop
   * @param sortedComponent
   * @param index
   * @param parentSortedComponent
   * @param target
   */
  public void onReceive(Component sortedComponent, int index, Component parentSortedComponent, AjaxRequestTarget target) {
  }

  /**
   * On remove via drag n drop
   * @param sortedComponent
   * @param target
   */
  public void onRemove(Component sortedComponent, AjaxRequestTarget target) {
  }

  public abstract String getItemLabel(T t);

  public abstract void onUpdate(Component sortedComponent, int index, AjaxRequestTarget target);

  public abstract void addItem(AjaxRequestTarget target);

  public abstract void editItem(T t, AjaxRequestTarget target);

  public abstract void deleteItem(T t, AjaxRequestTarget target);

}
