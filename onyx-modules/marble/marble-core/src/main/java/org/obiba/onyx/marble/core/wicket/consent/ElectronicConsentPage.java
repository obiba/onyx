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
import java.io.InputStream;

import org.apache.wicket.IResourceListener;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.DynamicWebResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.target.coding.AbstractRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;

public class ElectronicConsentPage extends WebPage implements IResourceListener {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("serial")
  public ElectronicConsentPage() {

    add(new WebMarkupContainer("embedPdf") {
      @Override
      protected void onComponentTag(ComponentTag tag) {
        CharSequence url = ElectronicConsentPage.this.urlFor(IResourceListener.INTERFACE);
        String srcTagValue = url.toString();
        tag.put("data", srcTagValue);
        super.onComponentTag(tag);
      }
    });

    try {
      ((WebApplication) getApplication()).mountBookmarkablePage("/uploadConsent", ConsentUploadPage.class);
    } catch(Exception ex) {
    }

  }

  public void save() {
    // TODO Auto-generated method stub

  }

  @SuppressWarnings("serial")
  public void onResourceRequested() {
    Resource r = new DynamicWebResource() {
      @Override
      protected ResourceState getResourceState() {

        return new ResourceState() {

          @Override
          public String getContentType() {
            return "application/pdf";
          }

          @Override
          public byte[] getData() {
            InputStream pdfStream = getClass().getResourceAsStream("/test.pdf");
            ByteArrayOutputStream convertedStream = new ByteArrayOutputStream();
            byte[] readBuffer = new byte[1024];
            int bytesRead;

            try {
              while((bytesRead = pdfStream.read(readBuffer)) > 0) {
                convertedStream.write(readBuffer, 0, bytesRead);
              }
            } catch(IOException couldNotReadStream) {
              throw new RuntimeException(couldNotReadStream);
            }

            return convertedStream.toByteArray();
          }

        };
      }

    };

    RequestCycle.get().setRequestTarget(new ResourceStreamRequestTarget(r.getResourceStream()));

  }
}
