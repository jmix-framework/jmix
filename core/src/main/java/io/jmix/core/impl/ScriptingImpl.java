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

package io.jmix.core.impl;

import io.jmix.core.CoreProperties;
import io.jmix.core.HotDeployManager;
import io.jmix.core.Scripting;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component(Scripting.NAME)
public class ScriptingImpl extends AbstractScripting {

    private String[] scriptEngineRoots;

    @Inject
    public ScriptingImpl(Environment environment,
                         HotDeployManager hotDeployManager,
                         CoreProperties properties,
                         SpringBeanLoader springBeanLoader) {
        super(environment, properties.getConfDir(), hotDeployManager, springBeanLoader);
        scriptEngineRoots = new String[] { properties.getConfDir(), properties.getDbDir() };
    }

    @Override
    protected String[] getScriptEngineRootPath() {
        return scriptEngineRoots;
    }
}