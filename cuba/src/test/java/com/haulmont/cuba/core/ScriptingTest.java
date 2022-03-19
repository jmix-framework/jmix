/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.testsupport.CoreTest;
import groovy.lang.Binding;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class ScriptingTest {

    @Autowired
    protected Scripting scripting;

    @Test
    public void testSimpleEvaluate() {
        Integer intResult = scripting.evaluateGroovy("2 + 2", new Binding());
        assertEquals((Integer) 4, intResult);

        Binding binding = new Binding();
        binding.setVariable("instance", new User());
        Boolean boolResult = scripting.evaluateGroovy("import com.haulmont.cuba.core.global.AppBeans\n" +
                "import io.jmix.core.EntityStates\n" +
                "return AppBeans.get(EntityStates.class).isNew(instance)", binding);
        assertTrue(boolResult);
    }

    @Test
    public void testImportsEvaluate() {
        String result = scripting.evaluateGroovy("import io.jmix.core.common.util.StringHelper\n" +
                "return StringHelper.removeExtraSpaces(' Hello! ')", (Binding) null);
        assertNotNull(result);
    }

    @Test
    public void testPackageAndImportsEvaluate() {
        String result = scripting.evaluateGroovy("package com.haulmont.cuba.core\n" +
                "import io.jmix.core.common.util.StringHelper\n" +
                "return StringHelper.removeExtraSpaces(' Hello! ')", (Binding) null);
        assertNotNull(result);
    }

    @Test
    public void testPackageOnlyEvaluate() {
        Binding binding = new Binding();
        binding.setVariable("instance", new User());
        Boolean result = scripting.evaluateGroovy("package com.haulmont.cuba.core\n" +
                "import com.haulmont.cuba.core.global.AppBeans\n" +
                "import io.jmix.core.EntityStates\n" +
                "return AppBeans.get(EntityStates.class).isNew(instance)", binding);
        assertTrue(result);
    }
}