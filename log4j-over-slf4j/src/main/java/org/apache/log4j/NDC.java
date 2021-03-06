/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.log4j;

import java.util.Stack;

/**
 * Delegate all calls to slf4j's NDC.
 * The API is exactly identical.
 * @see org.slf4j.NDC
 */
public class NDC {

    /**
     * Clear any nested diagnostic information if any. This method is useful in
     * cases where the same thread can be potentially used over and over in
     * different unrelated contexts.
     * 
     * <p>
     * This method is equivalent to calling the {@link #setMaxDepth} method with
     * a zero <code>maxDepth</code> argument.
     * 
     * @since 0.8.4c
     */
    public static void clear() {
        NDC.clear();
    }

    /**
     * Clone the diagnostic context for the current thread.
     * 
     * <p>
     * Internally a diagnostic context is represented as a stack. A given thread
     * can supply the stack (i.e. diagnostic context) to a child thread so that
     * the child can inherit the parent thread's diagnostic context.
     * 
     * <p>
     * The child thread uses the {@link #inherit inherit} method to inherit the
     * parent's diagnostic context.
     * 
     * @return Stack A clone of the current thread's diagnostic context.
     */
    public static Stack cloneStack() {
        return NDC.cloneStack();
    }

    /**
     * Inherit the diagnostic context of another thread.
     * 
     * <p>
     * The parent thread can obtain a reference to its diagnostic context using
     * the {@link #cloneStack} method. It should communicate this information to
     * its child so that it may inherit the parent's diagnostic context.
     * 
     * <p>
     * The parent's diagnostic context is cloned before being inherited. In
     * other words, once inherited, the two diagnostic contexts can be managed
     * independently.
     * 
     * <p>
     * In java, a child thread cannot obtain a reference to its parent, unless
     * it is directly handed the reference. Consequently, there is no
     * client-transparent way of inheriting diagnostic contexts. Do you know any
     * solution to this problem?
     * 
     * @param stack
     *            The diagnostic context of the parent thread.
     */
    public static void inherit(Stack stack) {
        NDC.inherit(stack);
    }

    /**
     * <font color="#FF4040"><b>Never use this method directly, use the
     * {@link org.apache.log4j.spi.LoggingEvent#getNDC} method
     * instead</b></font>.
     */
    static public String get() {
        return NDC.get();
    }

    /**
     * Get the current nesting depth of this diagnostic context.
     * 
     * @see #setMaxDepth
     * @since 0.7.5
     */
    public static int getDepth() {
        return NDC.getDepth();
    }

    /**
     * Clients should call this method before leaving a diagnostic context.
     * 
     * <p>
     * The returned value is the value that was pushed last. If no context is
     * available, then the empty string "" is returned.
     * 
     * @return String The innermost diagnostic context.
     */
    public static String pop() {
        return NDC.pop();
    }

    /**
     * Looks at the last diagnostic context at the top of this NDC without
     * removing it.
     * 
     * <p>
     * The returned value is the value that was pushed last. If no context is
     * available, then the empty string "" is returned.
     * 
     * @return String The innermost diagnostic context.
     */
    public static String peek() {
        return NDC.peek();
    }

    /**
     * Push new diagnostic context information for the current thread.
     * 
     * <p>
     * The contents of the <code>message</code> parameter is determined solely
     * by the client.
     * 
     * @param message
     *            The new diagnostic context information.
     */
    public static void push(String message) {
        NDC.push(message);
    }

    /**
     * Remove the diagnostic context for this thread.
     * 
     * <p>
     * Each thread that created a diagnostic context by calling {@link #push}
     * should call this method before exiting. Otherwise, the memory used by the
     * <b>thread</b> cannot be reclaimed by the VM.
     * 
     * <p>
     * As this is such an important problem in heavy duty systems and because it
     * is difficult to always guarantee that the remove method is called before
     * exiting a thread, this method has been augmented to lazily remove
     * references to dead threads. In practice, this means that you can be a
     * little sloppy and occasionally forget to call {@link #remove} before
     * exiting a thread. However, you must call <code>remove</code> sometime. If
     * you never call it, then your application is sure to run out of memory.
     */
    static public void remove() {
        NDC.remove();
    }

    /**
     * Set maximum depth of this diagnostic context. If the current depth is
     * smaller or equal to <code>maxDepth</code>, then no action is taken.
     * 
     * <p>
     * This method is a convenient alternative to multiple {@link #pop} calls.
     * Moreover, it is often the case that at the end of complex call sequences,
     * the depth of the NDC is unpredictable. The <code>setMaxDepth</code>
     * method circumvents this problem.
     * 
     * <p>
     * For example, the combination
     * 
     * <pre>
     * void foo() {
     *       &nbsp;  int depth = NDC.getDepth();
     * 
     *       &nbsp;  ... complex sequence of calls
     * 
     *       &nbsp;  NDC.setMaxDepth(depth);
     *       }
     * </pre>
     * 
     * ensures that between the entry and exit of foo the depth of the
     * diagnostic stack is conserved.
     * 
     * @see #getDepth
     * @since 0.7.5
     */
    static public void setMaxDepth(int maxDepth) {
        NDC.setMaxDepth(maxDepth);
    }

}
