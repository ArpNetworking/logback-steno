/*
 * Copyright 2015 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arpnetworking.steno.aspect;

import com.arpnetworking.steno.LogBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.SourceLocation;

/**
 * Aspect for <code>LogBuilder</code> weaving line, file and class into the
 * context block of each <code>LogBuilder</code> message.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
@Aspect
@SuppressFBWarnings("MS_SHOULD_BE_FINAL")
public class LogBuilderAspect {

    /**
     * Before outputting the message inject additional context.
     *
     * @param joinPoint The <code>JoinPoint</code>.
     */
    @Before("call(* com.arpnetworking.steno.LogBuilder.log())")
    public void addToContextLineAndMethod(final JoinPoint joinPoint) {
        final SourceLocation sourceLocation = joinPoint.getSourceLocation();
        final LogBuilder targetLogBuilder = (LogBuilder) joinPoint.getTarget();
        targetLogBuilder.addContext("line", String.valueOf(sourceLocation.getLine()));
        targetLogBuilder.addContext("file", sourceLocation.getFileName());
        targetLogBuilder.addContext("class", sourceLocation.getWithinType());
    }
}
