
**PredixMobileJavaSampleApplication**
===========================

----------
Overview
----------

The Predix Mobile Java application is an example that shows how the Predix Mobile Java SDK can be used to create a native executing client for Mac, Windows, Linux or any other operating system (OS) that supports Java. For an overview of Predix Mobile software development kit see the [SDK repo](https://github.com/PredixDev/PredixMobileSDK) and [wiki](https://github.com/PredixDev/PredixMobileSDK/wiki)..

The Predix Mobile Java application provides the following:

*	A container for a web application hosted with the Predix Mobile Service
*	A process to Authenticate with a UAA instance bound to a Predix Mobile Service
*	A process to run the container application on platforms that support Java
*	An installer example using a Gradle task that creates a platfrom installer for the running platform (Windows and Mac only)

Technologies Used
-----------------

The PredixMobileJava example application is built using Gradle and JavaFX (part of Java 8). These technologies allow for easy integration with the JavaSDK because they can take advantage of the latest Java syntax that requires less code to complete similar tasks.

Gradle
---------

The PredixMobileJava application uses Gradle to manage its dependencies, execute builds, and create binaries that can be used as installers for a given OS. For more information about how Gradle works and how Java applications are managed with it, see the official Gradle documentation (https://docs.gradle.org/2.13/userguide/userguide.html)

The PredixMobileJava application bundles the Gradle wrapper with the project. This means, all you need is the Java 8 JRE to run the project without installing the Gradle. However, if you wish to use a new version of Gradle or change some of the properties, you will need to install Gradle for development. See the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) page for more information.


JavaFX 8
----------

The PredixMobileJava example application uses JavaFX as its primary display technology. This allows the example application to work with the JavaFX WebView which is available on any platform that support Java 8. JavaFX also allows for tight integration between JavaScript based application and Java. For more on JavaFX, see the Java documentation (http://docs.oracle.com/javase/8/javase-clienttechnologies.htm)
Note: You must install Java 8 JDK to use the PredixMobileJava example application.


Debugging
----------

You can use the following debugging options:

### Using Chrome
Unfortunately, the JavaFX WebView does not support remote Chrome debugging out of the box. 
To enable remote debugging with Chrome, include a couple of lines of code in any class that uses a new instance of the JavaFX WebView. 
Example:
```Java
public WindowController() {
    super();
    DevToolsDebugger.enableChromeRemoteDebugger(browser.getEngine(), 51742);
}
```
Including the debugging code shown in the above example may cause a message to print to the console similar to the following:
> To debug open chrome and load url: chrome-devtools://devtools/bundled/inspector.html?ws=localhost:51742/

This will allow you to set break points and examine areas of your JavaScript running in the JavaFX WebView.

##### Limitations

One limitation is that the console messages from your JavaScript will not be available in the Chrome console tab. We recommend that you enable a redirect that shows JavaScript console logs in the Java console. See the Redirecting the JavaScript console logs to the Java Console section for more information.

### Redirecting the JavaScript console to the Java console

You can enable the JavaScript console logs to appear in the Java console using the following debug code.

Example:
```Java
public WindowController() {
    super();
    DevToolsDebugger.enableChromeRemoteDebugger(browser.getEngine(), 51742);
    DevToolsDebugger.enableWebConsoleToJavaConsoleCapture(browser.getEngine());
}
```

### Using Firebug
Another debugging option is to enable Firebug. 
You can enable Firebug using the following code example.
Example:

```Java
public WindowController() {
    super();
    DevToolsDebugger.enableFireBugInWindowDebugger(browser.getEngine());
}
```
Setup and running the example
----------
### Setup

Before you run this example, you will need to connect to the Predix Mobile Service and define a name and version number for the Web Application.
*	To connect to Predix Mobile service,  point the example application to host the URI for the Mobile service.
*	To define a Web Application name and version number, locate the config.properties file in src/main/resources and update the pmapp_name, pmapp_version and server_hostname properties with the correct values. 
Example:


```
pmapp_name=<your web app name>
pmapp_version=<your web app version>
server_hostname=<Predix Mobile Service URL>
logging_level=error
```

### Running from the Command Line

Use Gradle to issue gradle commands from the command line to run the application.
Examples:

#### Mac/Linux:

```
./gradlew run
```
#### Windows:

```
gradle.bat run
```

### Running from Behind a Proxy

This example application allows you to enable network communication from behind a proxy.
If you are behind a proxy, pass the Java VM argument to enable system proxies.
Note that running from Gradle enables this flag for you in the example.

The example application includes a way to enable network communication from behind a proxy.

If you are behind a proxy you may want to consider passing the Java VM argument to enable system proxies when you run the application (NOTE: that running from Gradle enables this flag for you in the example).  
Example:

```
-Djava.net.useSystemProxies=true
```
If you have a more complex proxy setup, configure the proxies using the proxy.properties file in src/main/resources.
Example:

```
http.proxyHost=proxyHere
http.proxyPort=proxyPortHere
https.proxyHost=proxyHere
https.proxyPort=proxyPortHere
```
Note: In some cases both the configuration file and the Java VM argument may be required.

Using an IDE
----------

The PredixMobileJava example is fully compatible with all IDEs that work with Java and Gradle. This application is build using the Intellij Community edition (https://www.jetbrains.com/idea/download/). It should work with Netbeans, Eclipes, etc.,

If you use an IDE make sure you configure your JDK and perform a Gradle synchronization before you start working with the code base.
If you run the main class using an IDE, add the following VM properties:


```
-Djava.net.useSystemProxies=true -Xdock:icon=src/main/resources/icon.png -Xdock:name="Your application name"
```

Adding the VM properties allows the icon and app name to display during development 
Note: There are a few exceptions, see the “Things to Keep in Mind" section below.

Example Installer
----------
The PredixMobileJava example includes a very basic way to generate an Installer that you can use to distribute your application for Mac, Windows, and Linux operating systems. The installer is an example and is not required to be used if you want to distribute your application using other install builders like install4j (http://www.ej-technologies.com/products/install4j/overview.html) or lzPack (http://izpack.org/). 
The following is only an example to show how to use the Predix Java SDK application to create a desktop application.
You can execute the jfxNative task from your IDE or use the Gradle command line. The following examples show executing the installer using Gradle in the command line on Mac and Windows operating systems.

Example: 

##### Mac:
```
./gradlew jfxNative
```
On Mac, the above command will produce a DMG file that you can use to install the application in your application folder.

# &#x2757; NOTE &#x2757;

The PredixMobileJava example does not deal with signing the MacOS DMG or the application. For more information on how to sign your DMG and APP file please visit Apple's documentation on signing at (https://developer.apple.com/library/content/technotes/tn2206/_index.html). 
To integrate signing into the jfxNative Gradle Plugin see (https://github.com/FibreFoX/javafx-gradle-plugin)

Example: 

##### Windows:
```
gradle.bat jfxNative
```
On Windows, the above command creates an EXE file that will install the application like any other Windows application.

Things to Keep in Mind
-----------------------

### Mac

When developing on a Mac or from the Gradle command line you may notice that the main application menu and dock icon name is displayed as “java”. This is a known JavaFX defect and is only present during development. 
If you generate an installable application using the example installer or another install builder the main application menu and the dock icon name "Java" is replaced with the example installer or any other install builder name that you used. 

### Icons

The application icons must be built using their native implementations. For example, the application icon for Windows must be a proper .ico file, for Mac an .incs , and Linux a .png file.
