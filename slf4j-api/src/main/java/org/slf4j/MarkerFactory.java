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

package org.slf4j;

import java.lang.reflect.Field;

import org.slf4j.helpers.Util;
import org.slf4j.impl.StaticMarkerBinder;
import org.slf4j.spi.MarkerFactoryBinder;

/**
 * MarkerFactory is a utility class producing {@link Marker} instances as
 * appropriate for the logging system currently in use.
 * 
 * <p>
 * This class is essentially implemented as a wrapper around an
 * {@link IMarkerFactory} instance bound at compile time.
 * 
 * <p>
 * Please note that all methods in this class are static.
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
public class MarkerFactory {

  static MarkerFactoryBinder STATIC_MARKER_FACTORY_BINDER;
  
  private MarkerFactory() {
  }

  static {
    try {
      //see if we are inside OSGi.
      Class.forName("org.osgi.framework.Bundle");
      //we are in OSGi, plug the 'other' StaticLoggerBinder
      Class oSGiStaticMarkerBinderClass = MarkerFactory.class.getClassLoader().loadClass(
          "org.slf4j.osgi.OSGiStaticMarkerBinder");
      Field singleton = oSGiStaticMarkerBinderClass.getField("SINGLETON");
      STATIC_MARKER_FACTORY_BINDER =
          (MarkerFactoryBinder) singleton.get(oSGiStaticMarkerBinderClass);
    } catch (Throwable t) {
      //debugging osgi:
      t.printStackTrace();

      //use the default java one:
      try {
        //Should we use reflection to make sure this class will not depend on
        //org.slf4j.impl at runtime ?
        STATIC_MARKER_FACTORY_BINDER = StaticMarkerBinder.SINGLETON;
//        STATIC_MARKER_FACTORY_BINDER.getMarkerFactory();
      } catch (Exception e) {
        // we should never get here
        Util.reportFailure("Could not instantiate instance of class ["
            + StaticMarkerBinder.SINGLETON.getMarkerFactoryClassStr() + "]", e);
      }
    }
  }

  /**
   * Return a Marker instance as specified by the name parameter using the
   * previously bound {@link IMarkerFactory}instance.
   * 
   * @param name
   *          The name of the {@link Marker} object to return.
   * @return marker
   */
  public static Marker getMarker(String name) {
    return getIMarkerFactory().getMarker(name);
  }

  /**
   * Create a marker which is detached (even at birth) from the MarkerFactory.
   *
   * @return a dangling marker
   * @since 1.5.1
   */
  public static Marker getDetachedMarker(String name) {
    return getIMarkerFactory().getDetachedMarker(name);
  }
  
  /**
   * Return the {@link IMarkerFactory}instance in use.
   * 
   * <p>The IMarkerFactory instance is usually bound with this class at 
   * compile time.
   * 
   * @return the IMarkerFactory instance in use
   */
  public static IMarkerFactory getIMarkerFactory() {
    return STATIC_MARKER_FACTORY_BINDER.getMarkerFactory();
  }
}