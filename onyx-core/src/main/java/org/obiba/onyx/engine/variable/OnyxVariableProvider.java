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

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class OnyxVariableProvider implements IVariableProvider {

  public VariableData getVariableData(Participant participant, Variable variable, IVariablePathNamingStrategy variablePathNamingStrategy) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<Variable> getVariables() {
    List<Variable> variables = new ArrayList<Variable>();

    Variable admin = new Variable("Admin");
    variables.add(admin);

    Variable entity = new Variable("Participant");
    admin.addVariable(entity);
    entity.addVariable(new Variable("barcode").setDataType(DataType.TEXT));
    entity.addVariable(new Variable("gender").setDataType(DataType.TEXT));
    entity.addVariable(new Variable("firstName").setDataType(DataType.TEXT));
    entity.addVariable(new Variable("lastName").setDataType(DataType.TEXT));
    entity.addVariable(new Variable("birthDate").setDataType(DataType.DATE));
    entity.addVariable(new Variable("siteNo").setDataType(DataType.TEXT));
    entity.addVariable(new Variable("recruitmentType").setDataType(DataType.TEXT));

    entity = new Variable("User");
    admin.addVariable(entity);
    entity.addVariable(new Variable("login").setDataType(DataType.TEXT));
    entity.addVariable(new Variable("firstName").setDataType(DataType.TEXT));
    entity.addVariable(new Variable("lastName").setDataType(DataType.TEXT));

    return variables;
  }

}
