package org.obiba.onyx.crypt;

public class KeyStoreRuntimeException extends RuntimeException {

  private static final long serialVersionUID = 4601375274475054624L;

  public KeyStoreRuntimeException() {
    super();
  }

  public KeyStoreRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public KeyStoreRuntimeException(String message) {
    super(message);
  }

  public KeyStoreRuntimeException(Throwable cause) {
    super(cause);
  }

}
