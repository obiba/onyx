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

import java.io.File;
import java.io.FileOutputStream;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.obiba.core.util.StreamUtil;
import org.obiba.magma.Collection;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.xstream.Io;
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
        dumpVariables();
        dumpValueSets();
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

  private void dumpVariables() {

    MagmaEngine engine = MagmaEngine.get();
    FileOutputStream os = null;
    Io io = new Io();
    try {
      Collection collection = engine.lookupCollection("onyx-baseline");
      File temp = new File(System.getProperty("java.io.tmpdir"), "magma-variables.xml");
      os = new FileOutputStream(temp);
      io.writeVariables(collection, os);
      os.close();
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      StreamUtil.silentSafeClose(os);
    }

  }

  private void dumpValueSets() {
    MagmaEngine engine = MagmaEngine.get();
    FileOutputStream os = null;
    Io io = new Io();
    try {
      Collection collection = engine.lookupCollection("onyx-baseline");
      File temp = new File(System.getProperty("java.io.tmpdir"), "magma-valuesets.xml");
      os = new FileOutputStream(temp);
      io.writeEntities(collection, os);

    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      StreamUtil.silentSafeClose(os);
    }
  }
}
