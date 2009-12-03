/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.administration.panel;

import java.io.IOException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.Variable;
import org.obiba.magma.ValueTableWriter.ValueSetWriter;
import org.obiba.magma.ValueTableWriter.VariableWriter;
import org.obiba.magma.io.FsDatasource;
import org.obiba.onyx.webapp.OnyxApplication;
import org.obiba.wicket.hibernate.HibernateStatisticsPanel;

/**
 * 
 */
@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR" })
public class DevelopersPanel extends Panel {

  private static final long serialVersionUID = 8577685399815703632L;

  @SpringBean
  private SessionFactory factory;

  /**
   * 
   */
  public DevelopersPanel(String id) {
    super(id);

    AjaxLink devTab = new AjaxLink("dumpLink") {

      private static final long serialVersionUID = 109761762415267865L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        try {
          dump();
        } catch(IOException e) {
          throw new RuntimeException(e);
        }
        target.addComponent(DevelopersPanel.this.get("hibernateStats"));
      }

    };
    add(devTab);
    add(new HibernateStatisticsPanel("hibernateStats", new PropertyModel<Statistics>(factory, "statistics")).setOutputMarkupId(true));
  }

  @Override
  public boolean isVisible() {
    return ((OnyxApplication) OnyxApplication.get()).isDevelopmentMode();
  }

  private void dump() throws IOException {
    FsDatasource fs = new FsDatasource("target/magma-dump.zip");
    for(Datasource ds : MagmaEngine.get().getDatasources()) {
      for(ValueTable table : ds.getValueTables()) {
        ValueTableWriter writer = fs.createWriter(table.getName());
        VariableWriter vw = writer.writeVariables(table.getEntityType());
        for(Variable variable : table.getVariables()) {
          vw.writeVariable(variable);
        }
        vw.close();
        for(ValueSet valueSet : table.getValueSets()) {
          ValueSetWriter vsw = writer.writeValueSet(valueSet.getVariableEntity());
          for(Variable variable : table.getVariables()) {
            vsw.writeValue(variable, table.getValue(variable, valueSet));
          }
          vsw.close();
        }
        writer.close();
      }
    }
  }

}
