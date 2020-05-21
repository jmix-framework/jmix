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

package format_strings

import io.jmix.core.metamodel.datatype.FormatStrings
import io.jmix.core.metamodel.datatype.impl.FormatStringsRegistryImpl

import java.util.Locale

class TestFormatStringsRegistry extends FormatStringsRegistryImpl {

    TestFormatStringsRegistry() {
        setFormatStrings(Locale.ENGLISH, new FormatStrings(
                '.' as char, ',' as char,
                '#,##0', '#,##0.###', '#,##0.##',
                'dd/MM/yyyy', 'dd/MM/yyyy HH:mm', 'dd/MM/yyyy HH:mm', 'HH:mm', 'HH:mm',
                'True', 'False'))
    }
}
