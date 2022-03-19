/*
 * Copyright 2021 Haulmont.
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

import org.elasticsearch.action.DocWriteRequest;

import java.util.Objects;

public abstract class TestAbstractBulkRequestActionValidationData {

    protected String index;
    protected String id;

    public TestAbstractBulkRequestActionValidationData(String index, String id) {
        this.index = index;
        this.id = id;
    }

    public abstract DocWriteRequest.OpType getOperationType();

    public String getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestAbstractBulkRequestActionValidationData that = (TestAbstractBulkRequestActionValidationData) o;
        return Objects.equals(index, that.index) &&
                Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, id);
    }

    @Override
    public String toString() {
        return String.format("Operation=%s Index=%s ID=%s", getOperationType(), index, id);
    }
}
