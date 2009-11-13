package org.slf4j;

/**
 * Internal class. 
 * @author hmalphettes
 */
abstract class AbstractStaticLoggerBinder {

  /**
   * @return The actual ILoggerFactory
   */
  protected abstract ILoggerFactory internalGetILoggerFactory();
  
}
