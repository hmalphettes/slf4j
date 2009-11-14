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

import org.osgi.framework.FrameworkUtil;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MarkerFactoryBinder;

/**
 * Just a facade for the StaticMarkerBinder is actually pluggable.
 * It is called back by the BundleListener when an implementation of slf4j is started
 * in the osgi container.
 * 
 * @author Hugues Malphettes
 */
public class StaticMarkerBinder implements MarkerFactoryBinder {

  /**
   * The unique instance of this class.
   */
  public static final StaticMarkerBinder SINGLETON = new StaticMarkerBinder();
  
  private static final MarkerFactoryBinder DefaultMarkerFactoryBinder = new MarkerFactoryBinder() {
    private BasicMarkerFactory basic = new BasicMarkerFactory();
    public IMarkerFactory getMarkerFactory() {
      return basic;
    }
    public String getMarkerFactoryClassStr() {
      return basic.toString();
    }
    
  };
  
  private static MarkerFactoryBinder ACTUAL_MARKER_FACTORY_BINDER = DefaultMarkerFactoryBinder;
  
  /**
   * Use Introspection to set the ILoggerFactory and the REQUESTED_API_VERSION
   * @param actualBinderClass
   */
  static void setup(Class actualMarkerFactoryClass) {
    if (actualMarkerFactoryClass == null) {
      ACTUAL_MARKER_FACTORY_BINDER = DefaultMarkerFactoryBinder;
      return;
    }
    
    System.err.println("Setting up " + actualMarkerFactoryClass.getName() + " from bundle "
        + FrameworkUtil.getBundle(actualMarkerFactoryClass));
    
    try {
      Field singleton = actualMarkerFactoryClass.getField("SINGLETON");
      ACTUAL_MARKER_FACTORY_BINDER = (MarkerFactoryBinder) singleton.get(actualMarkerFactoryClass);
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
  
  private StaticMarkerBinder() {
  }
  
  /**
   * Delegates to the actual implementation.
   */
  public IMarkerFactory getMarkerFactory() {
    return ACTUAL_MARKER_FACTORY_BINDER.getMarkerFactory();
  }
  
  /**
   * Delegates to the actual implementation.
   */
  public String getMarkerFactoryClassStr() {
    return ACTUAL_MARKER_FACTORY_BINDER.getMarkerFactoryClassStr();
  }
  
  
}
