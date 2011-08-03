/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

  private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

  /**
   * Reads a stream as a string.
   * 
   * @param in The input stream
   * @return The string
   * @throws IOException
   */
  public static String readString(final InputStream in) throws IOException {
    return readString(new BufferedReader(new InputStreamReader(in)));
  }

  /**
   * Reads a string using a character encoding.
   * 
   * @param in The input
   * @param encoding The character encoding of the input data
   * @return The string
   * @throws IOException
   */
  public static String readString(final InputStream in, final CharSequence encoding) throws IOException {
    return readString(new BufferedReader(new InputStreamReader(in, encoding.toString())));
  }

  /**
   * Reads all input from a reader into a string.
   * 
   * @param in The input
   * @return The string
   * @throws IOException
   */
  public static String readString(final Reader in) throws IOException {
    final StringBuffer buffer = new StringBuffer(2048);
    int value;

    while((value = in.read()) != -1) {
      buffer.append((char) value);
    }

    return buffer.toString();
  }

  /**
   * Copy recursively one directory to another.
   * @param sourceDir
   * @param destDir
   * @throws IOException
   */
  public static void copyDirectory(File sourceDir, File destDir) throws IOException {
    if(sourceDir == null) throw new IllegalArgumentException("sourceDir cannot be null");
    if(destDir == null) throw new IllegalArgumentException("destDir cannot be null");

    if(!destDir.exists()) {
      if(destDir.mkdir() == false) {
        throw new IOException("Cannot create destination directory " + destDir.getAbsolutePath());
      }
    }

    if(sourceDir.exists()) {
      File[] children = sourceDir.listFiles();
      if(children != null) {
        for(File sourceChild : children) {
          String name = sourceChild.getName();
          File destChild = new File(destDir, name);
          if(sourceChild.isDirectory()) {
            copyDirectory(sourceChild, destChild);
          } else {
            copyFile(sourceChild, destChild);
          }
        }
      }
    }
  }

  /**
   * Copy a normal file to another.
   * @param source
   * @param dest destination directory or destination file
   * @throws IOException
   */
  public static void copyFile(File source, File dest) throws IOException {
    if(source == null) throw new IllegalArgumentException("source cannot be null");
    if(dest == null) throw new IllegalArgumentException("dest cannot be null");

    if(dest.isDirectory()) {
      dest = new File(dest, source.getName());
    }

    if(!dest.exists()) {
      if(dest.createNewFile() == false) {
        throw new IOException("Cannot create destination file " + dest.getAbsolutePath());
      }
    }

    InputStream in = null;
    OutputStream out = null;

    try {
      in = new FileInputStream(source);
      out = new FileOutputStream(dest);

      byte[] buf = new byte[4096];
      int len;

      while((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } finally {
      try {
        if(in != null) in.close();
      } catch(IOException e) {
        // ignore
      }
      try {
        if(out != null) out.close();
      } catch(IOException e) {
        // ignore
      }
    }
  }

  /**
   * Move file to a directory or a new file.
   * @param source
   * @param dest
   * @throws IOException
   */
  public static void moveFile(File source, File dest) throws IOException {
    if(dest.isDirectory()) {
      dest = new File(dest, source.getName());
    }

    if(!source.renameTo(dest)) {
      copyFile(source, dest);
      if(source.delete() == false) {
        log.warn("Cannot delete source file after copy {}", source.getAbsolutePath());
      }
    }
  }

  /**
   * Delete the normal file or delete recursively the directory.
   * @param resource
   * @return
   * @throws IOException
   */
  public static boolean delete(File resource) throws IOException {

    if(resource.isDirectory()) {
      File[] childFiles = resource.listFiles();
      for(File child : childFiles) {
        delete(child);
      }
    }

    return resource.delete();
  }

}
