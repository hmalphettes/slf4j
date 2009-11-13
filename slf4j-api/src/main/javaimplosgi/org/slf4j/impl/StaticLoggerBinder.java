/* 
 * Copyright (c) 2004-2007 QOS.ch
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.slf4j.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.osgi.framework.FrameworkUtil;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * This implementation of the StaticLoggerBinder is actually pluggable.
 * 
 * @author Hugues Malphettes
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {
  
  private static String actualClassStr;
  private static ILoggerFactory ACTUAL_LOGGER_FACTORY = new SubstituteLoggerFactory();
  
  /**
   * Declare the version of the SLF4J API this implementation is compiled against. 
   * The value of this field is read on the actual implementation.
   */
  public static String REQUESTED_API_VERSION = "1.5.9.RC1";
  

  /**
   * Use Introspection to set the ILoggerFactory and the REQUESTED_API_VERSION
   * @param actualBinderClass
   */
  public static void setup(Class actualBinderClass) {
    if (actualBinderClass == null) {
      actualClassStr = "org.slf4j.helpers.SubstituteLoggerFactory";
      ACTUAL_LOGGER_FACTORY = new SubstituteLoggerFactory();
      REQUESTED_API_VERSION = "1.5.9.RC1";
      return;
    }
    
    System.err.println("Setting up " + actualBinderClass.getName() + " from bundle "
        + FrameworkUtil.getBundle(actualBinderClass));
    
    try {
      //static method getSingleton
      Method getSingleton = actualBinderClass.getMethod("getSingleton", new Class[0]);
      LoggerFactoryBinder actualSingleton = (LoggerFactoryBinder) getSingleton.invoke(actualBinderClass, null);
      
      actualClassStr = (String) actualSingleton.getLoggerFactoryClassStr();
      ACTUAL_LOGGER_FACTORY = (ILoggerFactory) actualSingleton.getLoggerFactory();
      
      Field apiVersionField = actualBinderClass.getField("REQUESTED_API_VERSION");
      REQUESTED_API_VERSION = (String) apiVersionField.get(actualBinderClass);
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
 
  /**
   * The unique instance of this class.
   * 
   * @deprecated Please use the {@link #getSingleton()} method instead of
   *             accessing this field directly. In future versions, this field
   *             will become private.
   */
  public static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();
  
  /**
   * Return the singleton of this class.
   * 
   * @return the StaticLoggerBinder singleton
   */
  public static final StaticLoggerBinder getSingleton() {
    return SINGLETON;
  }
  
  public StaticLoggerBinder() {
  }

  public String getLoggerFactoryClassStr() {
    return actualClassStr;
  }

  public Logger getLogger(String name) {
    return getLoggerFactory().getLogger(name);
  }

  public ILoggerFactory getLoggerFactory() {
    return ACTUAL_LOGGER_FACTORY;
  }

}
