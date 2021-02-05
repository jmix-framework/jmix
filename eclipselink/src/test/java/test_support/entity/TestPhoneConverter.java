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

package test_support.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class TestPhoneConverter implements AttributeConverter<TestPhone, String> {

    @Override
    public String convertToDatabaseColumn(TestPhone attribute) {
        if (attribute == null)
            return null;
        return attribute.toString();
    }

    @Override
    public TestPhone convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        String[] strings = dbData.split(" ");
        return new TestPhone(strings[0], strings[1]);
    }
}
