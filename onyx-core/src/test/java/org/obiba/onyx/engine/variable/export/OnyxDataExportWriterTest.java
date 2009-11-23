/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.ValueSet;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.engine.output.Strategies;
import org.obiba.magma.filter.ExcludeAllFilter;
import org.obiba.magma.filter.Filter;
import org.obiba.magma.filter.JavaScriptFilter;
import org.obiba.magma.filter.VariableAttributeFilter;
import org.obiba.magma.filter.VariableNameFilter;

import com.thoughtworks.xstream.XStream;

/**
 * Test XStream configuration needed to produce the export-destinations.xml file.
 */
public class OnyxDataExportWriterTest {

  private XStream xstream;

  private List<OnyxDestination> destinations;

  private OnyxDestination destinationDcc;

  @Before
  public void setUp() throws Exception {
    xstream = new XStream();
    xstream.alias("destinations", List.class);
    xstream.alias("destination", OnyxDestination.class);
    xstream.alias("exportedInterviewStatuses", HashSet.class);
    xstream.autodetectAnnotations(true);

    destinationDcc = new OnyxDestination();
    destinationDcc.setName("DCC");

    List<String> strategiesList = new ArrayList<String>();
    strategiesList.add("encrypt");
    strategiesList.add("zip");
    strategiesList.add("file");

    destinationDcc.setStrategies(new Strategies(strategiesList));

    List<ValueSetFilter> valueSetFilters = new ArrayList<ValueSetFilter>();
    ValueSetFilter participantValueSetFilter = new ValueSetFilter("Participant");
    valueSetFilters.add(participantValueSetFilter);
    destinationDcc.setValueSetFilters(valueSetFilters);

    Filter<ValueSet> excludeAll = ExcludeAllFilter.Builder.newFilter().buildForValueSet();
    Filter<ValueSet> excludeAll2 = ExcludeAllFilter.Builder.newFilter().buildForValueSet();

    participantValueSetFilter.getEntityFilterChain().addFilter(excludeAll);
    participantValueSetFilter.getEntityFilterChain().addFilter(excludeAll2);

    Filter<VariableValueSource> varOne = VariableNameFilter.Builder.newFilter().prefix("Admin.Participant").include().build();
    participantValueSetFilter.getVariableFilterChain().addFilter(varOne);

    Filter<VariableValueSource> varTwo = VariableAttributeFilter.Builder.newFilter().attributeName("name").attributeValue("value").exclude().build();
    participantValueSetFilter.getVariableFilterChain().addFilter(varTwo);

    Filter<ValueSet> varThree = JavaScriptFilter.Builder.newFilter().javascript("$('Participant.Interview.status').any('CLOSED','COMPLETED')").exclude().build();
    participantValueSetFilter.getEntityFilterChain().addFilter(varThree);

    destinations = new ArrayList<OnyxDestination>();

    destinations.add(destinationDcc);

  }

  @Test
  public void writeTest() throws Exception {
    String xml = xstream.toXML(destinations);
    System.out.println(xml);
  }
}
