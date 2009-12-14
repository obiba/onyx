/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Set;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.js.JavascriptVariableValueSource;
import org.obiba.magma.xstream.MagmaXStreamExtension;
import org.obiba.magma.xstream.XStreamFactory;
import org.springframework.core.io.Resource;

import com.google.common.collect.ImmutableSet;
import com.thoughtworks.xstream.XStream;

/**
 * Reads an XStream serialisation of custom variables. Custom variables are all expected to be defined as javascript.
 * 
 * @see JavascriptVariableValueSource
 * @see XStreamFactory
 */
public class ScriptedVariableValueSourceFactory implements VariableValueSourceFactory {

  private Resource resource;

  public Set<VariableValueSource> createSources() {
    if(resource.exists() == false) {
      return ImmutableSet.of();
    }

    try {
      ImmutableSet.Builder<VariableValueSource> sources = new ImmutableSet.Builder<VariableValueSource>();
      ObjectInputStream ois = getXStream().createObjectInputStream(resource.getInputStream());
      try {
        while(true) {
          Variable variable = (Variable) ois.readObject();
          sources.add(new JavascriptVariableValueSource(variable));
        }
      } catch(EOFException e) {
        // Reached the end of the file.
      } finally {
        ois.close();
      }
      return sources.build();
    } catch(ClassNotFoundException e) {
      throw new RuntimeException(e);
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Resource getResource() {
    return resource;
  }

  public void setResource(Resource resource) {
    this.resource = resource;
  }

  private XStream getXStream() {
    return MagmaEngine.get().getExtension(MagmaXStreamExtension.class).getXStreamFactory().createXStream();
  }

}
