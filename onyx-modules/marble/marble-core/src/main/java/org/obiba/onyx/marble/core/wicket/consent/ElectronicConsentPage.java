/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.marble.core.wicket.consent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.wicket.Component;
import org.apache.wicket.IResourceListener;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.io.Streams;
import org.obiba.onyx.marble.core.service.FdfProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElectronicConsentPage extends WebPage implements IResourceListener {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(ElectronicConsentPage.class);

  @SpringBean
  private FdfProducer fdfProducer;

  private DynamicWebResource pdfResource;

  private DynamicWebResource fdfResource;

  private Component finishButton;

  @SuppressWarnings("serial")
  public ElectronicConsentPage(final Component finishButton) {

    pdfResource = new DynamicWebResource() {
      @Override
      protected ResourceState getResourceState() {
        return new ResourceState() {
          @Override
          public String getContentType() {
            return "application/pdf";
          }

          @Override
          public byte[] getData() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
              Streams.copy(fdfProducer.getPdfTemplate(), baos);
            } catch(IOException e) {
              throw new RuntimeException(e);
            }
            return baos.toByteArray();
          }
        };
      }

    };

    fdfResource = new DynamicWebResource() {
      @Override
      protected ResourceState getResourceState() {
        return new ResourceState() {
          byte[] fdf;

          @Override
          public String getContentType() {
            return "application/vnd.fdf";
          }

          @Override
          synchronized public byte[] getData() {
            if(fdf == null) {
              String pdfUrl = RequestUtils.toAbsolutePath(urlFor(IResourceListener.INTERFACE) + "#toolbar=0&navpanes=0&scrollbar=1");
              log.info("PDF URL is {}", pdfUrl);
              PageParameters params = new PageParameters();
              params.add("accepted", "true");
              params.add("finishLinkId", finishButton.getMarkupId());
              String acceptUrl = RequestUtils.toAbsolutePath(urlFor(ElectronicConsentUploadPage.class, params).toString());

              params = new PageParameters();
              params.add("accepted", "false");
              params.add("finishLinkId", finishButton.getMarkupId());
              String refuseUrl = RequestUtils.toAbsolutePath(urlFor(ElectronicConsentUploadPage.class, params).toString());
              try {
                fdf = fdfProducer.buildFdf(pdfUrl, acceptUrl, refuseUrl);
              } catch(IOException e) {
                fdf = null;
                throw new RuntimeException(e);
              }
            }
            return fdf;
          }
        };
      }
    };

    add(new FdfEmbedTag());
  }

  public void onResourceRequested() {
    pdfResource.onResourceRequested();
  }

  private class FdfEmbedTag extends WebMarkupContainer implements IResourceListener {

    private static final long serialVersionUID = 1L;

    public FdfEmbedTag() {
      super("embedPdf");
    }

    public void onResourceRequested() {
      fdfResource.onResourceRequested();
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
      super.onComponentTag(tag);
      CharSequence fdfUrl = urlFor(IResourceListener.INTERFACE);
      log.info("FDF url is {}", fdfUrl);
      tag.put("src", fdfUrl);
      tag.put("type", "application/vnd.fdf");
    }
  }
}
