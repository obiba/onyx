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

import java.util.Arrays;

import junit.framework.Assert;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.engine.variable.util.VariableStreamer;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class OnyxVariableProviderTest {

  private static final Logger log = LoggerFactory.getLogger(OnyxVariableProviderTest.class);

  private IVariableProvider variableProvider;

  private ParticipantMetadata participantMetadataMock;

  private ParticipantService participantServiceMock;

  private EntityQueryService queryServiceMock;

  @Before
  public void setUp() {
    ApplicationContextMock mockCtx = new ApplicationContextMock();
    participantServiceMock = createMock(ParticipantService.class);
    queryServiceMock = createMock(EntityQueryService.class);
    mockCtx.putBean("participantService", participantServiceMock);
    mockCtx.putBean("persistenceManager", queryServiceMock);

    participantMetadataMock = new ParticipantMetadata();
    ParticipantAttribute attr = new ParticipantAttribute();
    attr.setName("country");
    attr.setType(DataType.TEXT);
    participantMetadataMock.setConfiguredAttributes(Arrays.asList(new ParticipantAttribute[] { attr }));

    variableProvider = new OnyxVariableProvider();
    ((OnyxVariableProvider) variableProvider).setParticipantMetadata(participantMetadataMock);
    ((OnyxVariableProvider) variableProvider).setParticipantService(participantServiceMock);
    ((OnyxVariableProvider) variableProvider).setQueryService(queryServiceMock);
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
    Assert.assertNotNull(subVar.getVariable("country"));
    Assert.assertEquals(DataType.TEXT, subVar.getVariable("country").getDataType());

    subVar = variable.getVariable(OnyxVariableProvider.INTERVIEW);
    Assert.assertNotNull(subVar);

    subVar = variable.getVariable(OnyxVariableProvider.ACTION);
    Assert.assertNotNull(subVar);

    subVar = variable.getVariable(OnyxVariableProvider.USER);
    Assert.assertNotNull(subVar);
  }

  private Variable createVariable() {
    Variable root = new Variable("Root");
    for(Variable variable : variableProvider.getVariables()) {
      root.addVariable(variable);
    }
    return root;
  }

}
