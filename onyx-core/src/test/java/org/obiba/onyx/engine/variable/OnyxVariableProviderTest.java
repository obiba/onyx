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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.user.Role;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.variable.impl.DefaultVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.util.VariableStreamer;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class OnyxVariableProviderTest {

  private static final Logger log = LoggerFactory.getLogger(OnyxVariableProviderTest.class);

  private static final String COUNTRY = "country";

  private IVariableProvider variableProvider;

  private ParticipantMetadata participantMetadataMock;

  private ParticipantService participantServiceMock;

  private EntityQueryService queryServiceMock;

  private IVariablePathNamingStrategy variablePathNamingStrategy;

  @Before
  public void setUp() {
    ApplicationContextMock mockCtx = new ApplicationContextMock();
    participantServiceMock = createMock(ParticipantService.class);
    queryServiceMock = createMock(EntityQueryService.class);
    mockCtx.putBean("participantService", participantServiceMock);
    mockCtx.putBean("persistenceManager", queryServiceMock);

    participantMetadataMock = new ParticipantMetadata();
    ParticipantAttribute attr = new ParticipantAttribute();
    attr.setName(COUNTRY);
    attr.setType(DataType.TEXT);
    participantMetadataMock.setConfiguredAttributes(Arrays.asList(new ParticipantAttribute[] { attr }));

    variableProvider = new OnyxVariableProvider();
    ((OnyxVariableProvider) variableProvider).setParticipantMetadata(participantMetadataMock);
    ((OnyxVariableProvider) variableProvider).setParticipantService(participantServiceMock);
    ((OnyxVariableProvider) variableProvider).setQueryService(queryServiceMock);

    variablePathNamingStrategy = new DefaultVariablePathNamingStrategy();
  }

  @Test
  public void testVariable() {
    Variable root = createVariable();
    log.info(VariableStreamer.toXML(root));

    Assert.assertEquals(1, root.getVariables().size());
    Variable variable = root.getVariable(OnyxVariableProvider.ADMIN);
    Assert.assertNotNull(variable);
    Assert.assertEquals(4, variable.getVariables().size());

    Variable subVar = variable.getVariable(OnyxVariableProvider.PARTICIPANT);
    Assert.assertNotNull(subVar);
    Assert.assertNotNull(subVar.getVariable(COUNTRY));
    Assert.assertEquals(DataType.TEXT, subVar.getVariable(COUNTRY).getDataType());

    subVar = variable.getVariable(OnyxVariableProvider.INTERVIEW);
    Assert.assertNotNull(subVar);

    subVar = variable.getVariable(OnyxVariableProvider.ACTION);
    Assert.assertNotNull(subVar);

    subVar = variable.getVariable(OnyxVariableProvider.USER);
    Assert.assertNotNull(subVar);
  }

  @Test
  public void testParticipantAttribute() {
    Variable root = createVariable();

    Variable variable = root.getVariable(OnyxVariableProvider.ADMIN).getVariable(OnyxVariableProvider.PARTICIPANT).getVariable(OnyxVariableProvider.BARCODE);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    participant.setBarcode("123");

    VariableData varData = variableProvider.getVariableData(participant, variable, variablePathNamingStrategy);
    Assert.assertNotNull(varData);
    Assert.assertEquals(1, varData.getDatas().size());
    Assert.assertEquals(DataType.TEXT, varData.getDatas().get(0).getType());
    Assert.assertEquals("123", varData.getDatas().get(0).getValue());
  }

  @Test
  public void testParticipantConfiguredAttribute() {
    Variable root = createVariable();

    Variable variable = root.getVariable(OnyxVariableProvider.ADMIN).getVariable(OnyxVariableProvider.PARTICIPANT).getVariable(COUNTRY);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();

    expect(participantServiceMock.getConfiguredAttributeValue(participant, COUNTRY)).andReturn(DataBuilder.buildText("Canada"));
    replay(participantServiceMock);

    VariableData varData = variableProvider.getVariableData(participant, variable, variablePathNamingStrategy);
    Assert.assertNotNull(varData);
    Assert.assertEquals(1, varData.getDatas().size());
    Assert.assertEquals(DataType.TEXT, varData.getDatas().get(0).getType());
    Assert.assertEquals("Canada", varData.getDatas().get(0).getValue());

    verify(participantServiceMock);

  }

  @Test
  public void testInterview() {
    Variable root = createVariable();

    Variable variable = root.getVariable(OnyxVariableProvider.ADMIN).getVariable(OnyxVariableProvider.INTERVIEW).getVariable(OnyxVariableProvider.INTERVIEW_STATUS);
    Assert.assertNotNull(variable);

    Participant participant = new Participant();
    Interview interview = new Interview();
    interview.setStatus(InterviewStatus.COMPLETED);
    participant.setInterview(interview);

    VariableData varData = variableProvider.getVariableData(participant, variable, variablePathNamingStrategy);
    Assert.assertNotNull(varData);
    Assert.assertEquals(1, varData.getDatas().size());
    Assert.assertEquals(DataType.TEXT, varData.getDatas().get(0).getType());
    Assert.assertEquals(InterviewStatus.COMPLETED.toString(), varData.getDatas().get(0).getValue());

  }

  @Test
  public void testAction() {
    Variable root = createVariable();

    Variable variable = root.getVariable(OnyxVariableProvider.ADMIN).getVariable(OnyxVariableProvider.ACTION).getVariable(OnyxVariableProvider.ACTIONS);
    Assert.assertNotNull(variable);
    Assert.assertEquals(OnyxVariableProvider.ACTION_KEY, variable.getKey());

    Participant participant = new Participant();
    List<Action> actions = new ArrayList<Action>();

    Action action = new Action();
    action.setId(1);
    action.setComment("toto");
    actions.add(action);

    action = new Action();
    action.setId(2);
    action.setComment("tata");
    actions.add(action);

    expect(participantServiceMock.getActions(participant)).andReturn(actions);
    replay(participantServiceMock);

    VariableData varData = variableProvider.getVariableData(participant, variable, variablePathNamingStrategy);
    Assert.assertNotNull(varData);
    Assert.assertEquals(2, varData.getDatas().size());
    Assert.assertEquals(DataType.INTEGER, varData.getDatas().get(0).getType());
    Assert.assertEquals("1", varData.getDatas().get(0).getValueAsString());

    Assert.assertEquals(DataType.INTEGER, varData.getDatas().get(1).getType());
    Assert.assertEquals("2", varData.getDatas().get(1).getValueAsString());

    verify(participantServiceMock);
  }

  @Test
  public void testActionComment() {
    Variable root = createVariable();

    Variable variable = root.getVariable(OnyxVariableProvider.ADMIN).getVariable(OnyxVariableProvider.ACTION).getVariable(OnyxVariableProvider.ACTION_COMMENT);
    Assert.assertNotNull(variable);
    Assert.assertEquals(1, variable.getReferences().size());
    Assert.assertEquals(OnyxVariableProvider.ACTION_KEY, variable.getReferences().get(0));

    Participant participant = new Participant();
    List<Action> actions = new ArrayList<Action>();

    Action action = new Action();
    action.setId(1);
    action.setComment("toto");
    actions.add(action);

    action = new Action();
    action.setId(2);
    action.setComment("tata");
    actions.add(action);

    expect(participantServiceMock.getActions(participant)).andReturn(actions);
    replay(participantServiceMock);

    VariableData varData = variableProvider.getVariableData(participant, variable, variablePathNamingStrategy);
    Assert.assertNotNull(varData);
    Assert.assertEquals("/Root/Admin/Action/comment", varData.getVariablePath());
    Assert.assertEquals(0, varData.getDatas().size());
    Assert.assertEquals(2, varData.getVariableDatas().size());

    VariableData subVarData = varData.getVariableDatas().get(0);
    Assert.assertEquals("/Root/Admin/Action/comment?action=1", subVarData.getVariablePath());
    Assert.assertEquals(1, subVarData.getDatas().size());
    Assert.assertEquals(DataType.TEXT, subVarData.getDatas().get(0).getType());
    Assert.assertEquals("toto", subVarData.getDatas().get(0).getValueAsString());

    subVarData = varData.getVariableDatas().get(1);
    Assert.assertEquals("/Root/Admin/Action/comment?action=2", subVarData.getVariablePath());
    Assert.assertEquals(1, subVarData.getDatas().size());
    Assert.assertEquals(DataType.TEXT, subVarData.getDatas().get(0).getType());
    Assert.assertEquals("tata", subVarData.getDatas().get(0).getValueAsString());

    verify(participantServiceMock);
  }

  @Test
  public void testUser() {
    Variable root = createVariable();

    Variable variable = root.getVariable(OnyxVariableProvider.ADMIN).getVariable(OnyxVariableProvider.USER).getVariable(OnyxVariableProvider.USER_LOGIN);
    Assert.assertNotNull(variable);
    Assert.assertEquals(OnyxVariableProvider.USER_KEY, variable.getKey());

    Participant participant = new Participant();
    List<User> users = new ArrayList<User>();

    User user = new User();
    user.setLogin("toto");
    users.add(user);

    user = new User();
    user.setLogin("tata");
    users.add(user);

    expect(queryServiceMock.match((User) EasyMock.anyObject())).andReturn(users);
    replay(queryServiceMock);

    VariableData varData = variableProvider.getVariableData(participant, variable, variablePathNamingStrategy);
    Assert.assertNotNull(varData);
    Assert.assertEquals(2, varData.getDatas().size());
    Assert.assertEquals(DataType.TEXT, varData.getDatas().get(0).getType());
    Assert.assertEquals("toto", varData.getDatas().get(0).getValue());

    Assert.assertEquals(DataType.TEXT, varData.getDatas().get(1).getType());
    Assert.assertEquals("tata", varData.getDatas().get(1).getValue());

    verify(queryServiceMock);
  }

  @Test
  public void testUserRoles() {
    Variable root = createVariable();

    Variable variable = root.getVariable(OnyxVariableProvider.ADMIN).getVariable(OnyxVariableProvider.USER).getVariable(OnyxVariableProvider.USER_ROLES);
    Assert.assertNotNull(variable);
    Assert.assertEquals(1, variable.getReferences().size());
    Assert.assertEquals(OnyxVariableProvider.USER_KEY, variable.getReferences().get(0));

    Participant participant = new Participant();
    List<User> users = new ArrayList<User>();

    User user = new User();
    user.setLogin("toto");
    user.addRole(Role.DATA_COLLECTION_OPERATOR);
    user.addRole(Role.PARTICIPANT_MANAGER);
    users.add(user);

    user = new User();
    user.setLogin("tata");
    user.addRole(Role.SYSTEM_ADMINISTRATOR);
    users.add(user);

    expect(queryServiceMock.match((User) EasyMock.anyObject())).andReturn(users);
    replay(queryServiceMock);

    VariableData varData = variableProvider.getVariableData(participant, variable, variablePathNamingStrategy);
    Assert.assertNotNull(varData);
    Assert.assertEquals("/Root/Admin/User/roles", varData.getVariablePath());
    Assert.assertEquals(0, varData.getDatas().size());
    Assert.assertEquals(2, varData.getVariableDatas().size());

    VariableData subVarData = varData.getVariableDatas().get(0);
    Assert.assertEquals("/Root/Admin/User/roles?user=toto", subVarData.getVariablePath());
    Assert.assertEquals(2, subVarData.getDatas().size());
    Assert.assertEquals(DataType.TEXT, subVarData.getDatas().get(0).getType());
    Assert.assertEquals(Role.DATA_COLLECTION_OPERATOR.toString(), subVarData.getDatas().get(0).getValue());
    Assert.assertEquals(Role.PARTICIPANT_MANAGER.toString(), subVarData.getDatas().get(1).getValue());

    subVarData = varData.getVariableDatas().get(1);
    Assert.assertEquals("/Root/Admin/User/roles?user=tata", subVarData.getVariablePath());
    Assert.assertEquals(1, subVarData.getDatas().size());
    Assert.assertEquals(DataType.TEXT, subVarData.getDatas().get(0).getType());
    Assert.assertEquals(Role.SYSTEM_ADMINISTRATOR.toString(), subVarData.getDatas().get(0).getValue());

    verify(queryServiceMock);
  }

  private Variable createVariable() {
    Variable root = new Variable("Root");
    for(Variable variable : variableProvider.getVariables()) {
      root.addVariable(variable);
    }
    return root;
  }

}
