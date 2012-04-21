package org.obiba.onyx.spring;

import java.util.concurrent.ThreadFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalThreadFactory implements ThreadFactory {

  private final TransactionTemplate txTemplate;

  @Autowired
  public TransactionalThreadFactory(PlatformTransactionManager txManager) {
    if(txManager == null) throw new IllegalArgumentException("txManager cannot be null");
    this.txTemplate = new TransactionTemplate(txManager);
  }

  @Override
  public Thread newThread(Runnable r) {
    return new TransactionalThread(r);
  }

  private class TransactionalThread extends Thread {

    private final Runnable runnable;

    public TransactionalThread(Runnable runnable) {
      this.runnable = runnable;
    }

    public void run() {
      txTemplate.execute(new TransactionCallbackWithoutResult() {
        @Override
        protected void doInTransactionWithoutResult(TransactionStatus status) {
          runnable.run();
        }
      });
    }
  }
}
