/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.summitdoppler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.onyx.jade.instrument.summitdoppler.VantageReportParser.ExamData;
import org.obiba.onyx.jade.instrument.summitdoppler.VantageReportParser.SideData;

/**
 *
 */
public class VantageReportParserTest {

  @Test
  public void testParse() throws URISyntaxException, IOException {
    VantageReportParser parser = new VantageReportParser();

    File file = new File(getClass().getResource("/VAN00303.ABI").toURI());

    parser.parse(file);

    Assert.assertEquals(1, parser.getExamCount());
    ExamData exam = parser.getExamData(0);
    Assert.assertNotNull(exam);

    Assert.assertEquals("TEST", exam.getName());
    Assert.assertEquals("Thu Jul 14 11:52:15 EDT 2011", exam.getTimestamp().toString());

    SideData side = exam.getLeft();
    Assert.assertNotNull(side);
    Assert.assertEquals(120, side.getBrachial().intValue());
    Assert.assertEquals(135, side.getAnkle().intValue());
    Assert.assertEquals(109, (int) (side.getIndex().doubleValue() * 100));
    Assert.assertEquals("Thu Jul 14 11:51:16 EDT 2011", side.getClock().toString());
    Assert.assertEquals("94887C71655A50443D36322E2C2B2B2C2D2F3133353738393A3A3B3B3A3A3A38373634312F2C2A28292E394A5F768DA7BACAD6DEE3E3E1DCD5CBC2B8AEA297897E73685E554C453E3B393838393A3C3D3E3F3F3F4040404040403F3F3D3C3B39373533302D2926221F1D1C212B3C516F88A0B5C7D6E1E7E9E8E3DCD3CAC0B5A79B8F83786D62594F48433F3C3A3A3B3C3D3F4142434444444444434342413F3D3C3A39383635322F2C2926221F1C191613110F0E111928425C7893ACC2D4E1ECF0F0EEEAE4DCD3C7BCB1A69A8E83786C645C56514E4C4A4A4A4B4B4C4C4D4D4E4C4C4C4C4C4B4A49480F1011131517191B1D1F2122242526272726262423201E1B181615151A24384E667D94A7B9C5CED1D1CFCAC2BBB0A094877B6E6054483A3028211C1917161617191A1C1E20212324252626272727262422201D1B181614151B27384E657D97ABBAC3C8CAC8C4BDB4ABA1968B7F7465594D42382F27201B181615151617191B1C1E1F2022232425262626252423211F1C1B1B1D2431435E768DA2B5C2CDD4D8D8D4CFC8BFB6AC9F", side.toString(side.getWaveForm()));
    Assert.assertEquals("x8", side.getScale());

    side = exam.getRight();
    Assert.assertNotNull(side);
    Assert.assertEquals(123, side.getBrachial().intValue());
    Assert.assertEquals(130, side.getAnkle().intValue());
    Assert.assertEquals(105, (int) (side.getIndex().doubleValue() * 100));
    Assert.assertEquals("Thu Jul 14 11:52:15 EDT 2011", side.getClock().toString());
    Assert.assertEquals("7C70655A5148403A363432303031313233343434353434343333333333323231302F2D2B292723201C191513121520324964819CB4CBDDE6ECEEEEECE6E0D6CCC3B8ADA2968B7D72685E564E48433E3C3A39393A3B3B3D3D3E3E3E3E3E3D3C3B3B3A3A3837363332302D2A2825221F1E212A384D657F9DB3C8D7E1E8EAECE9E4DED6CDC3B9AFA1968B80766B6259514B4643403F403F40414242444445454545454545444342403E3D3A383533302C2926232121273043597089A0B5C8D5E0E5E6E6E2DDD7D0C7BDB3A89C91867A6E655C554E494542403F3F3F4142424545464646464645454445443B39383634312E2B2927292E394F67809DB4C7D8E4EDF1F1EEE9E3DBD2C4B9ACA1958A7E72645A5047403934302E2E2F3031323334353636363736363534343331302E2D2B282624201D1A1819202F435D7995AEC5D8E2EAEDEDEAE4DDD3C9BFB2A79B9084776C62595049433E3A383737373839393A3B3B3C3C3C3B3B3B3A3A393837363432302D2A2724211E1A18191D27394F6988A0B9CAD6DFE4E5E3DED6CDC3B7ACA19387", side.toString(side.getWaveForm()));
    Assert.assertEquals("x8", side.getScale());
  }

}
