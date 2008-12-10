/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.array;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * Question array build around columns definition and row provider: builds the headers, not the cell content.
 */
public abstract class AbstractQuestionArray extends Panel {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor building the headers from the given column headers, and call building cells content.
   * @param id
   * @param questionModel
   * @param columns
   * @param rows
   */
  public AbstractQuestionArray(String id, IModel questionModel, List<IColumn> columns, IDataProvider rows) {
    super(id, questionModel);

    RepeatingView headers = new RepeatingView("headers");
    for(IColumn column : columns) {
      WebMarkupContainer item = new WebMarkupContainer(headers.newChildId());
      headers.add(item);

      WebMarkupContainer header = new WebMarkupContainer("header");
      if(column instanceof IStyledColumn && ((IStyledColumn) column).getCssClass() != null) {
        header.add(new AttributeAppender("class", new PropertyModel(column, "cssClass"), " "));
      }

      item.add(header);
      item.setRenderBodyOnly(true);
      header.add(column.getHeader("label"));
    }
    add(headers);
    add(getRowsContent("rows", columns, rows));
  }

  /**
   * To be implemented for building array cells.
   * @param id
   * @param columns
   * @param rows
   * @return
   */
  public abstract Component getRowsContent(String id, List<IColumn> columns, IDataProvider rows);

}
