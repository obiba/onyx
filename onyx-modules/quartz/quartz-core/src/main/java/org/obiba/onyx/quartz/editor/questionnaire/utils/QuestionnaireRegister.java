/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire.utils;

import org.obiba.onyx.engine.ModuleRegistry;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.engine.StageManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.engine.QuartzModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

//class is becoming more than only register... probably rename soon
public class QuestionnaireRegister {

  private static final Logger log = LoggerFactory.getLogger(QuestionnaireRegister.class);

  private ModuleRegistry moduleRegistry;

  public Stage register(Questionnaire questionnaire) {
    QuartzModule quartzModule = (QuartzModule) moduleRegistry.getModule(QuartzModule.MODULE_NAME);
    StageManager stageManager = quartzModule.getStageManager();
    Stage stage = stageManager.getStage(questionnaire.getName());
    if(stage == null) {
      // create Stage if needed
      stage = new Stage(quartzModule, questionnaire.getName());
      quartzModule.addStage(stageManager.getStages().size(), stage);
      moduleRegistry.unregisterModule(QuartzModule.MODULE_NAME);
      moduleRegistry.registerModule(quartzModule);
    } else {
      quartzModule.stageChanged(stage);
    }
    return stage;
  }

  public void unregister(Questionnaire questionnaire) {
    QuartzModule quartzModule = (QuartzModule) moduleRegistry.getModule(QuartzModule.MODULE_NAME);
    StageManager stageManager = quartzModule.getStageManager();
    Stage stage = stageManager.getStage(questionnaire.getName());
    if(stage != null) {
      moduleRegistry.unregisterModule(QuartzModule.MODULE_NAME);
      quartzModule.removeStage(stage);
      moduleRegistry.registerModule(quartzModule);
    }
  }

  public boolean isConclusionQuestionnaire(Questionnaire questionnaire) {
    QuartzModule quartzModule = (QuartzModule) moduleRegistry.getModule(QuartzModule.MODULE_NAME);
    StageManager stageManager = quartzModule.getStageManager();
    Stage stage = stageManager.getStage(questionnaire.getName());
    if(stage == null) {
      log.warn("No stage defined for questionnare {}. Registering one now...", questionnaire.getName());
      stage = register(questionnaire);
    }
    return stage.isInterviewConclusion();
  }

  @Required
  public void setModuleRegistry(ModuleRegistry moduleRegistry) {
    this.moduleRegistry = moduleRegistry;
  }
}
