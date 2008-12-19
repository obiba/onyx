/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

import com.thoughtworks.xstream.XStream;

/**
 * 
 */
public class VariableTest {

  private XStream xstream;

  @Test
  public void testXStream() {
    initializeXStream();

    Entity root = new Entity("CARTAGENE");
    Entity parent;
    Variable variable;
    Variable subvariable;
    // participants

    parent = root.addEntity("ADMIN/PARTICIPANT");

    variable = new Variable("ID").setType(DataType.TEXT);
    parent.addEntity(variable);

    VariableData[] participants = new VariableData[3];
    for(int i = 1; i <= participants.length; i++) {
      participants[i - 1] = variable.addVariableData(new VariableData(DataBuilder.buildText(Integer.toString(i))));
    }

    subvariable = new Variable("NAME").setType(DataType.TEXT);
    parent.addEntity(subvariable);
    for(int i = 0; i < participants.length; i++) {
      subvariable.addVariableData(new ReferingData(DataBuilder.buildText("Name " + (i + 1))).setReferingData(participants[i]));
    }

    // questionnaire

    parent = root.addEntity("HealthQuestionnaire/1.0/AGE");

    variable = new Variable("AGE_CHOICE");
    parent.addEntity(variable);

    subvariable = new Variable("OPEN_AGE").setType(DataType.INTEGER).setUnit("year");
    variable.addEntity(subvariable);

    for(int i = 0; i < participants.length; i++) {
      subvariable.addVariableData(new ReferingData(DataBuilder.buildInteger(45 + i)).setReferingData(participants[i]));
    }

    parent = root.addEntity("HealthQuestionnaire/1.0/DATE_BIRTH/DOB_YEAR");

    variable = new Variable("DOB_YEAR_CHOICE");
    parent.addEntity(variable);

    subvariable = new Variable("OPEN_YEAR").setType(DataType.INTEGER).setUnit("year");
    variable.addEntity(subvariable);

    System.out.println("\n**** Variables directory ****\n");
    System.out.println(xstream.toXML(root));

    System.out.println("\n**** Variables paths ****\n");
    writeVariables(root);

    System.out.println("\n**** Variables data paths ****\n");
    writeVariablesData(root);

  }

  private void writeVariables(Entity parent) {
    for(Entity child : parent.getEntities()) {
      if(child instanceof Variable) {
        System.out.println(Entity.SCHEME + "://" + child.getPath());
      }
      writeVariables(child);
    }
  }

  private void writeVariablesData(Entity parent) {
    for(Entity child : parent.getEntities()) {
      if(child instanceof Variable) {
        for(VariableData data : ((Variable) child).getVariableDatas()) {
          System.out.println(Entity.SCHEME + "://" + data.getPath());
        }
      }
      writeVariablesData(child);
    }
  }

  private void initializeXStream() {
    xstream = new XStream();
    xstream.setMode(XStream.ID_REFERENCES);

    xstream.alias("entity", Entity.class);
    xstream.alias("variable", Variable.class);
    xstream.useAttributeFor(Variable.class, "type");
    xstream.useAttributeFor(Variable.class, "unit");
    xstream.alias("variableData", VariableData.class);
    xstream.alias("referingData", ReferingData.class);
    xstream.alias("participant", Participant.class);
    xstream.alias("data", Data.class);
    xstream.useAttributeFor(Data.class, "type");
  }

}
