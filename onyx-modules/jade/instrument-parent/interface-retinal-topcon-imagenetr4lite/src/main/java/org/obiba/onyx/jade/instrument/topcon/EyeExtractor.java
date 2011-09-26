/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.topcon;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.FileCopyUtils;

public abstract class EyeExtractor {

  private static final Logger log = LoggerFactory.getLogger(EyeExtractor.class);

  public void extract(JdbcTemplate jdbc, Map<String, Data> data, String patientUUID) {
    log.info("Extracting Data");
    SqlRowSet mediasRowSet = jdbc.queryForRowSet("SELECT FileName, FileExt, StoragePathUid FROM dbo.Media WHERE PatientUid = ? AND EyeType = ?", new Object[] { patientUUID, getEyeType().intValue() });
    while(mediasRowSet.next()) {
      String location = jdbc.queryForObject("SELECT Location FROM dbo.StoragePaths WHERE StoragePathUid = ?", new Object[] { mediasRowSet.getString("StoragePathUid") }, String.class);
      String fileName = mediasRowSet.getString("FileName").trim();
      String extension = mediasRowSet.getString("FileExt").trim();
      byte[] pict = pathToByteArray(location, fileName, extension);
      log.info(getName());
      log.info(pict.toString());
      data.put(getName(), new Data(DataType.DATA, pict));
    }
  }

  private byte[] pathToByteArray(String location, String fileName, String extension) {
    try {
      return FileCopyUtils.copyToByteArray(new File(location, fileName + extension));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public abstract String getName();

  public abstract EyeType getEyeType();

  public enum EyeType {
    LEFT, RIGHT;
    public int intValue() {
      if(this == LEFT) return 1;
      else
        return 2;
    }
  }

}
