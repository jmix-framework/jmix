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

package lazy_loading;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.DisabledIf;

/**
 * Runs {@link InstanceNameReferenceLazyLoadingTest} with wrapping of excessively loaded properties disabled, as that
 * wrapping would hide a load listener that does not treat the instance name reference as fetched by the fetch plan.
 */
@TestPropertySource(properties = "jmix.eclipselink.wrap-excessively-loaded-properties=false")
@DisabledIf(expression = "#{systemEnvironment['JMIX_ECLIPSELINK_DISABLELAZYLOADING'] == 'true'}")
public class InstanceNameReferenceNoWrappingLazyLoadingTest extends InstanceNameReferenceLazyLoadingTest {
}
