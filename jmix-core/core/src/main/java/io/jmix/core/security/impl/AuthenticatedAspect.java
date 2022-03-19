/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.security.impl;

import io.jmix.core.security.SystemAuthenticator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component("core_AuthenticatedAspect")
public class AuthenticatedAspect {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatedAspect.class);

    private SystemAuthenticator authenticator;

    @Autowired
    public void setAuthenticator(SystemAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Around("execution(@io.jmix.core.security.Authenticated * *(..))")
    private Object aroundInvoke(ProceedingJoinPoint ctx) throws Throwable {
        if (log.isTraceEnabled())
            log.trace("Authenticating: {}", ctx.getSignature());

        try {
            authenticator.begin();
            return ctx.proceed();
        } finally {
            authenticator.end();
        }
    }

}
