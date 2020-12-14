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

package io.jmix.uidata.filter;


import io.jmix.core.Metadata;
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.component.Filter;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.LogicalFilterComponent;
import io.jmix.ui.component.Window;
import io.jmix.ui.component.filter.configuration.RunTimeConfiguration;
import io.jmix.ui.component.filter.converter.FilterConverter;
import io.jmix.ui.component.filter.registration.FilterComponents;
import io.jmix.ui.entity.GroupFilterCondition;
import io.jmix.ui.entity.LogicalFilterCondition;
import io.jmix.uidata.entity.FilterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Internal
@Component("ui_FilterConfigurationConverter")
public class FilterConfigurationConverter {

    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected FilterComponents filterComponents;

    public FilterConfiguration toFilterConfiguration(Filter.Configuration configuration) {
        FilterConfiguration filterConfiguration = metadata.create(FilterConfiguration.class);
        return toFilterConfiguration(configuration, filterConfiguration);
    }

    public FilterConfiguration toFilterConfiguration(Filter.Configuration configuration,
                                                     FilterConfiguration existedFilterConfiguration) {
        Filter filterComponent = configuration.getOwner();
        String filterComponentId = generateFilterComponentId(filterComponent);
        existedFilterConfiguration.setComponentId(filterComponentId);

        existedFilterConfiguration.setCode(configuration.getCode());
        existedFilterConfiguration.setUsername(currentAuthentication.getUser().getUsername());

        LogicalFilterComponent rootLogicalFilterComponent = configuration.getRootLogicalFilterComponent();
        FilterConverter converter = filterComponents.getConverterByComponentClass(rootLogicalFilterComponent.getClass(),
                configuration.getOwner());
        existedFilterConfiguration.setRootCondition(
                (GroupFilterCondition) converter.convertToModel(rootLogicalFilterComponent));

        return existedFilterConfiguration;
    }

    public Filter.Configuration toConfiguration(FilterConfiguration filterConfiguration, Filter filter) {
        String code = filterConfiguration.getCode();
        LogicalFilterCondition rootCondition = filterConfiguration.getRootCondition();
        FilterConverter converter = filterComponents.getConverterByModelClass(rootCondition.getClass(), filter);
        LogicalFilterComponent logicalFilterComponent = (LogicalFilterComponent) converter.convertToComponent(rootCondition);
        Filter.Configuration configuration = new RunTimeConfiguration(code, logicalFilterComponent, filter);
        return configuration;
    }

    public String generateFilterComponentId(Filter filter) {
        StringBuilder sb = new StringBuilder();
        Frame frame = filter.getFrame();
        while (frame != null) {
            String s = frame.getId() != null ? frame.getId() : "frameWithoutId";
            s = "[" + s + "]";
            sb.insert(0, s);
            if (frame instanceof Window) {
                break;
            }
            frame = frame.getFrame();
        }

        sb.append(".")
                .append(filter.getId() != null ? filter.getId() : "filterWithoutId");

        return sb.toString();
    }
}
