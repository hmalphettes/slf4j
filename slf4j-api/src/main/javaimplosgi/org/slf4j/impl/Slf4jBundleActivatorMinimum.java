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

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.helpers.Util;
import org.slf4j.impl.StaticLoggerBinder;

/**
 * Class called when the slf4j-api bundle is started and stoppped.
 * Please note that as this is in fact packaged in a fragment it is needed to
 * invoke it from the real BundleActivator of slf4j-api bundle.
 * <p>
 * This implementation is the shortest path to be remove the cyclic dependency
 * as reported in this bug: http://bugzilla.slf4j.org/show_bug.cgi?id=75
 * <br/>
 * It gets a hold of the {@link PackageAdmin}. Look for a resolved bundle
 * that provides an slf4j implementation. Starts that bundle.
 * Loads the {@link StaticLoggerBinder} from that bundle and set it as
 * the implementation slf4j should use.
 * </p>
 * <p>
 * If no implementations are found  
 * </p>
 * 
 * @author Hugues Malphettes
 */
public class Slf4jBundleActivatorMinimum implements BundleActivator {
  
  private ServiceListener packageAdminServiceTracker;
  private Bundle currentProviderOfSlf4jImpl;
  
  /**
   * Called when the slf4j bundle is activated
   * @param context The framework context.
   */
  public void start(final BundleContext context) throws Exception {
    //we should eventually set a static boolean somewhere so that the rest of
    //slf4j-api knows that it is executed in an OSGi framework.
    
    //track other bundles and fragments attached to this bundle that we should activate.
    PackageAdmin packageAdmin = getPackageAdmin(context);
    if (packageAdmin != null) {
      //this is the general case: the osgi framework does setup the PackageAdmin
      //early before slf4j-api is activated.
      setupSlf4jImpl(packageAdmin);
    } else {
      //not the case is general,
      //a service tracker will be called back when the PackageAdmin is ready.
      //in the mean time no slf4j-impl is ready.
      packageAdminServiceTracker = new ServiceListener() {
        public void serviceChanged(ServiceEvent event) {
          if (event.getType() == ServiceEvent.REGISTERED) {
            PackageAdmin packageAdmin = (PackageAdmin)context.getService(
                event.getServiceReference());
            setupSlf4jImpl(packageAdmin);
          }
        }
      };
    }
    BundleListener listener = new BundleListener() {
      public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
        case BundleEvent.STARTED:
          PackageAdmin packageAdmin = getPackageAdmin(event.getBundle().getBundleContext());
          ExportedPackage[] exp = packageAdmin.getExportedPackages(event.getBundle());
          if (exp != null) {
            for (int i = 0; i < exp.length; i++) {
              ExportedPackage ex = exp[i];
              if (ex.getName().equals("org.slf4j.impl")) {
                setupSlf4jImpl(event.getBundle());
                break;
              }
            }
          }
          break;
        case BundleEvent.STOPPING:
          if (event.getBundle() == currentProviderOfSlf4jImpl) {
            //uninstall
            System.err.println("uninstalling the current slf4jimpl provided by bundle "
                + currentProviderOfSlf4jImpl.getSymbolicName());
            uninstallSlf4jImpl();
          }
          break;
        }
      }
    };
    context.addBundleListener(listener);
  }
  
  /**
   * Called when the slf4j bundle is stopped
   * @param context The framework context.
   */
  public void stop(BundleContext context) throws Exception {
    if (packageAdminServiceTracker != null) {
      context.removeServiceListener(packageAdminServiceTracker);
    }
  }
  
  /**
   * Look for the {@link PackageAdmin} instance. It is accessed a singleton object
   * provided by the OSGi framework.
   * @param context
   * @return
   */
  private PackageAdmin getPackageAdmin(BundleContext context) {
    ServiceReference sr = context.getServiceReference(PackageAdmin.class.getName());
    return sr != null ? (PackageAdmin) context.getService(sr) : null;
  }
  
  /**
   * @param packageAdmin The packageAdmin
   * @return a {@link StaticLoggerBinder} if indeed an implementation of slf4j was discovered and loaded
   */
  private boolean setupSlf4jImpl(PackageAdmin packageAdmin) {
    Bundle slf4jImpl = discoverAndInstallSlf4jImpl(packageAdmin);
    return setupSlf4jImpl(slf4jImpl);
  }
  
  private void uninstallSlf4jImpl() {
    StaticLoggerBinder.setup(null);
    StaticMDCBinder.setup(null);
    StaticMarkerBinder.setup(null);
  }
  
  private boolean setupSlf4jImpl(Bundle slf4jImpl) {
    currentProviderOfSlf4jImpl = slf4jImpl;
    if (slf4jImpl == null) {
      return false;
    }
    if (slf4jImpl.getState() == Bundle.RESOLVED) {
      try {
        slf4jImpl.start();
      } catch (BundleException e) {
        Util.reportFailure("Unable to start the bundle " + slf4jImpl.getSymbolicName(), e);
        return false;
      }
    }
    try {
      Class loggerBinder = slf4jImpl.loadClass("org.slf4j.impl.StaticLoggerBinder");
      StaticLoggerBinder.setup(loggerBinder);
    } catch (ClassNotFoundException e) {
      Util.reportFailure("Unable to load org.slf4j.impl.StaticLoggerBinder from bundle " + slf4jImpl.getSymbolicName(), e);
      return false;
    }
    try {
      Class mdcBinder = slf4jImpl.loadClass("org.slf4j.impl.StaticMDCBinder");
      StaticMDCBinder.setup(mdcBinder);
    } catch (ClassNotFoundException e) {
      Util.reportFailure("Unable to load org.slf4j.impl.StaticMDCBinder from bundle " + slf4jImpl.getSymbolicName(), e);
      return false;
    }
    try {
      Class markerBinder = slf4jImpl.loadClass("org.slf4j.impl.StaticMarkerBinder");
      StaticMarkerBinder.setup(markerBinder);
    } catch (ClassNotFoundException e) {
      Util.reportFailure("Unable to load org.slf4j.impl.StaticMarkerBinder from bundle " + slf4jImpl.getSymbolicName(), e);
      return false;
    }
    return true;
  }
  
  /**
   * @param packageAdmin
   * @return The bundle that is an slf4jimpl or null.
   */
  private Bundle discoverAndInstallSlf4jImpl(PackageAdmin packageAdmin) {
    ExportedPackage[] implementations = packageAdmin.getExportedPackages("org.slf4j.impl");

    //no bundle aka no implementation installed at this time.
    //an implementation could be plugged in later.
    if (implementations == null) {
      System.err.println("No bundle currently installed exports the package org.slf4j.impl");
      return null;
    }

    //if there is a single bundle this is it:
    if (implementations.length == 1) {
      ExportedPackage ep = implementations[0];
      Bundle exportingbundle = ep.getExportingBundle();
      if (isThisBundle(exportingbundle)) {
        //the impl provided by this bundle is the dummy one.
        //it is not present at runtime.
        return null;
      }
      return exportingbundle;
    }
    
    //collect the bundles that provide slf4j and can be started.
    //err... right now just take the first one...
   // List slf4jImplProviders = new ArrayList();
    for (int i = 0; i < implementations.length; i++) {
      ExportedPackage ep = implementations[i];
      Bundle exportingbundle = ep.getExportingBundle();
      if (isThisBundle(exportingbundle)) {
        //the impl provided by this bundle is the dummy one.
        //it is not present at runtime.
        continue;
      }
      if (exportingbundle.getState() == Bundle.RESOLVED
            || exportingbundle.getState() == Bundle.ACTIVE
            || exportingbundle.getState() == Bundle.STARTING) {
        //this bundle is resolved: it means all its dependencies are available
        //and it can be started.
        return exportingbundle;
      }
    }
    
    return null;
    
  }
  
  /**
   * @param bundle
   * @return true when the passed bundle is in fact the current slf4j-api bundle.
   */
  private boolean isThisBundle(Bundle bundle) {
    return bundle == FrameworkUtil.getBundle(Slf4jBundleActivatorMinimum.class);
  }
   
}
