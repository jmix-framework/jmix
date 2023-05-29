/*
 * Copyright 2022 Haulmont.
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

package test_support;

import io.jmix.core.JmixOrder;
import io.jmix.data.QueryParamValueProvider;
import org.springframework.lang.Nullable;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("test_TestQueryParamValueProvider")
@Order(JmixOrder.HIGHEST_PRECEDENCE - 100)
public class TestQueryParamValueProvider implements QueryParamValueProvider {

    public static final String TEST_PREFIX = "test_";

    private Map<String, Object> values = new HashMap<>();

    @Override
    public boolean supports(String paramName) {
        return paramName.startsWith(TEST_PREFIX);
    }

    @Nullable
    @Override
    public Object getValue(String paramName) {
        if (supports(paramName)) {
            String attrName = paramName.substring(TEST_PREFIX.length());
            return values.get(attrName);
        }
        return null;
    }

    public void setValue(String name, Object value) {
        values.put(name, value);
    }

    public void clear(String name) {
        values.remove(name);
    }
}
