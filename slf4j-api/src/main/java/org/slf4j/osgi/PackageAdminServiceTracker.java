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

package org.slf4j.osgi;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;
/**
 * <p>
 * Get a hold of the {@link PackageAdmin} to be able to find out what bundle
 * provide an implementation of slf4j api.
 * <p>
 * <p>
 * Two phases:
 * <ul>
 * <li>At initialization of slf4j-api, 2 choices:
 *   <ul>
 *     <li>eagerly look for resolved bundles that implement slf4j and install
 *         one of them.</li>
 *     <li>don't install any of them. presumably the user has configured the
 *     framework to auto-start one of those bundles.</li>
 * <li>After initialization: when a bundle is activated and provides an slf4j
 *     implementation.</li>
 * </ul>
 * </p>
 * 
 * When the PackageAdmin service is activated we can look for the bundles
 * that export a particular package even before those bundles are activated.
 * <p>
 * This is useful if we want to support the "old" behavior where it was not necessary
 * to start an slf4j implementation. Just depending on the slf4j-api bundle was enough.
 * </p>
 * @author hmalphettes
 */
class PackageAdminServiceTracker implements ServiceListener {
  
  /** system property to eagerly discover and install an slf4j implementation.  */
  public static String EAGERLY_INSTALL_DISCOVERED_SLF4J_IMPL = "slf4j.osgi.eager.install";
  /** system property to eagerly discover and install an slf4j implementation. */
  public static String DYNAMIC_INSTALL_SLF4J_IMPL = "slf4j.osgi.dynamic.install";
  
  private boolean trackerWasCalled = false;
      
  public PackageAdminServiceTracker() {
    BundleContext context = FrameworkUtil
      .getBundle(PackageAdminServiceTracker.class).getBundleContext();
    if (!setup(context)) {
      //register a service tracker that will be called back later.
      try {
        context.addServiceListener(this, "(objectclass="
            + PackageAdmin.class.getName() + ")");
      } catch (InvalidSyntaxException e) {
        e.printStackTrace(); // won't happen
      }
    } else {
      //already did the early wiring.
    }
  }
      
  /**
   * @return true if the PackageAdmin was found right away.
   * if not found a service tracker needs to be registered to be called back
   * once the PackageAdmin is setup. In general it is found right away
   * as it is created by the osgi framework early on.
   */
  private boolean setup(BundleContext context) {
    ServiceReference sr = context.getServiceReference(PackageAdmin.class.getName());
    trackerWasCalled = sr != null;
    if (trackerWasCalled)
      lookupSlf4JImplementation(context, sr);
    return trackerWasCalled;
  }
      
  /**
   * Invokes the optional BundleActivator in each fragment. By convention the
   * bundle activator for a fragment must be in the package that is defined by
   * the symbolic name of the fragment and the name of the class must be
   * 'FragmentActivator'.
   * 
   * @param event
   *          The <code>ServiceEvent</code> object.
   */
  public void serviceChanged(ServiceEvent event) {
    if (event.getType() == ServiceEvent.REGISTERED) {
      BundleContext context = FrameworkUtil
        .getBundle(PackageAdminServiceTracker.class).getBundleContext();
      lookupSlf4JImplementation(context, event.getServiceReference());
    }
  }
  
  /**
   * @param sr The service reference to the PackageAdmin service
   */
  private void lookupSlf4JImplementation(BundleContext context, ServiceReference sr) {
    PackageAdmin packageAdmin = (PackageAdmin)context.getService(sr);
    ExportedPackage[] implementations = packageAdmin.getExportedPackages("org.slf4j.impl");

    //no bundle aka no implementation installed at this time.
    //an implementation could be plugged in later.
    if (implementations == null) {
      System.err.println("No bundle currently installed exports the package org.slf4j.impl");
      return;
    }

    //if there is a single bundle this is it:
    if (implementations.length == 1) {
      ExportedPackage ep = implementations[0];
      Bundle b = ep.getExportingBundle();
      if (b.getState() == Bundle.RESOLVED) {
        try {
          b.start();
        } catch (BundleException e) {
          e.printStackTrace();
        }
      }
      return;
    }
    
    //look for the bundles that are set to auto-start
    for (int i = 0; i < implementations.length; i++) {
      ExportedPackage ep = implementations[i];
      Bundle exportingbundle = ep.getExportingBundle();
      
    }
  }
  
  /**
   * @param bundle
   * @return true if the bundle is set to start
   */
  private boolean isBundleAutoStart(Bundle bundle) {
    return false;
  }
  
}
