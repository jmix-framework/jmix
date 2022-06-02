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

package io.jmix.autoconfigure.emailtemplatesui;

import io.jmix.emailtemplates.EmailTemplatesConfiguration;
import io.jmix.emailtemplatesui.EmailTemplatesUiConfiguration;
import io.jmix.grapesjs.GrapesJsConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({EmailTemplatesConfiguration.class, EmailTemplatesUiConfiguration.class, GrapesJsConfiguration.class})
public class EmailTemplatesUiAutoConfiguration {
}
