package org.obiba.onyx.spring.remoting;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerClientConfiguration;
import org.springframework.remoting.support.RemoteInvocation;

/**
 * A {@code RequestExecutor} that will not copy the {@code RemoteInvocation} to a byte array. Avoids duplicating objects
 * on the heap.
 * <p>
 * As per https://jira.springsource.org/browse/SPR-1223 Spring doesn't support streaming.
 * 
 * <p>
 * Note: currently, this class is NOT used. It was created to solve an issue but was not put into production (an
 * alternative fix was done). This class lacks testing. It should be thoroughly tested before putting in production.
 */
public class NoCopyRequestExecutor extends CommonsHttpInvokerRequestExecutor {

  private final ExecutorService executor = Executors.newCachedThreadPool();

  @Override
  protected ByteArrayOutputStream getByteArrayOutputStream(RemoteInvocation invocation) throws IOException {
    return new NoCopyByteArrayOutputStream(invocation);
  }

  @Override
  protected
      void
      setRequestBody(HttpInvokerClientConfiguration config, PostMethod postMethod, ByteArrayOutputStream baos) throws IOException {
    if(baos instanceof NoCopyByteArrayOutputStream) {
      NoCopyByteArrayOutputStream stream = (NoCopyByteArrayOutputStream) baos;
      postMethod.setRequestEntity(new InputStreamRequestEntity(stream.asInputStream(), stream.length(), getContentType()));
    } else {
      super.setRequestBody(config, postMethod, baos);
    }
  }

  private class NoCopyByteArrayOutputStream extends ByteArrayOutputStream {

    private final RemoteInvocation remoteInvocation;

    private NoCopyByteArrayOutputStream(RemoteInvocation ri) {
      this.remoteInvocation = ri;
    }

    public long length() throws IOException {
      // Copied from Guava ByteStreams.length()
      long count = 0;
      boolean threw = true;
      InputStream in = asInputStream();
      try {
        while(true) {
          // We skip only Integer.MAX_VALUE due to JDK overflow bugs.
          long amt = in.skip(Integer.MAX_VALUE);
          if(amt == 0) {
            if(in.read() == -1) {
              threw = false;
              return count;
            }
            count++;
          } else {
            count += amt;
          }
        }
      } finally {
        try {
          in.close();
        } catch(IOException e) {
          if(threw == false) throw e;
        }
      }

    }

    public InputStream asInputStream() throws IOException {
      Pipe pipe = Pipe.open();
      OutputStream os = Channels.newOutputStream(pipe.sink());
      final ObjectOutputStream oos = new ObjectOutputStream(os);
      Runnable writeObject = new Runnable() {

        @Override
        public void run() {
          try {
            try {
              oos.writeObject(remoteInvocation);
            } finally {
              oos.close();
            }
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
        }

      };
      executor.submit(writeObject);
      return Channels.newInputStream(pipe.source());
    }
  }
}
