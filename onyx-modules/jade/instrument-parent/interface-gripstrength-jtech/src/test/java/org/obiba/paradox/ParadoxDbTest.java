/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.paradox;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.paradox.ParadoxDb.ParadoxDbBlock;

public class ParadoxDbTest {

  @Test
  public void test_canReadHeader() throws IOException {
    for(File file : new File("src/test/resources/db").listFiles()) {
      if(file.getName().endsWith("DB")) {
        System.out.println("Reading " + file.getName());
        ParadoxDb db = new ParadoxDb(file);
        System.out.println(db);

        System.out.println(Arrays.toString(db.getHeader().getFieldNames().toArray()));
        for(ParadoxRecord record : db) {
          System.out.println(Arrays.toString(record.getValues()));
        }
      }
    }
  }

  @Test
  public void test_dbContainsEmptyBlock() throws IOException {
    ParadoxDb db = new ParadoxDb(new File("src/test/resources/db/ZSustainedTestData.DB"));
    Assert.assertTrue(db.getHeader().getFileBlocks() > 0);
    for(ParadoxDbBlock block : db.getBlocks()) {
      Assert.assertEquals(0, block.numRecords());
    }
  }
}
