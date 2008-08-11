package org.obiba.onyx.jade.instrument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public final class FileHelper {
  
  public static void copy( File sourceFile, File targetFile ) throws Exception {
    FileChannel sourceChnl = null;
    FileChannel targetChnl = null; 
   
    try {
      sourceChnl = new FileInputStream( sourceFile ).getChannel();
      targetChnl = new FileOutputStream( targetFile ).getChannel();
      sourceChnl.transferTo( 0, sourceChnl.size(), targetChnl );
    } catch ( IOException ioEx ) {
      throw new Exception ("Problem encountered while copying " + sourceFile.getName() + ": " + ioEx.toString());
    } finally {
      try {
        sourceChnl.close();
        targetChnl.close();
      } catch ( Exception ex ) {
        ex.printStackTrace();
      }
    }
  }
}