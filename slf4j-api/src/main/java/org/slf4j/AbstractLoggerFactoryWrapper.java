package org.slf4j;

/**
 * Internal class. 
 * @author hmalphettes
 */
abstract class AbstractLoggerFactoryWrapper {

  /**
   * @return The actual ILoggerFactory
   */
  protected abstract ILoggerFactory internalGetILoggerFactory();
  
}
