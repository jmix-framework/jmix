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
package com.haulmont.cuba.core.app;

import com.haulmont.cuba.core.global.ViewNotFoundException;
import com.haulmont.cuba.core.global.ViewRepository;
import io.jmix.core.Entity;
import io.jmix.core.FetchPlan;
import io.jmix.core.TimeSource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.TimeZone;

/**
 * Standard implementation of {@link ServerInfoService} interface.
 *
 * <p>Annotated with <code>@Component</code> instead of <code>@Service</code> to be available before user login.</p>
 */
@Component(ServerInfoService.NAME)
public class ServerInfoServiceBean implements ServerInfoService {

    @Inject
    protected ViewRepository viewRepository;

    @Inject
    protected ServerInfoAPI serverInfo;

    @Inject
    protected TimeSource timeSource;

    @Override
    public String getReleaseNumber() {
        return serverInfo.getReleaseNumber();
    }

    @Override
    public String getReleaseTimestamp() {
        return serverInfo.getReleaseTimestamp();
    }

    @Override
    public FetchPlan getView(Class<? extends Entity> entityClass, String name) {
        try {
            return viewRepository.getView(entityClass, name);
        } catch (ViewNotFoundException e) {
            return null;
        }
    }

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    @Override
    public long getTimeMillis() {
        return timeSource.currentTimeMillis();
    }
}
