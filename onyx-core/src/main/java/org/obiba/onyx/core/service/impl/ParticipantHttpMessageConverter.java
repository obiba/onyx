/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.service.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.thoughtworks.xstream.XStream;

/**
 * Rest utility that reads from the HTTP response and creates a {@link Participant} from that.
 */
public class ParticipantHttpMessageConverter implements HttpMessageConverter<Participant> {

  public boolean canRead(Class<?> clazz, MediaType mediaType) {
    return Participant.class.equals(clazz);
  }

  public boolean canWrite(Class<?> clazz, MediaType mediaType) {
    return false;
  }

  public List<MediaType> getSupportedMediaTypes() {
    return Collections.singletonList(new MediaType("text", "xml"));
  }

  public Participant read(Class<? extends Participant> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    XStream xstream = new XStream();
    return (Participant) xstream.fromXML(inputMessage.getBody());
  }

  public void write(Participant t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    throw new UnsupportedOperationException("Not implemented");
  }

}
