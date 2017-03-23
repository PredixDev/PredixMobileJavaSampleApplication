**PredixMobileJavaSampleApplication**
===========================


----------

Overview
----------

The Predix Mobile Java application is an example of how the [Predix Mobile Java SDK](https://github.com/PredixDev/PredixMobileJavaSDK) can be used to create a thin client for Mac, Windows, Linux or any other operating system that Java supports.  For an overview of Predix Mobile development please visit the [SDK repo](https://github.com/PredixDev/PredixMobileSDK) and [wiki](https://github.com/PredixDev/PredixMobileSDK/wiki).

This reference application provides the following:

- A container for a web application hosted with the Predix Mobile Service
- A way Authenticate with a UAA instance bound to a Predix Mobile Service
- A way to run the container application on platforms that support Java
- An installer example using a gradle task that creates an installer on the executed platform (Windows and Mac only)

Technology
----------

The example application is built using Gradle and JavaFX (part of Java 8).  These technologies allow for easy integration with the JavaSDK since they can take advantage of the latest Java syntax that allow for less code to complete similar tasks.

Gradle
----------

The reference application uses Gradle to manage its dependencies, execute builds and create binaries that can be used as installers for a given OS.  For more information about how Gradle works and how Java applications are managed with it please visit the official Gradle page (https://docs.gradle.org/2.13/userguide/userguide.html)

The sample application bundles the Gradle wrapper with the project.  This means all you need is the Java 8 JRE to run the project and you don't need to install Gradle.  However if you wish to use a new version of Gradle or changes some of the properties you will need to install Gradle for development.  Please visit the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) page for more information.

JavaFX 8
----------

The PredixMobileJava example application uses JavaFX as its primary display technology.  This allows the example application to work with the JavaFX WebView which is available on any platform that support Java 8.  JavaFX also allows for tight integration between JavaScript based application and Java.  For more on JavaFX please visit the Java documentation (http://docs.oracle.com/javase/8/javase-clienttechnologies.htm)

This mean you will be required to install Java 8 JDK to use this product.

Debugging
----------

### Using Chrome:
Unfortunatly, the JavaFX WebView does not support remote Chrome debugging out of the box.  In order to enable remote degging with Chrome you will have to include a couple of lines of code in any class that uses a *new* instance of the JavaFX WebView.  Here is an example:
```Java
public WindowController() {
    super();
    DevToolsDebugger.enableChromeRemoteDebugger(browser.getEngine(), 51742);
}
```
This will cause a message to be printed to the console that looks something like this:
> To debug open chrome and load url: chrome-devtools://devtools/bundled/inspector.html?ws=localhost:51742/

This will allow you to set break points and examine areas of your JavaScript running in the JavaFX WebView.

##### Limitations:

One limitation is console messages from you JavaScript will not be available in the Chrome console tab.  Instead you should enable a redirect that shows JavaScript console logs in the Java console.

### Redirecting the JavaScript console to the Java console:

To enable JavaScript console logs in the Java Console you would include the following debug code in a similar fashion to the way you enabled Chrome debugging.  Here is an example:

```Java
public WindowController() {
    super();
    DevToolsDebugger.enableChromeRemoteDebugger(browser.getEngine(), 51742);
    DevToolsDebugger.enableWebConsoleToJavaConsoleCapture(browser.getEngine());
}
```

### Using Firebug:

Another debugging option is to use Firebug.  To enable Firebug you can use the following code example:

```Java
public WindowController() {
    super();
    DevToolsDebugger.enableFireBugInWindowDebugger(browser.getEngine());
}
```

Setup and running the example
----------
### Setup:

To connect to the Predix Mobile Service you will need to point the example application to host URI for the Mobile Service.  You will also need to set the Web Application name and version.  To do this, locate the config.properties file in src/main/resources and update the pmapp_name, pmapp_version and server_hostname properties with the correct values.  Here is an example of the file:

```
pmapp_name=<your web app name>
pmapp_version=<your web app version>
server_hostname=<Predix Mobile Service URL>
logging_level=error
```

### Running from the command line:

Using gradle you can simple issue gradle commands from the command line to run the application.

#### Mac/Linux:
```
./gradlew run
```
#### Windows:
```
gradle.bat run
```

### Running from behind a proxy:

The example application includes a way to enable network communication from behind a proxy.

If you are behind a proxy you may want to consider passing the Java VM argument to enable system proxies when you run the application (NOTE: that running from Gradle enables this flag for you in the example).  

```
-Djava.net.useSystemProxies=true
```

If you have a more complex proxy setup you can configure the proxies by using the proxy.properties file in src/main/resources.

```
http.proxyHost=proxyHere
http.proxyPort=proxyPortHere
https.proxyHost=proxyHere
https.proxyPort=proxyPortHere
```

In some cases both the configuration file and the Java VM argument may be required.

Using an IDE
----------

The PredixMobileJava example is fully compatible with all IDEs that can work with Java and Gradle.  To build this application we used the Intellij Comunity edition (https://www.jetbrains.com/idea/download/).  It should work with Netbeans, Eclipes, etc...

If you use an IDE make sure you configure your JDK and do a Gradle sync before you start working with the code base.

If you run the main class using an IDE I would suggest adding the following VM properties:

```
-Djava.net.useSystemProxies=true -Xdock:icon=src/main/resources/icon.png -Xdock:name="Your application name"
```

These will allow the icon and app name to be seeing during development (there are some exceptions:  See the Things to be aware of section).

Example installer
----------

This example includes a very basic way to generate an Installer you could use to distribute your application for Mac, Windows, Linux, etc...  The included installer is just an example and is not required to be used if you want to distribute your application using another install builder like install4j (http://www.ej-technologies.com/products/install4j/overview.html) or lzPack (http://izpack.org/).  This is simply a quick and dirty example to show what is possible.

to use the example installer simply execute the jfxNative task from your IDE or using the Gradle command line.  Here is an example of executing the installer task using Gradle in the command line.
##### Mac:
```
./gradlew jfxNative
```
On Mac this will produce a DMG file that can be used to install the application into the users application folder.

# &#x2757; NOTE &#x2757;

This example does not deal with signing the MacOS DMG or the application.  For more on how to sign your DMG and APP file please visit Apple's documentation on signing (https://developer.apple.com/library/content/technotes/tn2206/_index.html).  To integrate signing into the jfxNative Gradle Plugin please visit (https://github.com/FibreFoX/javafx-gradle-plugin)

##### Windows:
```
gradle.bat jfxNative
```
On Windows this will produce an EXE that will install the application like any other Windows application install.

Things to be aware of
----------

### Mac

When running on a mac during development or from the Gradle command line you will notice a couple of things, first the main application menu and dock icon name will display "Java".  This is a JavaFX defect and is only present when doing development, if you generate an installable application using the example installer or another install builder "Java" will be replace by the application name.

### Icons

The application icons must be built using their native implementations.  For example the application icon for windows needs to be a proper .ico file, Mac an proper .incs and Linux a .png file.
