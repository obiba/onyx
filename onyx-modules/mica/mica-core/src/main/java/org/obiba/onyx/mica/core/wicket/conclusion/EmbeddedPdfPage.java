/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.mica.core.wicket.conclusion;

import org.apache.wicket.IResourceListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.DynamicWebResource.ResourceState;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.marble.core.service.ActiveConsentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedPdfPage extends WebPage {

  @SpringBean
  protected ActiveConsentService activeConsentService;

  protected DynamicWebResource pdfResource;

  protected static final Logger log = LoggerFactory.getLogger(EmbeddedPdfPage.class);

  @SuppressWarnings("serial")
  public EmbeddedPdfPage(final byte[] pdfContent) {

    pdfResource = new DynamicWebResource() {

      protected ResourceState getResourceState() {
        return new PdfResourceState(pdfContent);
      }
    };

    add(new PdfEmbedTag());

  }

  public void onResourceRequested() {
    pdfResource.onResourceRequested();
  }

  protected class PdfResourceState extends ResourceState {
    private byte[] stream;

    public PdfResourceState(byte[] data) {
      stream = data;
    }

    public byte[] getData() {
      return stream;
    }

    public int getLength() {
      return stream.length;
    }

    public String getContentType() {
      return "application/pdf";
    }
  }

  protected class PdfEmbedTag extends WebMarkupContainer implements IResourceListener {

    private static final long serialVersionUID = 1L;

    public PdfEmbedTag() {
      super("embedPdf");
    }

    public void onResourceRequested() {
      pdfResource.onResourceRequested();
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
      super.onComponentTag(tag);
      String pdfUrl = RequestUtils.toAbsolutePath(urlFor(IResourceListener.INTERFACE) + "#toolbar=1&navpanes=0&scrollbar=1");
      log.info("Embedded PDF url is {}", pdfUrl);
      tag.put("src", pdfUrl);
      tag.put("type", "application/pdf");
    }
  }

}
