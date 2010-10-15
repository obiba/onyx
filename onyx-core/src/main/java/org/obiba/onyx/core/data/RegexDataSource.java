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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

/**
 * Applies a regex to an {@link IDataSource} and returns the first matching group. Only works with variables
 * (IDataSource) of type TEXT.
 */
public class RegexDataSource extends AbstractDataSourceDataModifier {

  private final String regex;

  /**
   * 
   * @param dataSource The regex will be applied to data from this dataSource.
   * @param regex First matching item will be returned. e.g "^(...).*$" will match the first three characters on the
   * line.
   * @throws IllegalArgumentException if arguments are null.
   */
  public RegexDataSource(IDataSource dataSource, String regex) {
    super(dataSource);
    if(dataSource == null) throw new IllegalArgumentException("The dataSource must not be null.");
    if(regex == null) throw new IllegalArgumentException("The regex must not be null.");
    this.regex = regex;
  }

  protected RegexDataSource(IDataSource dataSource) {
    super(dataSource);
    if(dataSource == null) throw new IllegalArgumentException("The dataSource must not be null.");
    this.regex = "";
  }

  private static final long serialVersionUID = 4660715152687262120L;

  @Override
  protected Data modify(Data data, Participant participant) {
    if(data == null) return null;
    if(!data.getType().equals(DataType.TEXT)) throw new IllegalArgumentException("DataType [" + DataType.TEXT + "] expected, [" + data.getType() + "] received.");
    if(participant == null) throw new IllegalArgumentException("The participant value must not be null.");
    if(regex == null) throw new IllegalArgumentException("The regex must not be null.");
    Pattern regexPattern = Pattern.compile(regex);
    Matcher regexMatcher = regexPattern.matcher(data.getValueAsString());
    if(regexMatcher.matches() && regexMatcher.groupCount() > 0) {
      return new Data(data.getType(), regexMatcher.group(1));
    }
    return null;
  }

  @Override
  public String toString() {
    return "Regex[" + super.toString() + ", '" + regex + "']";
  }

}
