/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import static org.easymock.EasyMock.createMock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.easymock.EasyMock;
import org.obiba.core.util.StreamUtil;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.impl.QuestionnaireBundleManagerImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.singledocument.SingleDocumentQuestionnairePage;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.wicket.test.MockSpringApplication;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Renders a questionnaire to a single HTML document.
 */
public class QuestionnaireRenderer {

  private File outputDir;

  private QuestionnaireBundleManager bundleManager;

  private ApplicationContextMock mockCtx = new ApplicationContextMock();

  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationServiceMock;

  /**
   * Constructs a renderer with the specified questionnaire bundle directory and output directory
   * @param bundles the directory where the questionnaire bundles are located
   * @param outputDir the directory where to write generated HTML files
   */
  public QuestionnaireRenderer(File bundles, File outputDir) {
    this.outputDir = outputDir;
    QuestionnaireBundleManagerImpl mgr = new QuestionnaireBundleManagerImpl(bundles);
    mgr.setPropertyKeyProvider(new DefaultPropertyKeyProviderImpl());
    mgr.setResourceLoader(new PathMatchingResourcePatternResolver());
    try {
      mgr.afterPropertiesSet();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
    bundleManager = mgr;

    activeQuestionnaireAdministrationServiceMock = createMock(ActiveQuestionnaireAdministrationService.class);
    mockCtx.putBean(activeQuestionnaireAdministrationServiceMock);
    mockCtx.putBean(mgr);
  }

  public void render(final String name, Locale... locales) {
    QuestionnaireBundle bundle = bundleManager.getBundle(name);
    if(bundle == null) {
      StringBuilder sb = new StringBuilder();
      for(QuestionnaireBundle b : bundleManager.bundles()) {
        if(sb.length() > 0) {
          sb.append(", ");
        }
        sb.append(b.getName());
      }
      throw new IllegalArgumentException("Bundle '" + name + "' does not exist. Available bundles are: " + sb.toString());
    }
    Questionnaire questionnaire = bundle.getQuestionnaire();

    QuestionnaireParticipant qp = new QuestionnaireParticipant();
    qp.setParticipant(new Participant());
    qp.setQuestionnaireName(name);

    if(locales == null || locales.length == 0) {
      locales = bundle.getAvailableLanguages().toArray(new Locale[] {});
    }

    for(Locale locale : locales) {
      EasyMock.expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaireParticipant()).andReturn(qp).anyTimes();
      EasyMock.expect(activeQuestionnaireAdministrationServiceMock.getQuestionnaire()).andReturn(questionnaire).anyTimes();
      EasyMock.expect(activeQuestionnaireAdministrationServiceMock.getLanguage()).andReturn(locale).anyTimes();
      EasyMock.expect(activeQuestionnaireAdministrationServiceMock.findAnswer((QuestionCategory) EasyMock.anyObject())).andReturn(null).anyTimes();
      EasyMock.expect(activeQuestionnaireAdministrationServiceMock.findAnswer((Question) EasyMock.anyObject(), (Category) EasyMock.anyObject())).andReturn(null).anyTimes();
      EasyMock.expect(activeQuestionnaireAdministrationServiceMock.findAnswer((Question) EasyMock.anyObject(), (QuestionCategory) EasyMock.anyObject())).andReturn(null).anyTimes();
      EasyMock.expect(activeQuestionnaireAdministrationServiceMock.findAnswer((String) EasyMock.anyObject(), (String) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(null).anyTimes();
      EasyMock.expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer((Question) EasyMock.anyObject(), (Category) EasyMock.anyObject(), (OpenAnswerDefinition) EasyMock.anyObject())).andReturn(null).anyTimes();
      EasyMock.expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer((QuestionCategory) EasyMock.anyObject(), (OpenAnswerDefinition) EasyMock.anyObject())).andReturn(null).anyTimes();
      EasyMock.expect(activeQuestionnaireAdministrationServiceMock.findOpenAnswer((String) EasyMock.anyObject(), (String) EasyMock.anyObject(), (String) EasyMock.anyObject(), (String) EasyMock.anyObject())).andReturn(null).anyTimes();
      EasyMock.expect(activeQuestionnaireAdministrationServiceMock.getComment((Question) EasyMock.anyObject())).andReturn(null).anyTimes();
      EasyMock.replay(activeQuestionnaireAdministrationServiceMock);

      MockSpringApplication mockApp = new MockSpringApplication() {
        @Override
        protected void init() {
          super.init();
          getMarkupSettings().setStripWicketTags(true);
        }
      };
      mockApp.setApplicationContext(mockCtx);

      WicketTester tester = new WicketTester(mockApp);
      tester.startPage(new SingleDocumentQuestionnairePage(new QuestionnaireModel(questionnaire)));

      FileOutputStream fos = null;
      try {
        InputStream is = new ByteArrayInputStream(tester.getServletResponse().getDocument().getBytes());
        File output = new File(outputDir, name + '_' + locale.toString() + ".html");
        fos = new FileOutputStream(output);
        StreamUtil.copy(is, fos);
      } catch(FileNotFoundException e) {
      } catch(IOException e) {
      }
      StreamUtil.silentSafeClose(fos);

      EasyMock.reset(activeQuestionnaireAdministrationServiceMock);
    }
  }

  public static void main(String[] args) {
    QuestionnaireRenderer renderer = new QuestionnaireRenderer(new File(args[0]), new File(args[1]));
    renderer.render(args[2]);
  }
}
