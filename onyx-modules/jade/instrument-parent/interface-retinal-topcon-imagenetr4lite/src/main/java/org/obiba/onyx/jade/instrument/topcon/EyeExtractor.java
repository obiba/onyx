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
import java.util.HashMap;
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

  public static final String EYE_PICT_VENDOR = "EYE_PICT_VENDOR";

  public static final String EYE_SIDE_VENDOR = "EYE_SIDE_VENDOR";

  public Map<String, Data> extractData(JdbcTemplate jdbc, String patientUUID) {
    log.info("Extracting Data");
    Map<String, Data> data = new HashMap<String, Data>();
    SqlRowSet mediaRowSet = jdbc.queryForRowSet(//
    "SELECT FileName, FileExt, StoragePathUid, CreateDate" + //
    " FROM dbo.Media" + //
    " WHERE PatientUid = ?" + //
    " AND EyeType = ? " + //
    " AND Status = 1 " + //
    " AND Display = 1 " + //
    " ORDER BY CreateDate ASC", new Object[] { patientUUID, getEyeTypeIntValue() });

    if(mediaRowSet.last()) {
      String storagePathUid = mediaRowSet.getString("StoragePathUid");
      String fileName = mediaRowSet.getString("FileName").trim();
      String extension = mediaRowSet.getString("FileExt").trim();
      String location = EyeExtractorQueryUtil.getLocation(jdbc, storagePathUid);

      byte[] imageByteArray = pathToByteArray(location, fileName, extension);
      data.put(EYE_PICT_VENDOR, new Data(DataType.DATA, imageByteArray));
      data.put(EYE_SIDE_VENDOR, new Data(DataType.TEXT, getSideName()));
    } else {
      log.warn("Missing Picture");
    }
    return data;
  }

  private byte[] pathToByteArray(String location, String fileName, String extension) {
    try {
      return FileCopyUtils.copyToByteArray(new File(location, fileName + extension));
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public abstract String getSideName();

  /**
   * Value of EyeType in MSSQL of IMAGEnet
   * @return
   */
  public abstract int getEyeTypeIntValue();

}
