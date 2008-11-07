/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.core.domain.parser.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.springframework.context.MessageSourceResolvable;

/**
 *
 */
public class ParticipantCodeBarcodePartParserTest {

  private ActiveInterviewService activeInterviewServiceMock;

  private ParticipantCodeBarcodePartParser parser;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    activeInterviewServiceMock = createMock(ActiveInterviewService.class);
    parser = new ParticipantCodeBarcodePartParser();
    parser.setSize(5);
    parser.setFormat("^[A-Za-z0-9]+$");
    parser.setActiveInterviewService(activeInterviewServiceMock);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.ParticipantCodeBarcodePartParser#validatePart(java.lang.String, org.obiba.onyx.core.service.ActiveInterviewService)}
   * .
   */
  @Test
  public void testShouldPassValidatePart() {
    String part = "54321";
    Participant participant = new Participant();
    participant.setBarcode(part);
    expect(activeInterviewServiceMock.getParticipant()).andReturn(participant);
    replay(activeInterviewServiceMock);
    MessageSourceResolvable error = parser.validatePart(part);

    verify(activeInterviewServiceMock);
    Assert.assertNull(error);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.FixedSizeBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, org.obiba.onyx.core.service.ActiveInterviewService, java.util.List)}
   * .
   */
  @Test
  public void testShouldPassEatAndValidatePart() {
    StringBuilder barcodeFragment = new StringBuilder("5432108981");
    String part = "54321";
    Participant participant = new Participant();
    participant.setBarcode(part);
    expect(activeInterviewServiceMock.getParticipant()).andReturn(participant);
    replay(activeInterviewServiceMock);

    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    BarcodePart barcodePart = parser.eatAndValidatePart(barcodeFragment, errors);

    verify(activeInterviewServiceMock);
    Assert.assertEquals(0, errors.size());
    Assert.assertEquals(part, barcodePart.getPart());
    Assert.assertEquals("08981", barcodeFragment.toString());
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.FixedSizeBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, org.obiba.onyx.core.service.ActiveInterviewService, java.util.List)}
   * .
   */
  @Test
  public void testShouldFailValidationWithFormatError() {
    StringBuilder barcodeFragment = new StringBuilder("!5432108981");

    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    BarcodePart barcodePart = parser.eatAndValidatePart(barcodeFragment, errors);

    Assert.assertEquals(1, errors.size());
    Assert.assertNull(barcodePart);
    Assert.assertEquals("ParticipantCodeFormatError", errors.get(0).getCodes()[0]);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.FixedSizeBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, org.obiba.onyx.core.service.ActiveInterviewService, java.util.List)}
   * .
   */
  @Test
  public void testShouldFailValidationWithParticipantNotFoundError() {
    StringBuilder barcodeFragment = new StringBuilder("5432108981");

    expect(activeInterviewServiceMock.getParticipant()).andReturn(null);
    replay(activeInterviewServiceMock);

    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    BarcodePart barcodePart = parser.eatAndValidatePart(barcodeFragment, errors);

    verify(activeInterviewServiceMock);
    Assert.assertEquals(1, errors.size());
    Assert.assertNull(barcodePart);
    Assert.assertEquals("ParticipantNotFoundError", errors.get(0).getCodes()[0]);
  }

  /**
   * Test method for
   * {@link org.obiba.onyx.ruby.core.domain.parser.impl.FixedSizeBarcodePartParser#eatAndValidatePart(java.lang.StringBuilder, org.obiba.onyx.core.service.ActiveInterviewService, java.util.List)}
   * .
   */
  @Test
  public void testShouldFailValidationWithParticipantNotMatchError() {
    StringBuilder barcodeFragment = new StringBuilder("5432108981");

    Participant participant = new Participant();
    participant.setBarcode("3210");

    expect(activeInterviewServiceMock.getParticipant()).andReturn(participant);
    replay(activeInterviewServiceMock);

    List<MessageSourceResolvable> errors = new ArrayList<MessageSourceResolvable>();
    BarcodePart barcodePart = parser.eatAndValidatePart(barcodeFragment, errors);

    verify(activeInterviewServiceMock);
    Assert.assertEquals(1, errors.size());
    Assert.assertNull(barcodePart);
    Assert.assertEquals("ParticipantCodeMatchError", errors.get(0).getCodes()[0]);
  }

}
