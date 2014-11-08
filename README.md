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

_Note 1_: Injecting the logger name into the context is not strictly compliant with the current definition of Steno.
_Note 2_: Injecting class, file, method or line will incur a significant performance penalty. 

Optionally, you may additionally wrap the FileAppender in an AsyncAppender:<br/>
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

Code:<br/>
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

Output:<br/>
```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"message":"foo was called"},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.100Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.200Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.300Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.400Z","name":"foo","level":"info","data":{"message":"foo was called","key1":1234,"widget":{"name":"MyWidget"}},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.400Z","name":"foo","level":"info","data":{"name":"MyWidget",},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
{"time":"2011-11-11T00:00:00.400Z","name":"foo","level":"info","data":{"name":"MyWidget",},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 2: Embedding Key-Value Pairs With Arrays

This allows injecting serialized values for corresponding keys across two arrays.

Code:<br/>
```java
LOGGER.info(StenoMarker.ARRAY_MARKER, "log", new String[] {"key1","key2"}, new Object[] {1234, "foo"});
```

Output:<br/>
```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"key1":1234,"key2":"foo"},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 3: Embedding Json With Arrays

This allows injecting raw json values for corresponding keys across two arrays.

Code:<br/>
```java
log.info(StenoMarker.ARRAY_JSON_MARKER, "log", new String[] {"json"}, new Object[] {"{\"key\":\"value\"}"});
```

Output:<br/>
```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"json":{"key":"value"}},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 4: Embedding Key-Value Pairs With Maps

This allows injecting serialized values for keys in a map.

Code:<br/>
```java
LOGGER.info(StenoMarker.MAP_MARKER, "log", ImmutableMap.of("key1",1234,"key2","foo"));
```

Output:<br/>
```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"key1":1234,"key2":"foo"},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 5: Embedding Json With Maps

This allows injecting raw json values for keys in a map.

Code:<br/>
```java
log.info(StenoMarker.MAP_JSON_MARKER, "log", ImmutableMap.of("json", "{\"key\":\"value\"}"));
```

Output:<br/>
```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"json":{"key":"value"}},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 6: Embedding Object

This allows insertion of an object as the value (when serialized) of the _data_ key. 

Code:<br/>
```java
public class Widget {
  public Widget(final String value) {
    this.value = value;
  }
  public String getValue() {
    return value;
  }
  private final String value;
}

log.info(StenoMarker.OBJECT_MARKER, "log", new Widget("value"));
```

Output:<br/>
```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"key":"value"},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

### Example 7: Embedding Json Object

This allows insertion of a raw json object as the value of the _data_ key. 

Code:<br/>
```java
log.info(StenoMarker.OBJECT_JSON_MARKER, "log", "{\"key\":\"value\"}");
```

Output:<br/>
```json
{"time":"2011-11-11T00:00:00.000Z","name":"log","level":"info","data":{"key":"value"},"context":{"host":"h","processId":"p","threadId":"t"},"id":"oRw59PrARvatGNC7fiWw4A"}
```

Redacting and Ignoring Fields
-----------------------------

When serializing non-primitive class instances it is often desirable to redact certain field values or to suppress them
entirely.  Redaction is supported with the @LogRedact annotation.  Any bean property marked with this annotation will
emit the key but replace the value with *<REDACTED>*.  Redaction may be disabled by setting the RedactEnabled encoder
property to false (it defaults to true).  Further, redaction of null values may be disabled by setting the RedactNull
encoder property to false (it defaults to true). Suppression is supported with Jackson's @JsonIgnore.

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
