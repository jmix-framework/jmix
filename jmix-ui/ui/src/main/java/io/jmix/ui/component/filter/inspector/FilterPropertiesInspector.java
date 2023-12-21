/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.component.filter.inspector;

import io.jmix.core.metamodel.model.MetaPropertyPath;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class FilterPropertiesInspector implements Predicate<MetaPropertyPath> {

    protected String includedPropertiesRegexp = ".*";
    protected String excludedPropertiesRegexp = "";
    protected List<String> excludedProperties = new ArrayList<>();
    protected List<String> includedProperties = new ArrayList<>();
    protected boolean excludeRecursively;

    public FilterPropertiesInspector() {
    }

    public String getIncludedPropertiesRegexp() {
        return includedPropertiesRegexp;
    }

    public void setIncludedPropertiesRegexp(String includedPropertiesRegexp) {
        this.includedPropertiesRegexp = includedPropertiesRegexp;
    }

    public String getExcludedPropertiesRegexp() {
        return excludedPropertiesRegexp;
    }

    public void setExcludedPropertiesRegexp(String excludedPropertiesRegexp) {
        this.excludedPropertiesRegexp = excludedPropertiesRegexp;
    }

    public List<String> getIncludedProperties() {
        return includedProperties;
    }

    public void setIncludedProperties(List<String> includedProperties) {
        this.includedProperties = includedProperties;
    }

    public List<String> getExcludedProperties() {
        return excludedProperties;
    }

    public void setExcludedProperties(List<String> excludedProperties) {
        this.excludedProperties = excludedProperties;
    }

    public boolean isExcludeRecursively() {
        return excludeRecursively;
    }

    public void setExcludeRecursively(boolean excludeRecursively) {
        this.excludeRecursively = excludeRecursively;
    }

    @Override
    public boolean test(MetaPropertyPath metaPropertyPath) {
        String propertyName = metaPropertyPath.toPathString();

        boolean propertyIncluded = true;
        if (includedPropertiesRegexp != null) {
            Pattern includePattern = Pattern.compile(includedPropertiesRegexp.replace(" ", ""));
            propertyIncluded = includePattern.matcher(propertyName).matches();
        }

        boolean propertyExcluded = false;
        if (excludedPropertiesRegexp != null) {
            Pattern excludePattern = Pattern.compile(excludedPropertiesRegexp.replace(" ", ""));
            propertyExcluded = excludePattern.matcher(propertyName).matches();
        }

        if (!propertyExcluded
                && excludedProperties != null) {
            propertyExcluded = excludedProperties.contains(propertyName)
                    || (excludeRecursively && excludedProperties.contains(metaPropertyPath.getMetaProperty().getName()));
        }

        return propertyIncluded && !propertyExcluded;
    }
}
