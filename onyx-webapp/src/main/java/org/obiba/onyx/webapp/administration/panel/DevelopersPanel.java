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
import java.io.IOException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.FileResourceStream;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.obiba.magma.Datasource;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.Variable;
import org.obiba.magma.datasource.excel.ExcelDatasource;
import org.obiba.magma.datasource.fs.FsDatasource;
import org.obiba.magma.support.DatasourceCopier;
import org.obiba.magma.support.DatasourceCopier.DatasourceCopyEventListener;
import org.obiba.onyx.webapp.OnyxApplication;
import org.obiba.wicket.hibernate.HibernateStatisticsPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR" })
public class DevelopersPanel extends Panel {

  private static final long serialVersionUID = 8577685399815703632L;

  private static final Logger log = LoggerFactory.getLogger(DevelopersPanel.class);

  @SpringBean
  private SessionFactory factory;

  /**
   * 
   */
  public DevelopersPanel(String id) {
    super(id);

    AjaxLink dumpLink = new AjaxLink("dumpLink") {

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
    add(dumpLink);
    add(new Label("dumpFilename", new PropertyModel<String>(this, "dumpFile.absolutePath")));

    Link catalogueLink = new Link("catalogueLink") {
      @Override
      public void onClick() {

        try {
          File catalogueFile = getCatalogueFile();
          if(catalogueFile.exists()) {
            catalogueFile.delete();
          }

          ExcelDatasource target;
          MagmaEngine.get().addDatasource(target = new ExcelDatasource("onyx-catalogue", catalogueFile));
          try {
            DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyValues().build();
            for(Datasource ds : MagmaEngine.get().getDatasources()) {
              if(ds != target) { // Don't copy target datasource on target datasource
                copier.copy(ds, target);
              }
            }
          } finally {
            try {
              MagmaEngine.get().removeDatasource(target);
            } catch(RuntimeException e) {
              log.warn("Exception thrown while removing datasource. May be caused by ", e);
            }
          }

          getRequestCycle().setRequestTarget(new ResourceStreamRequestTarget(new TmpFileResourceStream(catalogueFile) {
            public String getContentType() {
              return "application/vnd.ms-excel";
            }
          }, catalogueFile.getName()));
        } catch(Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    add(catalogueLink);

    AjaxLink clearCacheLink = new AjaxLink("clearCacheLink") {

      private static final long serialVersionUID = 109761762415267865L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        factory.evictQueries();
        target.addComponent(DevelopersPanel.this.get("hibernateStats"));
      }

    };
    add(clearCacheLink);
    add(new HibernateStatisticsPanel("hibernateStats", new PropertyModel<Statistics>(factory, "statistics")).setOutputMarkupId(true));
  }

  @Override
  public boolean isVisible() {
    return ((OnyxApplication) OnyxApplication.get()).isDevelopmentMode();
  }

  private void dump() throws IOException {

    File dumpFile = getDumpFile();
    if(dumpFile.exists()) {
      dumpFile.delete();
    }

    FsDatasource target;
    MagmaEngine.get().addDatasource(target = new FsDatasource("magma-dump", dumpFile));
    try {
      DatasourceCopier copier = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withThroughtputListener().withListener(new SessionClearingListener()).build();
      for(Datasource ds : MagmaEngine.get().getDatasources()) {
        if(ds != target) { // Don't copy target datasource on target datasource
          copier.copy(ds, target);
        }
      }
    } finally {
      try {
        MagmaEngine.get().removeDatasource(target);
      } catch(RuntimeException e) {
        log.warn("Exception thrown while removing datasource. May be caused by ", e);
      }
    }
  }

  public File getDumpFile() {
    return new File(System.getProperty("java.io.tmpdir"), "magma-dump.zip");
  }

  public File getCatalogueFile() {
    return new File(System.getProperty("java.io.tmpdir"), "onyx-catalogue.xlsx");
  }

  private class SessionClearingListener implements DatasourceCopyEventListener {

    public void onValueSetCopied(ValueSet valueSet) {
      factory.getCurrentSession().clear();
    }

    public void onValueSetCopy(ValueSet valueSet) {
    }

    public void onVariableCopied(Variable variable) {
    }

    public void onVariableCopy(Variable variable) {
    }

  }

  private class TmpFileResourceStream extends FileResourceStream {

    private File tmpFile;

    public TmpFileResourceStream(File tmpFile) {
      super(tmpFile);
      this.tmpFile = tmpFile;
    }

    @Override
    public void close() throws IOException {
      super.close();
      if(tmpFile != null) {
        tmpFile.delete();
      }
    }
  }

}
