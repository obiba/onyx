/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.util.data;

import java.util.Date;
import java.util.Random;

import org.obiba.onyx.util.Base64;

/**
 * 
 */
public class RandomDataBuilder {

  static final private Random random = new Random();

  static public void setRandomSeed(long seed) {
    random.setSeed(seed);
  }

  static public Data buildRandom(DataType type) {
    switch(type) {
    case BOOLEAN:
      return DataBuilder.buildBoolean(random.nextBoolean());
    case INTEGER:
      return DataBuilder.buildInteger(random.nextLong());
    case TEXT:
      // Generate 50 random characters
      byte[] randomBytes = new byte[random.nextInt(50) + 1];
      random.nextBytes(randomBytes);
      // Encode the bytes in base 64 so we get "readable" characters.
      return DataBuilder.buildText(Base64.encodeBytes(randomBytes));
    case DATE:
      return DataBuilder.buildDate(new Date(random.nextLong()));
    case DECIMAL:
      return DataBuilder.buildDecimal(random.nextDouble());
    default:
      return null;
    }
  }

}
