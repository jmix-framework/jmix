/*
 * Copyright (c) 2008-2016 Haulmont.
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

package io.jmix.core.impl.validation;


import io.jmix.core.TimeSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ClockProvider;
import java.time.Clock;
import java.time.ZonedDateTime;

@Component("core_JmixValidationClockProvider")
public class ValidationClockProvider implements ClockProvider {

    @Autowired
    protected TimeSource timeSource;

    @Override
    public Clock getClock() {
        ZonedDateTime now = timeSource.now();
        return Clock.fixed(now.toInstant(), now.getZone());
    }
}