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
import java.util.Arrays;

import org.apache.wicket.IClusterable;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
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

    final DumpInput dumpInput = new DumpInput();
    setDefaultModel(new CompoundPropertyModel<DumpInput>(dumpInput));

    Form<?> dumpForm = new Form("dumpForm");
    add(dumpForm);

    dumpForm.add(new DropDownChoice<String>("format", Arrays.asList(new String[] { "XML", "Excel" })));
    dumpForm.add(new CheckBox("values"));
    dumpForm.add(new FeedbackPanel("feedback"));

    SubmitLink dumpLink = new SubmitLink("dumpLink", dumpForm) {

      private static final long serialVersionUID = 109761762415267865L;

      @Override
      public void onSubmit() {
        try {
          File dumpFile = dump(dumpInput);
          getRequestCycle().setRequestTarget(new ResourceStreamRequestTarget(new TmpFileResourceStream(dumpFile) {
            public String getContentType() {
              if(dumpInput.format.equals("XML")) {
                return "application/zip";
              }
              return "application/vnd.ms-excel";
            }
          }, dumpFile.getName()));
        } catch(Exception e) {
          log.error("Magma dump failed.", e);
          error(e.getMessage());
        }
      }

    };
    dumpForm.add(dumpLink);

    AjaxLink refreshStatsLink = new AjaxLink("refreshStatsLink") {

      private static final long serialVersionUID = 109761762415267865L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        target.addComponent(DevelopersPanel.this.get("hibernateStats"));
      }

    };
    add(refreshStatsLink);

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

  private File dump(DumpInput dumpInput) throws IOException {

    File dumpFile;
    Datasource target;

    if(dumpInput.format.equals("XML")) {
      dumpFile = getDumpXMLFile();
      target = new FsDatasource("magma-dump", dumpFile);
    } else {
      dumpFile = getDumpExcelFile();
      target = new ExcelDatasource("magma-dump", dumpFile);
    }
    if(dumpFile.exists()) {
      dumpFile.delete();
    }

    MagmaEngine.get().addDatasource(target);
    try {
      DatasourceCopier.Builder builder = DatasourceCopier.Builder.newCopier().dontCopyNullValues().withLoggingListener().withThroughtputListener().withListener(new SessionClearingListener());
      if(!dumpInput.values) {
        builder.dontCopyValues();
      }
      DatasourceCopier copier = builder.build();
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

    return dumpFile;
  }

  public File getDumpXMLFile() {
    return new File(System.getProperty("java.io.tmpdir"), "onyx.zip");
  }

  public File getDumpExcelFile() {
    return new File(System.getProperty("java.io.tmpdir"), "onyx.xlsx");
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

  private static class DumpInput implements IClusterable {
    public Boolean values = Boolean.FALSE;

    public String format = "Excel";
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
