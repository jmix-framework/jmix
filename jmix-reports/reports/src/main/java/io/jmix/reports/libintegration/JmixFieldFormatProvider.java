/*
 * Copyright 2021 Haulmont.
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
package io.jmix.reports.libintegration;

import com.haulmont.yarg.formatters.impl.DefaultFormatProvider;
import io.jmix.core.InstanceNameProvider;
import io.jmix.core.Entity;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

public class JmixFieldFormatProvider implements DefaultFormatProvider {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected Messages messages;

    @Autowired
    protected InstanceNameProvider instanceNameProvider;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Override
    @Nullable
    public String format(@Nullable Object o) {
        if (o != null) {
            Datatype datatype = datatypeRegistry.find(o.getClass());
            if (datatype != null) {
                if (currentAuthentication.isSet()) {
                    return datatype.format(o, currentAuthentication.getLocale());
                } else {
                    return datatype.format(o);
                }
            } else if (o instanceof Enum) {
                return messages.getMessage((Enum) o);
            } else if (o instanceof Entity) {
                return instanceNameProvider.getInstanceName((Entity) o);
            } else {
                return String.valueOf(o);
            }
        } else {
            return null;
        }
    }
}