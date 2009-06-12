/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.engine.state.variable.configurable;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.data.RegexDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.core.domain.participant.Group;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.engine.variable.configurable.DataSourceVariable;
import org.obiba.onyx.util.data.DataType;

import com.thoughtworks.xstream.XStream;

/**
 * Test XStream configuration needed to produce the configurable-variable.xml file.
 */
public class DataSourceVariableWriterTest {

  private XStream xstream;

  private VariableDataSource postalCodeDataSource;

  private RegexDataSource regexDataSource;

  @Before
  public void setUp() throws Exception {
    xstream = new XStream();
    xstream.alias("attribute", ParticipantAttribute.class);

    postalCodeDataSource = new VariableDataSource("Onyx.Admin.Participant.Postal_Code");
    regexDataSource = new RegexDataSource(postalCodeDataSource, "^\\s*([a-zA-Z]\\d[a-zA-Z]).*$");
  }

  @Test
  public void testWriteXmlConfigurableVariablesTest() {
    List<DataSourceVariable> variables = new ArrayList<DataSourceVariable>();
    xstream.alias("variables", List.class);
    xstream.alias("dataSourceVariable", DataSourceVariable.class);
    xstream.alias("regexDataSource", RegexDataSource.class);
    xstream.alias("variableDataSource", VariableDataSource.class);
    xstream.alias("group", Group.class);
    xstream.useAttributeFor(Group.class, "name");
    xstream.addImplicitCollection(Group.class, "participantAttributes");

    variables.add(new DataSourceVariable("PostalCodePrefix", DataType.TEXT, regexDataSource));

    String xml = xstream.toXML(variables);
    System.out.println(xml);
  }

}
