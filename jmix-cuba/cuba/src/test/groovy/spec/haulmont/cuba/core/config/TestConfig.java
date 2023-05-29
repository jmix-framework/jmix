/*
 * Copyright 2019 Haulmont.
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

package spec.haulmont.cuba.core.config;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;

public interface TestConfig extends Config {

    @Property("test.bar")
    @Source(type = SourceType.APP)
    @Default("bar-value")
    String getBar();

    @Property("test.foo")
    @Source(type = SourceType.DATABASE)
    String getFoo();

    void setFoo(String value);

    default String getFooOrDefault() {
        String foo = getFoo();
        return foo != null ? foo : getBar();
    }
}
