logback-steno
=============

<a href="https://raw.githubusercontent.com/ArpNetworking/logback-steno/master/LICENSE">
    <img src="https://img.shields.io/hexpm/l/plug.svg"
         alt="License: Apache 2">
</a>
<a href="https://travis-ci.org/ArpNetworking/logback-steno/">
    <img src="https://travis-ci.org/ArpNetworking/logback-steno.png"
         alt="Travis Build">
</a>
<a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.arpnetworking.logback%22%20a%3A%22logback-steno%22">
    <img src="https://img.shields.io/maven-central/v/com.arpnetworking.logback/logback-steno.svg"
         alt="Maven Artifact">
</a>
<a href="http://www.javadoc.io/doc/com.arpnetworking.logback/logback-steno">
    <img src="http://www.javadoc.io/badge/com.arpnetworking.logback/logback-steno.svg"
         alt="Javadocs">
</a>

Logback encoders for handling Steno and other formats.  Steno is a JSON based container that standardizes the way log
messages are encoded.  In particular it defines a standard set of meta-data that may be used for routing, indexing and
other automated log processing operations.  This package includes an encoder for the popular Logback logging
implementation which automatically encodes log messages in a Steno compatible format.

Dependency
----------

Determine the latest version of the library in [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.arpnetworking.logback%22%20a%3A%22logback-steno%22).

#### Maven

Add a dependency to your pom:

```xml
<dependency>
    <groupId>com.arpnetworking.logback</groupId>
    <artifactId>logback-steno</artifactId>
    <version>VERSION</version>
</dependency>
```

The Maven Central repository is included by default.

#### Gradle

Add a dependency to your build.gradle:

    compile group: 'com.arpnetworking.logback', name: 'logback-steno', version: 'VERSION'

Add the Maven Central Repository into your *build.gradle*:

```groovy
repositories {
    mavenCentral()
}
```

#### SBT

Add a dependency to your project/Build.scala:

```scala
val appDependencies = Seq(
    "com.arpnetworking.logback" % "logback-steno" % "VERSION"
)
```

The Maven Central repository is included by default.

Class Preparation
-----------------

Not all classes are designed for serialization. To ensure the encoder is able to successfully generate a JSON
representation of each value it is provided the following rules will be applied.

Instances of the following are logged as-is using Jackson serialization:
* `java.lang.Number`
* `java.lang.Boolean`
* `java.lang.String`
* `java.util.Map` where the `key` is `java.lang.String`
* `java.util.List`

Instances of classes declaring a method to create a serializable representation with @LogValue or @JsonValue or
using a specific serializer with @JsonSerialize are logged in that representation. Also, instances mapping to a custom
serializer via a registered Jackson module are also logged in the representation provided by that serializer.

Next, if the type is annotated with @Loggable it is also serialized as-is by Jackson.

Finally, all other types are serialized as a the instance identifier and class name. It is possible to override this
behavior and send all values to Jackson for natural serialization by setting the __safe__ property of the encoder to false.
It is recommended that you do __not__ do this and instead provide serialization by one of the three means described above.

These rules are applied recursively to any objects encountered during serialization.


Encoder Configuration
---------------------

This package supplies two encoders for Logback.  The first is the StenoEncoder which encodes your
log messages in JSON.  Although fully compatible with standard SLF4J logging directives, this encoder
supports a provided set of Markers to encode structured data with your log message.  Logging supporting
data in this way makes it easier to parse and analyze especially if common structures are shared across
the organization (e.g. connection established, http request received, http response sent, etc.).

The second encoder is more human friendly and encodes each key-value pair of supporting data using the
data instance's toString method.  This is intended for development.  For more information about Logback
configuration please see: http://logback.qos.ch/manual/configuration.html

__IMPORTANT__: The structured data markers injected by this library are __not__ supported by any other
Logback encoders. Therefore, it is required that you _either_ specify the StenoEncoder or the
KeyValueEncoder in your Logback configuration when using this library.

#### StenoEncoder

Example appender configuration in XML:

```xml
<configuration>
    <appender name="STENO_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <File>log/application.steno.log</File>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>log/application-%d{yyyy-MM-dd}.steno.log.gz</fileNamePattern>
            <maxHistory>10</maxHistory>
        </rollingPolicy>
        <encoder class="com.arpnetworking.logback.StenoEncoder" >
            <!-- Steno Encoder Options Go Here -->
        </encoder>
    </appender>
    <root>
        <level value="INFO"/>
        <appender-ref ref="STENO_FILE"/>
    </root>
</configuration>
```

The StenoEncoder encoder supports several options:

* LogEventName - Set the default event name. The default is "log".
* RedactEnabled - Redact fields with @LogRedact annotation. The default is true.
* RedactNull - Redact fields with @LogRedact even if the value is null. The default is true.
* InjectContextProcess - Add the process identifier to the context block. The default is true.
* InjectContextHost - Add the host name to the context block. The default is true.
* InjectContextThread - Add the thread name to the context block. The default is true.
* InjectContextLogger - Add the logger name to the context block. The default is false. (1)
* InjectContextClass - Add the calling class name to the context block. The default is false. (2)
* InjectContextFile - Add the calling file name to the context block. The default is false.  (2)
* InjectContextMethod - Add the calling method name to the context block. The default is false. (2)
* InjectContextLine - Add the calling line to the context block. The default is false. (2)
* InjectContextMdc - Add the specified key pairs from MDC into the context. The default is none. Injected MDC keys
override any context keys pairs injected by the Steno encoder. (1)
* InjectBeanIdentifier - Add the "_id" and "_class" attributes to all objects serialized with Jackson's BeanSerializer. In safe mode these are objects annotated with @Loggable. Also any classes with @LogValue or @JsonValue returning a LogValueValueMap with a reference to the instance being logged receive these identifying attributes. The default is false.
* CompressLoggerName - Compress the dotted logger name replacing each segment except the last with only its first letter. The default is false.
* JacksonModule - Add the specified Jackson module instance to the ObjectMapper configuration.
* Safe - Setting to false causes all types to be deferred to Jackson for serialization. Otherwise, only types that are determined to be safe are serialized as-is; see Class Preparation for details. The default is true.

_Note 1_: Injecting additional key-value pairs into context is not strictly compliant with the current definition of Steno.<br>
_Note 2_: Injecting class, file, method or line will incur a significant performance penalty.

Optionally, you may additionally wrap the FileAppender in an AsyncAppender:

```xml
<configuration>
    <appender name="STENO_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <File>log/application.steno.log</File>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>log/application-%d{yyyy-MM-dd}.steno.log.gz</fileNamePattern>
            <maxHistory>10</maxHistory>
        </rollingPolicy>
        <encoder class="com.arpnetworking.logback.StenoEncoder">
            <!-- Steno Encoder Options Go Here -->
        </encoder>
    </appender>
    <appender name="STENO_ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="STENO_FILE"/>
        <queueSize>1024000</queueSize>
        <discardingThreshold>0</discardingThreshold>
    </appender>
    <root>
        <level value="INFO"/>
        <appender-ref ref="STENO_ASYNC"/>
    </root>
</configuration>
```

Example appender configuration in Java:

```java
final LoggerContext loggerContext;
// Obtain the existing logger context
loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
// -OR- Create a new logger context
loggerContext = new LoggerContext();

final StenoEncoder encoder = new StenoEncoder();
encoder.setContext(loggerContext);
encoder.start();

final FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
fileAppender.setFile("log.txt");
fileAppender.setEncoder(encoder);
fileAppender.setContext(loggerContext);
fileAppender.start();

final Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
rootLogger.setLevel(Level.INFO);
rootLogger.addAppender(fileAppender);
```

For more information about Logback configuration please see: http://logback.qos.ch/manual/configuration.html

#### KeyValueEncoder

Example appender configuration in XML:

```xml
<configuration>
    <appender name="STENO_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <File>log/application.steno.log</File>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>log/application-%d{yyyy-MM-dd}.steno.log.gz</fileNamePattern>
            <maxHistory>10</maxHistory>
        </rollingPolicy>
        <encoder class="com.arpnetworking.logback.KeyValueEncoder">
            <layout class="ch.qos.logback.classic.PatternLayout">
                <pattern>%date %t [%level] %logger : %message %ex%n</pattern>
            </layout>
        </encoder>
    </appender>
    <root>
        <level value="INFO"/>
        <appender-ref ref="STENO_FILE"/>
    </root>
</configuration>
```

The KeyValueEncoder encoder supports a smaller set of options:

* LogEventName - Set the default event name. The default is "log".

Configuring the encoder from Java is similar to the StenoEncoder example above.

Jackson Configuration
---------------------

The Jackson ObjectMapper used to serialize data and context values can be customized by registering additional Jackson
modules.  In XML configuration such configuration looks like this:

```xml
<encoder class="com.arpnetworking.logback.StenoEncoder" >
    <jacksonModule class="com.fasterxml.jackson.datatype.joda.JodaModule" />
</encoder>
```

In Java the same configuration may be achieved like this:

```java
encoder.addJacksonModule(new com.fasterxml.jackson.datatype.joda.JodaModule());
```

Note, the module must be added __before__ you invoke start on the encoder.  Common modules that you may want to register
include:

* com.fasterxml.jackson.datatype.joda.JodaModule ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.fasterxml.jackson.datatype%22%20a%3A%22jackson-datatype-joda%22))
* com.fasterxml.jackson.datatype.guava.GuavaModule ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.fasterxml.jackson.datatype%22%20a%3A%22jackson-datatype-guava%22))
* com.fasterxml.jackson.datatype.jdk7.Jdk7Module ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.fasterxml.jackson.datatype%22%20a%3A%22jackson-datatype-jdk7%22))
* com.fasterxml.jackson.datatype.jdk8.Jdk8Module ([Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.fasterxml.jackson.datatype%22%20a%3A%22jackson-datatype-jdk8%22))

Steno Logger
------------

Although you may use the StenoEncoder through the standard SLF4J Logger the library includes its own classes to improve
type-safety, reduce boilerplate and improve readability.  Under the covers these implementations invoke the same SLF4J
Logger that you would normally use so you remain completely compatible with the SLF4J standard.

### Steno Logger Example

Code:

```java
import com.arpnetworking.logback.annotations.Loggable;
import com.arpnetworking.steno.Logger;
import com.arpnetworking.steno.LoggerFactory;
import com.google.common.collect.ImmutableMap;

public class MyClass {
  private static final Logger LOGGER = LoggerFactory.getLogger(MyClass.class);

  public void foo() {
    final Widget widget = new Widget("MyWidget");

    // Backwards compatible with common SLF4J methods:
    LOGGER.info("foo was called");

    // Fluent and type-safe log builder:
    LOGGER.info()
        .setEvent("foo")
        .setMessage("foo was called")
        .addData("key1", 1234)
        .addData("widget", widget)
        .log();

    // Fluent builder via lambda:
    LOGGER.info(l -> {
        l.setEvent("foo")
            .setMessage("foo was called")
            .addData("key1", 1234)
            .addData("widget", widget)
    });

    // Additional data with arrays:
    LOGGER.info("foo", "foo was called", new String[]{"key1", "widget"}, new Object[]{1234, widget});

    // Additional data with a map:
    LOGGER.info("foo", "foo was called", ImmutableMap.of("key1", 1234, "widget", widget));

    // Additional data with an array and var args:
    LOGGER.info("foo", "foo was called", new String[]{"key1", "widget"}, 1234, widget);
  }

  @Loggable
  private static class Widget() {
    private final String name;

    public Widget(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }
  }
}
```

Output:

```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"message":"foo was called"},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw41"}
{"time":"2011-11-11T00:00:00.100Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw42"}
{"time":"2011-11-11T00:00:00.200Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw43"}
{"time":"2011-11-11T00:00:00.300Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw44"}
{"time":"2011-11-11T00:00:00.400Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw45"}
{"time":"2011-11-11T00:00:00.500Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw46"}
```

### Rate Limited Logging

It is possible to limit the number of times any particular messages are logged in an interval by using a __RateLimitLogger__.
Instead of instantiating a __Logger__ instantiate a __RateLimitLogger__ using the __LoggerFactory__. Any messages logged to
the __RateLimitLogger__ instance will be limited to no more than one in the specified duration.

For example:

```java
private static final Logger CONNECT_INFO_LOGGER = LoggerFactory.getRateLimitLogger(MyClass.class, Duration.ofSeconds(1));

public void accept(final ConnectionInfo info) {
    CONNECT_INFO_LOGGER.setMessage("Connection established")
        .addData("source", info.getSource())
        .log();
}
```

### Context Weaving

The library contains an Aspect for weaving additional context into all __log()__ invocations of the Steno LogBuilder.  The
additional context includes file, class and line.  Since the additional context is woven at compile time this
enables efficient injection of this context information versus the inefficient stack trace capture used by default in Logback.
Finally, the file, class and line context injection should __not__ be enabled in the encoder configuration if using context
weaving.

#### Maven

To enable context weaving in your Maven project add the following to your pom:

```xml
<project>
    ...
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>aspectj-maven-plugin</artifactId>
                <version>1.7</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>test-compile</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <complianceLevel>1.8</complianceLevel>
                    <aspectLibraries>
                        <aspectLibrary>
                            <groupId>com.arpnetworking.logback</groupId>
                            <artifactId>logback-steno</artifactId>
                        </aspectLibrary>
                    </aspectLibraries>
                    <showWeaveInfo>true</showWeaveInfo>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>org.aspectj</groupId>
                        <artifactId>aspectjtools</artifactId>
                        <version>1.8.5</version>
                    </dependency>
                </dependencies>
            </plugin>
            ...
        <plugins>
    </build>
    ...
</project>
```

__Note:__ The example above assumes you are compiling from/to Java8.

#### SBT

To enable context weaving in your SBT project make the following changes:

project/plugins.sbt<br/>
```scala
resolvers += "SBT Community repository" at "http://dl.bintray.com/sbt/sbt-plugin-releases/"

addSbtPlugin("com.typesafe.sbt" % "sbt-aspectj" % "0.10.0")
```

project/Build.scala<br/>
```scala
import sbt._
import Keys._
// You probably have other imports here
import com.typesafe.sbt.SbtAspectj._
import com.typesafe.sbt.SbtAspectj.AspectjKeys._

object ApplicationBuild extends Build {

    // Add 'aspectjSettings' to any other settings here with ++:
    val s = aspectjSettings
    val main = Project("MyApp", file("."), settings = s).settings(
      // Other project settings go here

      // AspectJ
      binaries in Aspectj <++= update map { report =>
        report.matching(moduleFilter(organization = "com.arpnetworking.logback", name = "logback-steno"))
      },
      inputs in Aspectj <+= compiledClasses,
      products in Compile <<= products in Aspectj,
      products in Runtime <<= products in Compile
    )
}
```

#### Gradle

To enable context weaving in your Gradle project make the following changes:

```groovy
// Set to at least version 1.6.0
def logback_steno_version = '1.6.0'

project.ext {
    aspectjVersion = '1.8.5'
}

apply plugin: 'aspectj'

buildscript {
    repositories {
        // Other build repositories (e.g. Maven Central)
        maven {
            url "https://maven.eveoh.nl/content/repositories/releases"
        }
    }

    dependencies {
        // Other build dependencies (e.g. Protobuf)
        classpath group: 'nl.eveoh', name: 'gradle-aspectj', version: '1.5'
    }
}

dependencies {
    aspectpath group: 'com.arpnetworking.logback', name: 'logback-steno', version: logback_steno_version
    compile group: 'com.arpnetworking.logback', name: 'logback-steno', version: logback_steno_version
    // Other dependencies
}
```

For more information please see [https://github.com/eveoh/gradle-aspectj](https://github.com/eveoh/gradle-aspectj).


SLF4J Logger
------------

The Steno encoder uses SLF4Js marker methods to pass structured data to the encoder.  However, all log messages will be
Steno encoded although calls not using the marker method will not be able to attach additional data to each message.

### Example 1: Complete Example with SLF4J

Code:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.arpnetworking.logback.annotations.Loggable;
import com.arpnetworking.logback.StenoMarker;
import com.google.common.collect.ImmutableMap;

public class MyClass {
  private static final Logger LOGGER = LoggerFactory.getLogger(MyClass.class);

  public void foo() {
    final Widget widget = new Widget("MyWidget");

    // Default:
    LOGGER.info("foo was called");

    // Serialized Key-Values:
    LOGGER.info(StenoMarker.ARRAY_MARKER, "foo", new String[] {"message","key1","widget"}, new Object[] {"foo was called",1234,widget});
    LOGGER.info(StenoMarker.MAP_MARKER, "foo", ImmutableMap.of("message","foo was called","key1",1234,"widget",widget));

    // Raw Json Key-Values:
    LOGGER.info(StenoMarker.ARRAY_JSON_MARKER, "foo", new String[] {"message","key1","widget"}, new Object[] {"\"foo was called\"",1234,"{\"name\":\"MyWidget\"}"});
    LOGGER.info(StenoMarker.MAP_JSON_MARKER, "foo", ImmutableMap.of("message","\"foo was called\"","key1",1234,"widget","{\"name\":\"MyWidget\"}"));

    // Objects:
    LOGGER.info(StenoMarker.OBJECT_MARKER, "foo", widget);
    LOGGER.info(StenoMarker.OBJECT_JSON_MARKER, "foo", "{\"name\":\"MyWidget\"}");
  }

  @Loggable
  private static class Widget() {
    private final String name;

    public Widget(String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }
  }
}
```

Output:

```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"message":"foo was called"},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.100Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.200Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.300Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.400Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.500Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.600Z","name":"foo","level":"info","data":{"name":"MyWidget",},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.700Z","name":"foo","level":"info","data":{"name":"MyWidget",},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 2: Embedding Key-Value Pairs With Arrays

This allows injecting serialized values for corresponding keys across two arrays.

Code:

```java
LOGGER.info(StenoMarker.ARRAY_MARKER, "log", new String[] {"key1","key2"}, new Object[] {1234, "foo"});
```

Output:

```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"key1":1234,"key2":"foo"},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 3: Embedding Json With Arrays

This allows injecting raw json values for corresponding keys across two arrays.

Code:

```java
log.info(StenoMarker.ARRAY_JSON_MARKER, "log", new String[] {"json"}, new Object[] {"{\"key\":\"value\"}"});
```

Output:

```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"json":{"key":"value"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 4: Embedding Key-Value Pairs With Maps

This allows injecting serialized values for keys in a map.

Code:

```java
LOGGER.info(StenoMarker.MAP_MARKER, "log", ImmutableMap.of("key1",1234,"key2","foo"));
```

Output:

```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"key1":1234,"key2":"foo"},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 5: Embedding Json With Maps

This allows injecting raw json values for keys in a map.

Code:

```java
log.info(StenoMarker.MAP_JSON_MARKER, "log", ImmutableMap.of("json", "{\"key\":\"value\"}"));
```

Output:

```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"json":{"key":"value"}},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 6: Embedding Object

This allows insertion of an object as the value (when serialized) of the _data_ key.

Code:

```java
public class Widget {
  public Widget(final String name) {
    this.name = name;
  }
  public String getName() {
    return name;
  }
  private final String name;
}

log.info(StenoMarker.OBJECT_MARKER, "log", new Widget("MyWidget"));
```

Output:

```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"name":"MyWidget"},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 7: Embedding Json Object

This allows insertion of a raw json object as the value of the _data_ key.

Code:

```java
log.info(StenoMarker.OBJECT_JSON_MARKER, "log", "{\"key\":\"value\"}");
```

Output:

```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"key":"value"},"context":{"host":"<HOST>","processId":"<PROCESS>","threadId":"<THREAD>"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

Exceptions
----------

The SLF4J logging methods which accept a marker (e.g. StenoMarker.*) also accept a Throwable as the final argument.  The
value of the Throwable will be serialized into the top-level exception block in the Steno format.

Redacting and Ignoring Fields
-----------------------------

When serializing non-primitive class instances it is often desirable to redact certain field values or to suppress them
entirely.  Redaction is supported with the @LogRedact annotation.  Any bean property marked with this annotation will
emit the key but replace the value with *<REDACTED>*.  The annotation may be placed on a member or its getter; however,
if a getter exists the member must match the getter name (e.g. _private String foo;_ and _public String getFoo() {...}_).

Redaction may be disabled by setting the RedactEnabled encoder property to false (it defaults to true).  Further,
redaction of null values may be disabled by setting the RedactNull encoder property to false (it defaults to true).
Suppression is supported with Jackson's @JsonIgnore.

Logging Non-Pojo/Bean Classes
-----------------------------

To log a representation of an object other than what is defined by its fields/accessors you can annotate a method with
@LogValue to return an alternate representation.  Typically, this is built with LogValueMapFactory but it can be any
object which is serialized in place of the original.  If you wish to enable bean identifier injection you should provide
the LogValueMapFactory with the object being logged through the appropriate __builder__ static factory method.

The @LogValue annotation provides the same functionality as @JsonValue which is honored in the absence of @LogValue.
However, the separate @LogValue annotation permits an alternate logged form to the serialized form.  You may also
disable both @LogValue and @LogJson and revert an instance to bean serialization by including both annotations and
setting the value and fallback attributes both to false.

The following table summarizes the configuration options:

| Annotation        |               |                | @JsonValue Absent | @JsonValue Present |                    |
|-------------------|---------------|----------------|:-----------------:|:------------------:|:------------------:|
|                   |               |                |                   | value=true         | value=false        |
| @LogValue Absent  |               |                | SJS               | @JV                | SJS                |
| @LogValue Present | enabled=true  | fallback=true  | @LV               | @LV                | @LV                |
|                   |               | fallback=false | @LV               | @LV                | @LV                |
|                   | enabled=false | fallback=true  | SJS               | @JV                | SJS                |
|                   |               | fallback=false | SJS               | @JV                | SJS                |

The three possible outcomes are:

| Result     | Meaning                                                                                             |
|------------|-----------------------------------------------------------------------------------------------------|
| SJS        | Standard Jackson Serialization                                                                      |
| @JV        | @JsonValue Serialization                                                                            |
| @LV        | @LogValue Serialization                                                                             |

Rolling Policies
----------------

The package contains two additional log file rolling policies: __RandomizedTimeBasedFNATP__ and __SizeAndRandomizedTimeBasedFNATP__.  Both of these policies randomize the roll of the log with respect to time.  The benefit of this policy in a distributed environment is that every machine does not attempt to roll its log at the same time.  The second policy augments randomized time based rolling with a maximum file size.

Example of RandomizedTimeBasedFNATP configuration:

```xml
<timeBasedFileNamingAndTriggeringPolicy class="com.arpnetworking.logback.RandomizedTimeBasedFNATP">
    <maxOffsetInMillis>900000</maxOffsetInMillis>
</timeBasedFileNamingAndTriggeringPolicy>
```

Example of SizeAndRandomizedTimeBasedFNATP configuration:

```xml
<timeBasedFileNamingAndTriggeringPolicy class="com.arpnetworking.logback.SizeAndRandomizedTimeBasedFNATP">
    <maxOffsetInMillis>900000</maxOffsetInMillis>
    <maxFileSize>100MB</maxFileSize>
    <totalSizeCap>1GB</totalSizeCap>
</timeBasedFileNamingAndTriggeringPolicy>
```

Development
-----------

To build the library locally you must satisfy these prerequisites:
* [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (Or Invoke with JDKW)

Next, fork the repository, clone and build:

Building:

    logback-steno> ./mvnw verify

To use the local version in your project you must first install it locally:

    logback-steno> ./mvnw install

You can determine the version of the local build from the pom.xml file.  Using the local version is intended only for testing or development.

You may also need to add the local repository to your build in order to pick-up the local version:

* Maven - Included by default.
* Gradle - Add *mavenLocal()* to *build.gradle* in the *repositories* block.
* SBT - Add *resolvers += Resolver.mavenLocal* into *project/plugins.sbt*.

License
-------

Published under Apache Software License 2.0, see LICENSE
