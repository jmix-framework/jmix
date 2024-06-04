/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.reports.yarg.util.converter;

import io.jmix.reports.yarg.exception.ReportingException;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class AbstractObjectToStringConverter implements ObjectToStringConverter {
    protected Object convertFromStringUnresolved(Class<?> parameterClass, String paramValueStr) {
        try {
            Constructor constructor = ConstructorUtils.getAccessibleConstructor(parameterClass, String.class);
            if (constructor != null) {
                return constructor.newInstance(paramValueStr);
            } else {
                Method valueOf = MethodUtils.getAccessibleMethod(parameterClass, "valueOf", String.class);
                if (valueOf != null) {
                    return valueOf.invoke(null, paramValueStr);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new ReportingException(
                    String.format("Could not instantiate object with class [%s] from [%s] string.",
                            parameterClass.getCanonicalName(),
                            paramValueStr));
        }
        return paramValueStr;
    }
}