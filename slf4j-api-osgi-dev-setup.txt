= = = = = = = = =
slf4j-api-on-osgi
= = = = = = = = =

== goal ===

remove the cyclic dependency reported here: http://bugzilla.slf4j.org/show_bug.cgi?id=75
don't use fragments to do that.
prototype pluggable slf4j-impl when slf4j is running in osgi.

design (currently):
===================
- keep slf4j-api as is except for:
   - remove the import of org.slf4j.impl (aka remove the cyclic dependency)
   - add a bundle activator that osgi calls and that remains inactive otherwise.
- keep the current implementations of slf4j identical. this will work for all of them except if they are themselves fragments.
- new fragment hosted by slf4j-api provides a pluggable implementation of org.slf4j.impl
it listens to bundles being started and stopped.
when such a bundle provides an implementation of org.slf4j.impl it loads it and makes it the actual
currently in used implementation.

An alternative packaging would consists of generating an slf4j-api-with-osgi-impl.
It would make one less jar in the osgi apps but it would prevent implementations that
are fragments to work anymore and it would not wok at all if by mistake this was used in 
a non-OSGi environment.

An alternative architecture would be to make LoggerFactory, MarkerFactory and MDC support
pluggable binders. But that sounds like it would impact quite a bit the current design.

source code:
============
eclipse-PDE is the developement environment.
due to the fact the eclipse-PDE cannot compile a project if sources are missing we currently have setup 
the slf4j.impl as a separate source folder of slf4j-api and we test all of it without doing the fragment.
everything is bundles in slf4j-api including the osgi implementation.

the maven build generates the old slf4j-api without the org.slf4j.impl
and another artifact which is the fragment.

setup of the dev environment
===============================
1- checkout the osgi-binder branch of http://github.com/hmalphettes/slf4j
2- Rename the osgi.classpath and osgi.project files of slf4j-osgi-other-test-bundle and slf4j-api into .classpath and .project
3- Download eclipse-SDK-3.6-M3 (it should work with eclipse-3.5 too: this is all basic osgi and depends a single jar org.eclipse.osgi-v.jar. I recommend using a separate version of eclipse though.)
4- Open eclipse-SDK 
5- Import.../Existing Plugins and Fragments... / import logback-core and logback-classic as "Binary Projects"
The version I am testing with is the latest build of logback trunk with the patch attached here: http://jira.qos.ch/browse/LBCLASSIC-168 applied
6- Import as existing project slf4j-osgi-other-test-bundle and slf4j-api
7- The 2 imported projects must compile correctly.
8- Menu "Run/Run Configuration..../" select the entry called "slf4j-osgi-test". Choose the tab "Arguments".
In the "VM Arguments" text-area, change: -Dlogback.configurationFile=/home/hmalphettes/proj/osgi-experiments/slf4j-osgi/logback-dev.xml to path to a logback config file with INFO enabled by default.
9- Run or debug with this configuration.
The console of eclipse-PDE should appear and print this type of things:
osgi> Setting up org.slf4j.impl.StaticLoggerBinder from bundle ch.qos.logback.classic_0.9.18.SNAPSHOT [4]
Setting up org.slf4j.impl.StaticMDCBinder from bundle ch.qos.logback.classic_0.9.18.SNAPSHOT [4]
Setting up org.slf4j.impl.StaticMarkerBinder from bundle ch.qos.logback.classic_0.9.18.SNAPSHOT [4]
ch.qos.logback.classic.LoggerContext[default]
2009-11-13 13:32:47,439 INFO  [org.slf4j.osgi.testbundle.TestBundleActivator] [Thread-2] the time is 1:32:47 PM
Trying to log with ch.qos.logback.classic.LoggerContext[default]
2009-11-13 13:32:52,443 INFO  [org.slf4j.osgi.testbundle.TestBundleActivator] [Thread-2] the time is 1:32:52 PM
Trying to log with ch.qos.logback.classic.LoggerContext[default]
2009-11-13 13:32:57,444 INFO  [org.slf4j.osgi.testbundle.TestBundleActivator] [Thread-2] the time is 1:32:57 PM
Trying to log with ch.qos.logback.classic.LoggerContext[default]
2009-11-13 13:33:02,445 INFO  [org.slf4j.osgi.testbundle.TestBundleActivator] [Thread-2] the time is 1:33:02 PM
Trying to log with ch.qos.logback.classic.LoggerContext[default]



