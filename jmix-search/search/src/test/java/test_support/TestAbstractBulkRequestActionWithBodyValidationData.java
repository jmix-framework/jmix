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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public abstract class TestAbstractBulkRequestActionWithBodyValidationData extends TestAbstractBulkRequestActionValidationData {

    protected JsonNode source;

    public TestAbstractBulkRequestActionWithBodyValidationData(String index, String id, JsonNode source) {
        super(index, id);
        this.source = source;
    }

    public JsonNode getSource() {
        return source;
    }

    @Override
    public String toString() {
        return super.toString() + " Source=" + source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TestAbstractBulkRequestActionWithBodyValidationData that = (TestAbstractBulkRequestActionWithBodyValidationData) o;
        return TestJsonUtils.areEqualIgnoringOrder(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), source);
    }
}
