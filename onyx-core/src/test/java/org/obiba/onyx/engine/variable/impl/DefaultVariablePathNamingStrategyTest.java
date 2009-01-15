/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.impl;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.engine.variable.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default variable path is for instance:
 */
public class DefaultVariablePathNamingStrategyTest {

  private static final Logger log = LoggerFactory.getLogger(DefaultVariablePathNamingStrategyTest.class);

  private DefaultVariablePathNamingStrategy strategy;

  @Before
  public void setUp() {
    strategy = new DefaultVariablePathNamingStrategy();
    strategy.setRootName("Root");
    strategy.setPathSeparator("/");
    strategy.setStartWithPathSeparator(true);
  }

  @Test
  public void testParameters() {
    Variable variable = (new Variable(strategy.getRootName())).addVariable("Test/Toto", strategy.getPathSeparator());

    String path;
    log.info(path = strategy.getPath(variable));
    Assert.assertEquals("/Root/Test/Toto", path);

    log.info(path = strategy.getPath(variable, "id", "1"));
    Assert.assertEquals("/Root/Test/Toto?id=1", path);

    log.info(path = strategy.addParameters(strategy.addParameters(strategy.getPath(variable, "id", "1"), "name", "Vincent Ferreti"), "path", "/tutu/tata"));
    Assert.assertEquals("/Root/Test/Toto?id=1&name=Vincent+Ferreti&path=%2Ftutu%2Ftata", path);

    List<String> names = strategy.getNormalizedNames(path);
    log.info("names={}", names);
    Assert.assertEquals(3, names.size());
    Assert.assertEquals("Root", names.get(0));
    Assert.assertEquals("Test", names.get(1));
    Assert.assertEquals("Toto", names.get(2));

    Assert.assertEquals(0, strategy.getParameters("/Root/Test/Toto?").size());

    Map<String, String> map = strategy.getParameters(path);
    Assert.assertEquals(3, map.size());
    Assert.assertTrue(map.containsKey("id"));
    Assert.assertEquals("1", map.get("id"));
    Assert.assertTrue(map.containsKey("name"));
    Assert.assertEquals("Vincent Ferreti", map.get("name"));
    Assert.assertTrue(map.containsKey("path"));
    Assert.assertEquals("/tutu/tata", map.get("path"));
  }
}
