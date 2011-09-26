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

import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 */
public class EyeExtractorQueryUtil {
  public static String getLocation(JdbcTemplate jdbc, String storagePathUid) {
    return jdbc.queryForObject("SELECT Location FROM dbo.StoragePaths WHERE StoragePathUid = ?", new Object[] { storagePathUid }, String.class);
  }
}
