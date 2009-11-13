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

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * This class tracks the installation of a bundle that provides an slf4j 
 * implementation: those bundles are identified by the fact that they depend on
 * this current bundle and that they export the package org.slf4j.impl
 * <p>
 * This bundle listener requires the {@link PackageAdmin} service to be available
 * it is constructed by the {@link PackageAdminServiceTracker} that makes sure
 * that indeed it is avalable.
 * <p>
 * 
 * @author Hugues Malphettes
 */
class StaticLoggerExtender implements BundleListener {
  
  private BundleContext context;
  
  public StaticLoggerExtender(BundleContext context) {
    this.context = context;
  }
  
  /**
   * Receives notification that a bundle has had a lifecycle change.
   * 
   * @param event The <code>BundleEvent</code>.
   */
  public void bundleChanged(BundleEvent event) {
    switch (event.getType()) {
    case BundleEvent.STARTING:
    case BundleEvent.STOPPING:
      
    }
  }
  
}
