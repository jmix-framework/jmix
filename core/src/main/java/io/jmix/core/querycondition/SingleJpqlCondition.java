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

package io.jmix.core.querycondition;

import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Condition that represents JPQL query with "where" and optional "join" sections and one parameter.
 */
public class SingleJpqlCondition extends JpqlCondition {

    protected Object parameterValue;

    public SingleJpqlCondition(List<Entry> entries) {
        super(entries);
    }

    public SingleJpqlCondition(@Nullable String join, String where) {
        super(join, where);
    }

    public SingleJpqlCondition(String where) {
        super(where);
    }

    public static SingleJpqlCondition createWithValue(@Nullable String join,
                                                      String where,
                                                      @Nullable Object parameterValue) {
        SingleJpqlCondition condition = new SingleJpqlCondition(join, where);
        condition.setParameterValue(parameterValue);
        return condition;
    }

    @Nullable
    public Object getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(@Nullable Object parameterValue) {
        this.parameterValue = parameterValue;
    }

    @Nullable
    @Override
    public Condition actualize(Set<String> actualParameters) {
        if (actualParameters.containsAll(getParameters())) {
            return this;
        }

        if (parameterValue != null) {
            if (parameterValue instanceof String) {
                if (!Strings.isNullOrEmpty((String) parameterValue)) {
                    return this;
                }
            } else {
                return this;
            }
        }
        return null;
    }

    @Override
    public Condition copy() {
        List<Entry> entriesCopy = new ArrayList<>(entries.size());
        for (Entry entry : entries) {
            entriesCopy.add(new Entry(entry.name, entry.value));
        }
        SingleJpqlCondition condition = new SingleJpqlCondition(entriesCopy);
        condition.setParameterValue(parameterValue);
        return condition;
    }
}
