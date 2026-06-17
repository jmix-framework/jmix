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

package io.jmix.aitools.dataload.introspection.model;

import java.util.List;
import java.util.Map;

/**
 * Description of an enum class used as a property type.
 */
public class EnumClassDescriptor {

    protected String name;

    protected List<String> localizedNames;

    protected Map<String, EnumValueDescriptor> constants;

    public EnumClassDescriptor(String name,
                               List<String> localizedNames,
                               Map<String, EnumValueDescriptor> constants) {
        this.name = name;
        this.localizedNames = localizedNames;
        this.constants = constants;
    }

    /**
     * Returns the simple name of the enum class.
     *
     * @return enum class name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the enum class captions for the configured locales.
     *
     * @return localized enum class names
     */
    public List<String> getLocalizedNames() {
        return localizedNames;
    }

    /**
     * Returns the enum constants keyed by constant name.
     *
     * @return enum constants by name
     */
    public Map<String, EnumValueDescriptor> getConstants() {
        return constants;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", localizedNames=" + localizedNames +
                ", constants=" + constants +
                '}';
    }
}
