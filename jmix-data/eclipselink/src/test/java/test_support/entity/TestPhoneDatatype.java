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

import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.datatype.Datatype;

import jakarta.annotation.Nullable;
import java.text.ParseException;
import java.util.Locale;

@DatatypeDef(id = "testPhone", javaClass = TestPhone.class, defaultForClass = true)
public class TestPhoneDatatype implements Datatype<TestPhone> {

    @Override
    public String format(@Nullable Object value) {
        return value == null ? "" : value.toString();
    }

    @Override
    public String format(@Nullable Object value, Locale locale) {
        return format(value);
    }

    @Nullable
    @Override
    public TestPhone parse(@Nullable String value) throws ParseException {
        if (value == null)
            return null;
        String[] strings = value.split(" ");
        return new TestPhone(strings[0], strings[1]);
    }

    @Nullable
    @Override
    public TestPhone parse(@Nullable String value, Locale locale) throws ParseException {
        return parse(value);
    }
}
