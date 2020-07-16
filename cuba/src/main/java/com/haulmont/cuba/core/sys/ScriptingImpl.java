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

package com.haulmont.cuba.core.sys;

import com.haulmont.cuba.core.global.Scripting;
import io.jmix.core.CoreProperties;
import io.jmix.core.ClassManager;
import io.jmix.core.impl.SpringBeanLoader;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Component(Scripting.NAME)
public class ScriptingImpl extends AbstractScripting {

    private String[] scriptEngineRoots;

    @Autowired
    public ScriptingImpl(Environment environment,
                         ClassManager classManager,
                         CoreProperties properties,
                         SpringBeanLoader springBeanLoader) {
        super(environment, properties.getConfDir(), classManager, springBeanLoader);
        scriptEngineRoots = new String[] { properties.getConfDir(), properties.getDbDir() };
    }

    @Override
    protected String[] getScriptEngineRootPath() {
        return scriptEngineRoots;
    }
}