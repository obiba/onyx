/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.obiba.onyx.util.data.Data;

/**
 * 
 */
public class ReferingData extends VariableData {

  private static final long serialVersionUID = 1L;

  private VariableData referingData;

  public ReferingData() {
    super();
  }

  public ReferingData(Data data) {
    super(data);
  }

  public VariableData getReferingData() {
    return referingData;
  }

  public ReferingData setReferingData(VariableData referingVariableData) {
    this.referingData = referingVariableData;
    return this;
  }

  @Override
  public String getPath() {
    String path = super.getPath();
    if(referingData != null) {

      try {
        String ref = URLEncoder.encode(referingData.getPath(), ENCODING);
        if(path.contains(QUERY)) {
          path += "&";
        } else {
          path += QUERY;
        }
        path += "ref=" + ref;
      } catch(UnsupportedEncodingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return path;
  }

}
