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

import io.jmix.eclipselink.impl.dbms.HsqlDbmsFeatures;

/**
 * HSQLDB reporting that its char function returns a binary string, as MySQL's does, so that the suite can run the
 * branch of the localized string expression that lower-cases the column. The class is in the package of the test
 * module, so that {@code DbmsSpecifics} prefers it to the HSQLDB features of the framework, and it is not a
 * component: only the test that needs it registers it.
 */
public class BinaryCharHsqlDbmsFeatures extends HsqlDbmsFeatures {

    @Override
    public boolean isCharFunctionBinary() {
        return true;
    }
}
