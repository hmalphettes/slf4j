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

package org.slf4j.helpers.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.impl.PluggableSlf4jImplSupportBundleActivator;

/**
 * BundleActivator for Slf4j.
 * Looks for {@link PluggableSlf4jImplSupportBundleActivator} that is usually packaged
 * in a fragment and delegates the start and stop calls to it.
 * 
 * @author hmalphettes
 */
public class Slf4jBundleActivator  implements BundleActivator {
  
  /** Look for the actual BundleActivator directly in the fragment.
   * this will work even if we are decide to package the OSGi classes
   * directly with the slf4j-api. */
  private static final String PLUGGABLE_IMPL_SUPPORT_ACTIVATOR_CLASS =
    "org.slf4j.impl.PluggableSlf4jImplSupportBundleActivator";
  
  private BundleActivator pluggableSlf4jImplSupportBundleActivator;
  
  /**
   * Called when the slf4j bundle is activated
   * @param context The framework context.
   */
  public void start(final BundleContext context) throws Exception {
    try {
      pluggableSlf4jImplSupportBundleActivator = (BundleActivator)
        context.getBundle().loadClass(PLUGGABLE_IMPL_SUPPORT_ACTIVATOR_CLASS).newInstance();
    } catch (Throwable t) {
      //nevermind. maybe someone is using slf4j without the pluggable-osgi-support
      //for example, if slf4j-api was re-packaged and executed with
      //plugins that are fragment hosted by slf4j-api it makes sense
      //and the pluggable support would in fact break that. 
    }
  }
  
  /**
   * Called when the slf4j bundle is stopped
   * @param context The framework context.
   */
  public void stop(BundleContext context) throws Exception {
    if (pluggableSlf4jImplSupportBundleActivator != null) {
      pluggableSlf4jImplSupportBundleActivator.stop(context);
    }
  }

}
