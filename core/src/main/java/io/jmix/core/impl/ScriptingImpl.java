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

import io.jmix.core.ConfigInterfaces;
import io.jmix.core.GlobalConfig;
import io.jmix.core.Scripting;
import io.jmix.core.ServerConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component(Scripting.NAME)
public class ScriptingImpl extends AbstractScripting {

    private String[] scriptEngineRoots;

    @Inject
    public ScriptingImpl(Environment environment,
                         JavaClassLoader javaClassLoader,
                         ConfigInterfaces configInterfaces,
                         SpringBeanLoader springBeanLoader) {
        super(environment, javaClassLoader, configInterfaces, springBeanLoader);
        scriptEngineRoots = new String[] {
                configInterfaces.getConfig(GlobalConfig.class).getConfDir(),
                configInterfaces.getConfig(ServerConfig.class).getDbDir()
        };
    }

    @Override
    protected String[] getScriptEngineRootPath() {
        return scriptEngineRoots;
    }
}