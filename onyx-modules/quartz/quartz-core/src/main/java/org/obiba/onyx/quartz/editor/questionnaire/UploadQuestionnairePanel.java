/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.behavior.tooltip.HelpTooltipPanel;
import org.obiba.onyx.quartz.editor.utils.SaveCancelPanel;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class UploadQuestionnairePanel extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  private static final int BUFFER_SIZE = 1024;

  private static final byte[] BUFFER = new byte[BUFFER_SIZE];

  @SpringBean
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "Needs to be be re-initialized upon deserialization")
  private QuestionnaireBundleManager questionnaireBundleManager;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private FileUploadField zipFile;

  public UploadQuestionnairePanel(String id, final ModalWindow uploadWindow) {
    this(id, uploadWindow, null);
  }

  public UploadQuestionnairePanel(String id, final ModalWindow uploadWindow, IModel<Questionnaire> questionnaireModel) {
    super(id);

    final Questionnaire updatedQuestionnaire = questionnaireModel == null ? null : questionnaireModel.getObject();

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    zipFile = new FileUploadField("zip");
    zipFile.setLabel(new ResourceModel("File.ZIP"));
    zipFile.setRequired(true);

    Form<?> form = new Form<Void>("form") {
      @Override
      protected void onSubmit() {
        FileUpload zip = zipFile.getFileUpload();
        if(zip == null) {
          error(getLocalizer().getString("Error.NoFileUploaded", UploadQuestionnairePanel.this));
          return;
        }
        String clientFileName = zip.getClientFileName();
        if(!clientFileName.endsWith(".zip")) {
          error(getLocalizer().getString("Error.NotAZipFile", UploadQuestionnairePanel.this));
          return;
        }
        try {

          ZipInputStream zis = new ZipInputStream(zip.getInputStream());
          ZipEntry entry = null;
          Questionnaire questionnaire = null;
          List<File> localeProperties = new ArrayList<File>();
          while((entry = zis.getNextEntry()) != null) {
            if(!entry.isDirectory()) {
              String name = entry.getName();
              int n;
              if("questionnaire.xml".equals(name)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                while((n = zis.read(BUFFER, 0, BUFFER_SIZE)) > -1) {
                  out.write(BUFFER, 0, n);
                }
                questionnaire = questionnaireBundleManager.load(new ByteArrayInputStream(out.toByteArray()));
                try {
                  out.close();
                } catch(Exception e) {
                }

                if(updatedQuestionnaire == null) {
                  for(QuestionnaireBundle bundle : questionnaireBundleManager.bundles()) {
                    if(questionnaire.getName().equals(bundle.getName())) {
                      error(new StringResourceModel("Error.QuestionnaireAlreadyExists", UploadQuestionnairePanel.this, null, new Object[] { questionnaire.getName() }).getString());
                      return;
                    }
                  }
                } else {
                  if(!questionnaire.getName().equals(updatedQuestionnaire.getName())) {
                    error(new StringResourceModel("Error.NameDifferentFromQuestionnaireToUpdate", UploadQuestionnairePanel.this, null, new Object[] { questionnaire.getName(), updatedQuestionnaire.getName() }).getString());
                    return;
                  }
                }

              } else if(name.endsWith(".properties")) {
                File tmp = new File(name);
                tmp.deleteOnExit();
                FileOutputStream out = new FileOutputStream(tmp);
                while((n = zis.read(BUFFER, 0, BUFFER_SIZE)) > -1) {
                  out.write(BUFFER, 0, n);
                }
                localeProperties.add(tmp);
                try {
                  out.close();
                } catch(Exception e) {
                }
              }
              zis.closeEntry();
            }
          }

          if(questionnaire == null) {
            error(new StringResourceModel("Error.MissingFile", UploadQuestionnairePanel.this, null, new Object[] { "questionnaire.xml" }).getString());
          } else {
            for(Locale locale : questionnaire.getLocales()) {
              final String fileName = "language_" + locale.getLanguage() + ".properties";
              File localeFile = Iterables.find(localeProperties, new Predicate<File>() {
                public boolean apply(File file) {
                  return file.getName().equals(fileName);
                };
              });
              if(localeFile == null) {
                error(new StringResourceModel("Error.MissingFile", UploadQuestionnairePanel.this, null, new Object[] { fileName }).getString());
              }
              if(hasError()) return;
            }
            questionnaireBundleManager.createBundle(questionnaire, localeProperties.toArray(new File[localeProperties.size()]));
          }

        } catch(IOException e) {
          log.error("IOException", e);
          error(getLocalizer().getString("Error.CannotReadFile", UploadQuestionnairePanel.this) + ": " + e.getMessage());
        }
      }
    };
    form.setMultiPart(true);
    add(form);
    form.add(zipFile).add(new SimpleFormComponentLabel("zipLabel", zipFile)).add(new HelpTooltipPanel("zipHelp", new ResourceModel("File.ZIP.Tooltip")));

    form.add(new SaveCancelPanel("saveCancel", form) {

      @Override
      protected void onSave(AjaxRequestTarget target, Form<?> form1) {
        UploadQuestionnairePanel.this.onSave(target);
        uploadWindow.close(target);
      }

      @Override
      protected void onCancel(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        uploadWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, @SuppressWarnings("hiding") Form<?> form) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

  }

  protected abstract void onSave(AjaxRequestTarget target);

}
