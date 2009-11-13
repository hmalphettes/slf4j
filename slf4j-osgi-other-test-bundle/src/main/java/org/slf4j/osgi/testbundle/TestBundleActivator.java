package org.slf4j.osgi.testbundle;

import java.text.DateFormat;
import java.util.Date;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * On startup of the bundle a thread is started. It logs the time every 5 seconds.
 * 
 * @author Hugues Malphettes
 */
public class TestBundleActivator implements BundleActivator {

  private final Logger log = LoggerFactory.getLogger(TestBundleActivator.class);

  private Thread loggingThread;
  
  /**
   * 
   * 
   * @param bundleContext
   *          the framework context for the bundle
   * @throws Exception
   */
  public void start(BundleContext bundleContext) throws Exception {
    ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
    System.err.println(loggerFactory);
    if (loggerFactory instanceof LoggerContext) {
      ch.qos.logback.core.util.StatusPrinter.printIfErrorsOccured(
        (LoggerContext)loggerFactory);
    }
    loggingThread = new Thread() {
      public void run() {
        while (true) {
          System.err.println("Trying to log");
          log.info("the time is {}", DateFormat.getTimeInstance().format(
              new Date()));
          try {
            sleep(5000);
          } catch (InterruptedException e) {
            log.warn("interrupted", e);
          }
        }
      }
    };
    loggingThread.start();
  }

  /**
   * Implements <code>BundleActivator.stop()</code>. Prints a message and
   * removes itself from the bundle context as a service listener.
   * 
   * @param bundleContext
   *          the framework context for the bundle
   * @throws Exception
   */
  public void stop(BundleContext bundleContext) throws Exception {
    loggingThread.stop();
  }

}
