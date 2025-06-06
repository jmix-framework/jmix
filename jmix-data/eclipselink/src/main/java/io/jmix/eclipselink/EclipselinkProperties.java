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

package io.jmix.eclipselink;

import io.jmix.core.annotation.Experimental;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.eclipselink")
public class EclipselinkProperties {
    boolean queryCacheEnabled;
    /**
     * EXPERIMENTAL:
     * Disables lazy loading. Accessing an unfetched reference property will throw an {@link IllegalStateException}
     * instead of performing a separate loading request.
     */
    @Experimental
    boolean disableLazyLoading;

    public EclipselinkProperties(@DefaultValue("true") boolean queryCacheEnabled,
                                 @DefaultValue("false") boolean disableLazyLoading) {
        this.queryCacheEnabled = queryCacheEnabled;
        this.disableLazyLoading = disableLazyLoading;
    }

    public boolean isQueryCacheEnabled() {
        return queryCacheEnabled;
    }

    /**
     * @see #disableLazyLoading
     */
    public boolean isDisableLazyLoading() {
        return disableLazyLoading;
    }
}
