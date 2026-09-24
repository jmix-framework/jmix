/*
 * Copyright 2026 Haulmont.
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

package test_support.localized_string;

import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.datatype.impl.StringDatatype;
import org.jspecify.annotations.Nullable;

import java.text.ParseException;
import java.util.Locale;

/**
 * A datatype derived from the standard string one that parses strictly, like an application datatype of a phone
 * number or a code: a filter must not hand the partial text of a string operation to its parser.
 */
@DatatypeDef(id = StrictCodeDatatype.ID, javaClass = String.class, value = "test_StrictCodeDatatype")
public class StrictCodeDatatype extends StringDatatype {

    public static final String ID = "test_strictCode";

    @Override
    public String parse(@Nullable String value, Locale locale) throws ParseException {
        if (value != null && !value.matches("\\d{6}")) {
            throw new ParseException("Not a six-digit code: " + value, 0);
        }
        return value;
    }
}
