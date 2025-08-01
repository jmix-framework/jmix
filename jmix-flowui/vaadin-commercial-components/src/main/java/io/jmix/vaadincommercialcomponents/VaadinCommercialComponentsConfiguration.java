/*
 * Copyright 2025 Haulmont.
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

package io.jmix.vaadincommercialcomponents;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.board.Row;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import io.jmix.core.annotation.JmixModule;
import io.jmix.flowui.FlowuiConfiguration;
import io.jmix.flowui.sys.registration.ComponentRegistration;
import io.jmix.flowui.sys.registration.ComponentRegistrationBuilder;
import io.jmix.vaadincommercialcomponents.component.loader.BoardLoader;
import io.jmix.vaadincommercialcomponents.component.loader.RowLoader;
import io.jmix.vaadincommercialcomponents.component.loader.SpreadsheetLoader;
import io.jmix.vaadincommercialcomponents.component.spreadsheet.JmixSpreadsheet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@JmixModule(dependsOn = FlowuiConfiguration.class)
public class VaadinCommercialComponentsConfiguration {

    @Bean
    public ComponentRegistration vaadinBoard() {
        return ComponentRegistrationBuilder.create(Board.class)
                .withComponentLoader("board", BoardLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration vaadinBoardRow() {
        return ComponentRegistrationBuilder.create(Row.class)
                .withComponentLoader("boardRow", RowLoader.class)
                .build();
    }

    @Bean
    public ComponentRegistration vaadinSpreadsheet() {
        return ComponentRegistrationBuilder.create(JmixSpreadsheet.class)
                .replaceComponent(Spreadsheet.class)
                .withComponentLoader("spreadsheet", SpreadsheetLoader.class)
                .build();
    }
}
