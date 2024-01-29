/*
 * Copyright 2024 Haulmont.
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

package test_support.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jmix.chartsflowui.kit.component.event.dto.BaseChartEventDetail;

import java.util.List;
import java.util.Map;

public class JmixChartTestEventDetail extends BaseChartEventDetail {

    protected Map<String, String> testMap;

    protected List<Integer> numbers;

    protected TestDto testDto;

    protected String nullField;

    @JsonProperty("named")
    protected String alterNamed;

    @JsonIgnore
    protected String ignored;

    public Map<String, String> getTestMap() {
        return testMap;
    }

    public void setTestMap(Map<String, String> testMap) {
        this.testMap = testMap;
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<Integer> numbers) {
        this.numbers = numbers;
    }

    public TestDto getTestDto() {
        return testDto;
    }

    public void setTestDto(TestDto testDto) {
        this.testDto = testDto;
    }

    public String getNullField() {
        return nullField;
    }

    public void setNullField(String nullField) {
        this.nullField = nullField;
    }

    public String getAlterNamed() {
        return alterNamed;
    }

    public void setAlterNamed(String alterNamed) {
        this.alterNamed = alterNamed;
    }

    public String getIgnored() {
        return ignored;
    }

    public void setIgnored(String ignored) {
        this.ignored = ignored;
    }

    public static class TestDto {

        protected String strField;

        protected Integer intField;

        protected Boolean boolField;

        protected Long longField;

        public String getStrField() {
            return strField;
        }

        public void setStrField(String strField) {
            this.strField = strField;
        }

        public Integer getIntField() {
            return intField;
        }

        public void setIntField(Integer intField) {
            this.intField = intField;
        }

        public Boolean getBoolField() {
            return boolField;
        }

        public void setBoolField(Boolean boolField) {
            this.boolField = boolField;
        }

        public Long getLongField() {
            return longField;
        }

        public void setLongField(Long longField) {
            this.longField = longField;
        }
    }
}
