/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.obiba.core.util.HexUtil;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashedDataSource implements IDataSource {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(HashedDataSource.class);

  private IDataSource dataSource;

  private Integer hashedStringMaxLength = null;

  private String hashedStringFilter = null;

  public HashedDataSource(IDataSource dataSourceToHash) {
    super();
    this.dataSource = dataSourceToHash;
  }

  public Data getData(Participant participant) {
    Data data = dataSource.getData(participant);
    String hashedString = hash(data.getValueAsString().getBytes());

    if(hashedStringFilter != null) {
      hashedString = hashedString.replaceAll(hashedStringFilter, "");
      log.debug("Filtered hashed string = {}", hashedString);
    }

    if(hashedStringMaxLength != null && hashedStringMaxLength < hashedString.length()) {
      hashedString = hashedString.substring(0, hashedStringMaxLength);
      log.debug("Reduced length hashed string = {}", hashedString);
    }

    log.debug("Hashed string = {}", hashedString);

    return DataBuilder.buildText(hashedString);
  }

  public static String hash(byte[] dataBytes) {
    if(dataBytes == null) return "";

    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-512");
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    md.update(dataBytes);
    byte[] digest = md.digest();

    return HexUtil.bytesToHex(digest);
  }

  public String getUnit() {
    return null;
  }

  public IDataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(IDataSource dataSource) {
    this.dataSource = dataSource;
  }

  public String getHashedStringFilter() {
    return hashedStringFilter;
  }

  public void setHashedStringFilter(String hashedStringFilter) {
    this.hashedStringFilter = hashedStringFilter;
  }

  public Integer getHashedStringMaxLength() {
    return hashedStringMaxLength;
  }

  public void setHashedStringMaxLength(Integer hashedStringMaxLength) {
    this.hashedStringMaxLength = hashedStringMaxLength;
  }

}
