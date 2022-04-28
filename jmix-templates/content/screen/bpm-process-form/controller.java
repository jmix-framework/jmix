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

package ${packageName};

import io.jmix.bpmui.processform.ProcessFormContext;
<%if (controllerName != "ProcessForm") {
%>import io.jmix.bpmui.processform.annotation.ProcessForm;<%
    annotation = "ProcessForm"
} else {
    annotation = "io.jmix.bpmui.processform.annotation.ProcessForm"}
%>
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

<%if (classComment) {%>
${classComment}
<%}%>@$annotation
@UiController("${id}")
@UiDescriptor("${descriptorName}.xml")
public class ${controllerName} extends Screen {
    @Autowired
    private ProcessFormContext processFormContext;
}