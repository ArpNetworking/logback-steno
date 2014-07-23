logback-steno
=============

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

Configuration
-------------

Example appender configuration in XML:<br/>
```xml
<configuration>
    <appender name="STENO_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <File>log/application.steno.log</File>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>log/application-%d{yyyy-MM-dd}.steno.log.gz</fileNamePattern>
            <maxHistory>10</maxHistory>
        </rollingPolicy>
        <encoder class="com.arpnetworking.logback.StenoEncoder" />
    </appender>
    
     <root>
        <level value="INFO"/>
        <appender-ref ref="STENO_FILE"/>
    </root>
</configuration>
```

Optionally, you may wrap the FileAppender in an AsyncAppender:<br/>
```xml
<configuration>
    <appender name="STENO_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <File>log/application.steno.log</File>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>log/application-%d{yyyy-MM-dd}.steno.log.gz</fileNamePattern>
            <maxHistory>10</maxHistory>
        </rollingPolicy>
        <encoder class="com.arpnetworking.logback.StenoEncoder" />
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

Example appender configuration in Java:<br/>
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

Usage Examples
--------------

### Example 1: Complete Example with SLF4J

Command:<br/>
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyClass {
  private static final Logger LOGGER = LoggerFactory.getLogger(MyClass.class);

  public void foo() {
    LOGGER.info("foo was called");
  }
}
```

Output:<br/>
```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"message":"foo was called"},"context":{"thread_id":"thread"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 2: Embedding Key-Value Pairs

Command:<br/>
```java
LOGGER.info(StenoMarker.ARRAY_MARKER, "log", new String[] {"key1","key2"}, new Object[] {1234, "foo"});
```

Output:<br/>
```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"key1":1234,"key2":"foo"},"context":{"thread_id":"thread"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 3: Embedding Json

Java:<br/>
```java
log.info(StenoMarker.JSON_MARKER, "log", "json", "{\"key\":\"value\"}");
```

Output:<br/>
```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"json":{"key":"value"}},"context":{"thread_id":"thread"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

Prerequisites
-------------

To build the library locally you must satisfy these prerequisites:
* [JDK7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* [Maven 3.0.5+](http://maven.apache.org/download.cgi)

Next, fork the repository, clone and build:

Building:

    logback-steno> mvn verify

To use the local version in your project you must first install it locally:
 
    logback-steno> mvn install

You can determine the version of the local build from the pom file.  Using the local version is intended only for testing or development.

You may also need to add the local repository to your build in order to pick-up the local version:

* Maven - Included by default.
* Gradle - Add *mavenLocal()* to *build.gradle* in the *repositories* block.
* SBT - Add *resolvers += Resolver.mavenLocal* into *project/plugins.sbt*.

License
-------

Published under Apache Software License 2.0, see LICENSE
