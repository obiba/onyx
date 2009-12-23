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
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.ValueSet;
import org.obiba.magma.filter.ExcludeAllFilter;
import org.obiba.magma.filter.Filter;
import org.obiba.magma.filter.JavaScriptFilter;
import org.obiba.magma.js.MagmaJsExtension;

import com.thoughtworks.xstream.XStream;

/**
 * Test XStream configuration needed to produce the purge.xml file.
 */
public class OnyxDataPurgeWriterTest {

  private XStream xstream;

  private List<OnyxDataExportDestination> destinations;

  private OnyxDataExportDestination destinationDcc;

  @Before
  public void setUp() throws Exception {
    new MagmaEngine().extend(new MagmaJsExtension());

    xstream = new XStream();
    xstream.alias("list", List.class);
    xstream.alias("purge", OnyxDataExportDestination.class);
    xstream.autodetectAnnotations(true);

    destinationDcc = new OnyxDataExportDestination();

    List<ValueSetFilter> valueSetFilters = new ArrayList<ValueSetFilter>();
    ValueSetFilter participantValueSetFilter = new ValueSetFilter("Participant");
    valueSetFilters.add(participantValueSetFilter);
    destinationDcc.setValueSetFilters(valueSetFilters);

    Filter<ValueSet> excludeAll = ExcludeAllFilter.Builder.newFilter().buildForValueSet();

    participantValueSetFilter.getEntityFilterChain().addFilter(excludeAll);

    Filter<ValueSet> varOne = JavaScriptFilter.Builder.newFilter().javascript("$('Admin.Participant.exported').any('TRUE')").include().build();
    participantValueSetFilter.getEntityFilterChain().addFilter(varOne);

    Filter<ValueSet> varTwo = JavaScriptFilter.Builder.newFilter().javascript("$('Participant.Interview.status').any('CLOSED','COMPLETED') && $('Participant.Interview.endData') > $('LastExportDate')").include().build();
    participantValueSetFilter.getEntityFilterChain().addFilter(varTwo);

    destinations = new ArrayList<OnyxDataExportDestination>();

    destinations.add(destinationDcc);

  }

  @After
  public void cleanUp() throws Exception {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void writeTest() throws Exception {
    String xml = xstream.toXML(destinations);
    System.out.println(xml);
  }
}
