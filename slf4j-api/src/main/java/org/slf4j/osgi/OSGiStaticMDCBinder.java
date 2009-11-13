package org.slf4j.osgi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.osgi.framework.FrameworkUtil;
import org.slf4j.spi.MDCAdapter;


/**
 * Pluggable implementation of StaticMDCBinder
 *
 * @author Hugues Malphettes
 */
public class OSGiStaticMDCBinder implements MDCAdapter {

  /**
   * The unique instance of this class.
   */
  public static final OSGiStaticMDCBinder SINGLETON = new OSGiStaticMDCBinder();
  
//  private static Object ACTUAL_MDC_BINDER;
  
  private static MDCAdapter CURRENT_MDC_ADAPTER;
  private static String CURRENT_MDC_ADAPTER_CLASS_STR;

  public static void setup(Class actualMDCBinderClass) {
    if (actualMDCBinderClass == null) {
//      ACTUAL_MDC_BINDER = null;
      CURRENT_MDC_ADAPTER = null;
      CURRENT_MDC_ADAPTER_CLASS_STR = null;
      return;
    }
    
    System.err.println("Setting up " + actualMDCBinderClass.getName() + " from bundle "
        + FrameworkUtil.getBundle(actualMDCBinderClass));
    
    try {
      Field singleton = actualMDCBinderClass.getField("SINGLETON");
      Object actualSingleton = singleton.get(actualMDCBinderClass);
      
      //now invoke the various methods
      Method getLoggerFactoryClassStr = actualMDCBinderClass.getMethod("getMDCAdapterClassStr", new Class[0]);
      CURRENT_MDC_ADAPTER_CLASS_STR = (String) getLoggerFactoryClassStr.invoke(actualSingleton, null);
      
      Method getMDCA = actualMDCBinderClass.getMethod("getMDCA", new Class[0]);
      CURRENT_MDC_ADAPTER = (MDCAdapter) getMDCA.invoke(actualSingleton, null);


      
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
  
  public OSGiStaticMDCBinder() {
  }
  
  /**
   * Currently this method always returns an instance of 
   * {@link OSGiStaticMDCBinder}.
   */
  public MDCAdapter getMDCA() {
    return CURRENT_MDC_ADAPTER;
  }
  
  public String  getMDCAdapterClassStr() {
    return CURRENT_MDC_ADAPTER_CLASS_STR;
  }

  public void clear() {
    CURRENT_MDC_ADAPTER.clear();
  }

  public String get(String key) {
    return CURRENT_MDC_ADAPTER.get(key);
  }

  public Map getCopyOfContextMap() {
    return CURRENT_MDC_ADAPTER.getCopyOfContextMap();
  }

  public void put(String key, String val) {
    CURRENT_MDC_ADAPTER.put(key, val);
  }

  public void remove(String key) {
    CURRENT_MDC_ADAPTER.remove(key);
  }

  public void setContextMap(Map contextMap) {
    CURRENT_MDC_ADAPTER.setContextMap(contextMap);
  }
  
}
