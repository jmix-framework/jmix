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

package io.jmix.eclipselink.impl.entitycache;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class QueryResult implements Serializable {
    private static final long serialVersionUID = -4238659709136264710L;

    protected final List result;
    protected final String type;
    protected final Set<String> relatedTypes;
    protected final RuntimeException exception;

    public QueryResult(List<?> result, String type, Set<String> relatedTypes) {
        this(result, type, relatedTypes, null);
    }

    public QueryResult(List<?> result, String type, Set<String> relatedTypes, RuntimeException exception) {
        this.result = Collections.unmodifiableList(result);
        this.type = type;
        this.relatedTypes = relatedTypes;
        this.exception = exception;
    }

    public List getResult() {
        return result;
    }

    public String getType() {
        return type;
    }

    public Set<String> getRelatedTypes() {
        return relatedTypes;
    }

    public RuntimeException getException() {
        return exception;
    }
}
