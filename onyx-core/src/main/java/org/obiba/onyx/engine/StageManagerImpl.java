/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.obiba.core.spring.xstream.InjectingReflectionProviderWrapper;
import org.obiba.onyx.util.FileUtil;
import org.obiba.onyx.util.data.Data;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.thoughtworks.xstream.XStream;

/**
 *
 */
public class StageManagerImpl implements ApplicationContextAware, InitializingBean, StageManager {

  // private final Logger logger = LoggerFactory.getLogger(getClass());

  private ApplicationContext applicationContext;

  private XStream xstream;

  private Resource stageDescriptor;

  private List<Stage> stages;

  @Override
  public void afterPropertiesSet() throws Exception {
    xstream = new XStream(new InjectingReflectionProviderWrapper(new XStream().getReflectionProvider(), applicationContext));
    xstream.alias("stages", LinkedList.class);
    xstream.alias("stage", Stage.class);
    xstream.alias("stageCondition", PreviousStageDependencyCondition.class);
    xstream.alias("variableCondition", VariableStageDependencyCondition.class);
    xstream.alias("multipleCondition", MultipleStageDependencyCondition.class);
    xstream.alias("inverseCondition", InverseStageDependencyCondition.class);
    xstream.alias("moduleCondition", ModuleDependencyCondition.class);
    xstream.omitField(ModuleDependencyCondition.class, "moduleRegistry");
    xstream.alias("finalCondition", FinalDependencyCondition.class);
    xstream.omitField(FinalDependencyCondition.class, "moduleRegistry");
    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Stage> getStages() {
    if(stages == null) {
      try {
        InputStream is = stageDescriptor.getInputStream();
        try {
          stages = (List<Stage>) xstream.fromXML(is);
        } finally {
          if(is != null) {
            try {
              is.close();
            } catch(Exception e) {
            }
          }
        }
      } catch(IOException e) {
        throw new RuntimeException("Cannot read stages", e);
      }
    }
    return stages;
  }

  @Override
  public Stage getStage(String name) {
    if(getStages() != null) {
      for(Stage stage : getStages()) {
        if(StringUtils.equals(stage.getName(), name)) {
          return stage;
        }
      }
    }
    return null;
  }

  @Override
  public void addStage(int index, Stage stage) {
    if(getStages() == null) stages = new LinkedList<Stage>();
    stages.add(index, stage);
    write();
  }

  @Override
  public void removeStage(Stage stage) {
    if(getStages() != null) {
      stages.remove(stage);
      write();
    }
  }

  private void write() {
    try {
      File tmp = File.createTempFile("stages", ".tmp");
      Writer writer = new FileWriter(tmp);
      try {
        xstream.toXML(stages, writer);
      } finally {
        try {
          writer.close();
        } catch(Exception e) {
        }
      }
      FileUtil.copyFile(tmp, stageDescriptor.getFile());
      if(tmp.delete()) {
        // ignore
      }
    } catch(IOException e) {
      throw new RuntimeException("Cannot write stages", e);
    }
  }

  @Override
  @Required
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Required
  public void setStageDescriptor(Resource stageDescriptor) {
    this.stageDescriptor = stageDescriptor;
  }

}
