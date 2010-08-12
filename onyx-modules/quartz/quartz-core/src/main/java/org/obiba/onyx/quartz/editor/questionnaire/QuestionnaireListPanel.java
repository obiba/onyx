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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.IColumnProvider;

/**
 * @author cedric.thiebault
 */
public class QuestionnaireListPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  public QuestionnaireListPanel(String id) {
    super(id);

    add(new OnyxEntityList<User>("questionnaire-list", new QuestionnaireProvider(), new QuestionnaireListColumnProvider(), new StringResourceModel("QuestionnaireList", QuestionnaireListPanel.this, null)));
  }

  @SuppressWarnings("serial")
  private class QuestionnaireProvider extends SortableDataProvider<Questionnaire> {

    @Override
    public Iterator<Questionnaire> iterator(int first, int count) {
      Set<QuestionnaireBundle> bundles = questionnaireBundleManager.bundles();
      List<Questionnaire> questionnaires = new ArrayList<Questionnaire>(bundles.size());
      for(QuestionnaireBundle bundle : bundles) {
        questionnaires.add(bundle.getQuestionnaire());
      }
      return questionnaires.iterator();
    }

    @Override
    public int size() {
      return questionnaireBundleManager.bundles().size();
    }

    @Override
    public IModel<Questionnaire> model(Questionnaire questionnaire) {
      return new Model<Questionnaire>(questionnaire);
    }

    @Override
    public void detach() {
    }

  }

  private class QuestionnaireListColumnProvider implements IColumnProvider<Questionnaire>, Serializable {

    private static final long serialVersionUID = 1141339694945247910L;

    private final List<IColumn<Questionnaire>> columns = new ArrayList<IColumn<Questionnaire>>();

    private final List<IColumn<Questionnaire>> additional = new ArrayList<IColumn<Questionnaire>>();

    @SuppressWarnings("serial")
    public QuestionnaireListColumnProvider() {
      columns.add(new PropertyColumn<Questionnaire>(new StringResourceModel("Name", QuestionnaireListPanel.this, null), "name", "name"));
      columns.add(new PropertyColumn<Questionnaire>(new StringResourceModel("Version", QuestionnaireListPanel.this, null), "version", "version"));
      columns.add(new AbstractColumn<Questionnaire>(new StringResourceModel("Language(s)", QuestionnaireListPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<Questionnaire>> cellItem, String componentId, IModel<Questionnaire> rowModel) {
          StringBuilder localeList = new StringBuilder();
          Locale sessionLocale = Session.get().getLocale();
          for(Locale locale : rowModel.getObject().getLocales()) {
            if(localeList.length() != 0) localeList.append(", ");
            localeList.append(locale.getDisplayLanguage(sessionLocale));
          }
          cellItem.add(new Label(componentId, localeList.toString()));
        }
      });

      columns.add(new HeaderlessColumn<Questionnaire>() {
        @Override
        public void populateItem(Item<ICellPopulator<Questionnaire>> cellItem, String componentId, IModel<Questionnaire> rowModel) {
          cellItem.add(new LinkFragment(componentId, rowModel));
        }
      });

    }

    @Override
    public List<IColumn<Questionnaire>> getAdditionalColumns() {
      return additional;
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<Questionnaire>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<Questionnaire>> getRequiredColumns() {
      return columns;
    }

  }

  public class LinkFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("serial")
    public LinkFragment(String id, final IModel<Questionnaire> rowModel) {
      super(id, "linkFragment", QuestionnaireListPanel.this, rowModel);

      add(new AjaxLink<Questionnaire>("editLink", rowModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          // userDetailsModalWindow.setContent(new UserPanel("content", rowModel, userDetailsModalWindow));
          // userDetailsModalWindow.show(target);
        }
      });
    }
  }
}
