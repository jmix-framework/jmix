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

package io.jmix.reports.parameter;

import io.jmix.core.Metadata;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportInputParameter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
@TestPropertySource(properties = "jmix.reports.use-legacy-date-time-types=true")
public class ParameterDateTimeLegacyTest {

    @Autowired
    protected ParameterClassResolver parameterClassResolver;

    @Autowired
    protected Metadata metadata;

    @Test
    public void testLegacyTypes() {
        assertEquals(Date.class, parameterClassResolver.resolveClass(createParameter(ParameterType.DATE)));
        assertEquals(Date.class, parameterClassResolver.resolveClass(createParameter(ParameterType.DATETIME)));
        assertEquals(Date.class, parameterClassResolver.resolveClass(createParameter(ParameterType.TIME)));
    }

    protected ReportInputParameter createParameter(ParameterType type) {
        ReportInputParameter parameter = metadata.create(ReportInputParameter.class);
        parameter.setType(type);
        return parameter;
    }
}
