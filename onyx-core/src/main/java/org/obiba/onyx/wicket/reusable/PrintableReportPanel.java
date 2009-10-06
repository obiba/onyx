/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.reusable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.form.validation.IFormValidator;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.print.IPrintableReport;
import org.obiba.onyx.print.PrintableReportsRegistry;
import org.obiba.wicket.model.MessageSourceResolvableStringModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintableReportPanel extends Panel {

  private static final Logger log = LoggerFactory.getLogger(PrintableReportPanel.class);

  private static final long serialVersionUID = 1L;

  @SpringBean(name = "printableReportsRegistry")
  private PrintableReportsRegistry printableReportsRegistry;

  @SuppressWarnings("unchecked")
  private final CheckGroup group = new CheckGroup("group", new ArrayList());

  private FeedbackWindow feedbackWindow;

  public PrintableReportPanel(String id) {
    super(id);
    add(new AttributeModifier("class", true, new Model("printable-report-panel")));
    add(group);
    addPrintableReportList();

    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);
  }

  public void addPrintableReportList() {

    final List<IPrintableReport> interviewLogList = new ArrayList<IPrintableReport>(printableReportsRegistry.availableReports());
    final List<PrintableReportModel> printReportModels = new ArrayList<PrintableReportModel>(interviewLogList.size());
    for(IPrintableReport report : interviewLogList) {
      printReportModels.add(new PrintableReportModel(report));
    }

    Collections.sort(printReportModels, new Comparator<PrintableReportModel>() {

      public int compare(PrintableReportModel arg0, PrintableReportModel arg1) {
        return arg0.name.compareTo(arg1.name);
      }

    });

    Loop logItemLoop;
    logItemLoop = new Loop("table", printReportModels.size()) {

      private static final long serialVersionUID = 5173436167390888581L;

      @Override
      protected void populateItem(LoopItem item) {
        item.setRenderBodyOnly(true);
        item.add(new PrintableReportFragment("rows", "printableReportRow", PrintableReportPanel.this, printReportModels.get(item.getIteration()), item.getIteration()));
      }
    };
    group.addOrReplace(logItemLoop);
  }

  public class PrintableReportFragment extends Fragment {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("serial")
    public PrintableReportFragment(String id, String markupId, MarkupContainer markupContainer, IModel model, int iteration) {
      super(id, markupId, markupContainer, model);
      setRenderBodyOnly(true);
      IPrintableReport printableReport = (IPrintableReport) getDefaultModelObject();
      Store store = new Store();
      store.setReportName(printableReport.getName());
      StoreModel storeModel = new StoreModel();
      storeModel.setObject(store);

      WebMarkupContainer webMarkupContainer = new WebMarkupContainer("reportRow");
      add(webMarkupContainer);
      webMarkupContainer.add(new AttributeAppender("class", true, new Model(getOddEvenCssClass(iteration)), " "));

      Check box = new Check("checkbox", storeModel);
      if(!isPrintable(printableReport)) box.setEnabled(false);
      webMarkupContainer.add(box);

      MessageSourceResolvableStringModel nameStringModel = new MessageSourceResolvableStringModel(printableReport.getLabel());
      Label nameLabel = new Label("name", nameStringModel);
      nameLabel.setRenderBodyOnly(true);
      if(!isPrintable(printableReport)) nameLabel.add(new AttributeAppender("style", true, new Model("color : #999999"), " "));

      FormComponentLabel checkboxLabel = new FormComponentLabel("checkboxLabel", box);
      checkboxLabel.add(nameLabel);
      webMarkupContainer.add(checkboxLabel);

      Label statusLabel = new Label("status", getStatusResourceModel(printableReport));
      if(!isPrintable(printableReport)) statusLabel.add(new AttributeAppender("style", true, new Model("color : #A9A9A9"), " "));
      webMarkupContainer.add(statusLabel);

      List<Locale> locales = new ArrayList<Locale>(printableReport.availableLocales());
      final DropDownChoice choice = new DropDownChoice("language", new PropertyModel(store, "locale"), locales, new ChoiceRenderer() {
        public Object getDisplayValue(Object object) {
          return ((Locale) object).getDisplayName(getLocale());
        }
      });

      choice.add(new OnChangeAjaxBehavior() {

        private static final long serialVersionUID = 1L;

        @Override
        protected void onUpdate(AjaxRequestTarget target) {
          choice.updateModel();
        }

      });
      if(!isPrintable(printableReport)) choice.setEnabled(false);
      webMarkupContainer.add(choice);
      if(!printableReport.isLocalisable()) {
        choice.setVisible(false);
      }
    }
  }

  private ResourceModel getStatusResourceModel(IPrintableReport printableReport) {
    if(printableReport.isReady()) {
      if(printableReport.isElectronic()) {
        return new ResourceModel("ready");
      } else {
        return new ResourceModel("manual");
      }
    } else {
      return new ResourceModel("notReady");
    }
  }

  private boolean isPrintable(IPrintableReport printableReport) {
    if(printableReport.isReady()) {
      if(printableReport.isElectronic()) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  private String getOddEvenCssClass(int row) {
    return row % 2 == 1 ? "odd" : "even";
  }

  private class PrintableReportModel extends LoadableDetachableModel {

    private static final long serialVersionUID = 1L;

    String name;

    public PrintableReportModel(IPrintableReport report) {
      this.name = report.getName();
    }

    @Override
    protected Object load() {
      return printableReportsRegistry.getReportByName(name);
    }
  }

  private class StoreModel implements IModel {

    private static final long serialVersionUID = 1L;

    private Store store;

    public Object getObject() {
      return store;
    }

    public void setObject(Object object) {
      this.store = (Store) object;
    }

    public void detach() {
    }

  }

  private class Store implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reportName;

    private Locale locale;

    public Locale getLocale() {
      return locale;
    }

    public void setLocale(Locale locale) {
      this.locale = locale;
    }

    public String getReportName() {
      return reportName;
    }

    public void setReportName(String reportName) {
      this.reportName = reportName;
    }

    @Override
    public String toString() {
      return "[" + reportName + " : " + (locale != null ? locale.getDisplayName() : null) + "]";
    }
  }

  @SuppressWarnings("unchecked")
  public void printReports() {
    List<Store> checkedReports = (List<Store>) group.getModelObject();
    IPrintableReport report = null;
    Store store = null;
    try {
      for(int i = 0; i < checkedReports.size(); i++) {
        store = checkedReports.get(i);
        report = printableReportsRegistry.getReportByName(store.reportName);
        report.print(store.getLocale());
      }
    } catch(RuntimeException e) {
      String reportName = "";
      if(report != null) {
        MessageSourceResolvableStringModel nameStringModel = new MessageSourceResolvableStringModel(report.getLabel());
        reportName = (String) nameStringModel.getObject();
      } else {
        reportName = store.getReportName();
      }
      StringResourceModel errorMessageModel = new StringResourceModel("printErrorMessage", this, new Model(new ValueMap("reportName=" + reportName)));
      error(errorMessageModel.getString());
      log.error("Unable to print the report [" + store.reportName + "]. ", e);
      return;
    }
    StringResourceModel successMessageModel = new StringResourceModel("printSuccessMessage", this, new Model(""));
    info(successMessageModel.getString());
  }

  public IFormValidator getFormValidator() {
    return new PrintableReportValidator();
  }

  public FeedbackWindow getFeedbackWindow() {
    return feedbackWindow;
  }

  private class PrintableReportValidator implements IFormValidator {

    private static final long serialVersionUID = 1L;

    public FormComponent[] getDependentFormComponents() {
      return null;
    }

    @SuppressWarnings("unchecked")
    public void validate(Form form) {
      group.updateModel();

      List<Store> checkedReports = (List<Store>) group.getModelObject();
      if(checkedReports.size() == 0) {
        StringResourceModel rm = new StringResourceModel("selectReportsToPrint", PrintableReportPanel.this, null);
        error(rm.getString());
      } else {
        for(int i = 0; i < checkedReports.size(); i++) {
          Store store = checkedReports.get(i);
          IPrintableReport report = printableReportsRegistry.getReportByName(store.reportName);
          if(report.isLocalisable() && store.locale == null) {
            StringResourceModel rm = new StringResourceModel("selectPrintLanguage", PrintableReportPanel.this, null);
            error(rm.getString());
            break;
          }

        }
      }

    }
  }

}
