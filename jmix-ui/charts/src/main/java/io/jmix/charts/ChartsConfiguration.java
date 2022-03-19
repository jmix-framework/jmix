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

package io.jmix.charts;

import io.jmix.charts.component.*;
import io.jmix.charts.component.impl.*;
import io.jmix.charts.loader.*;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.sys.UiControllersConfiguration;
import io.jmix.ui.sys.registration.ComponentRegistration;
import io.jmix.ui.sys.registration.ComponentRegistrationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@ComponentScan
@JmixModule(dependsOn = UiConfiguration.class)
public class ChartsConfiguration {

    @Bean("ui_ChartsControllers")
    public UiControllersConfiguration screens(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers
                = new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("io.jmix.charts.screen"));
        return uiControllers;
    }

    @Bean
    public ComponentRegistration gaugeChart() {
        return ComponentRegistrationBuilder.create(AngularGaugeChart.NAME)
                .withComponentClass(AngularGaugeChartImpl.class)
                .withComponentLoaderClass(AngularGaugeChartLoader.class)
                .withTag("gaugeChart")
                .build();
    }

    @Bean
    public ComponentRegistration funnelChart() {
        return ComponentRegistrationBuilder.create(FunnelChart.NAME)
                .withComponentClass(FunnelChartImpl.class)
                .withComponentLoaderClass(FunnelChartLoader.class)
                .withTag("funnelChart")
                .build();
    }

    @Bean
    public ComponentRegistration pieChart() {
        return ComponentRegistrationBuilder.create(PieChart.NAME)
                .withComponentClass(PieChartImpl.class)
                .withComponentLoaderClass(PieChartLoader.class)
                .withTag("pieChart")
                .build();
    }

    @Bean
    public ComponentRegistration radarChart() {
        return ComponentRegistrationBuilder.create(RadarChart.NAME)
                .withComponentClass(RadarChartImpl.class)
                .withComponentLoaderClass(RadarChartLoader.class)
                .withTag("radarChart")
                .build();
    }

    @Bean
    public ComponentRegistration serialChart() {
        return ComponentRegistrationBuilder.create(SerialChart.NAME)
                .withComponentClass(SerialChartImpl.class)
                .withComponentLoaderClass(SerialChartLoader.class)
                .withTag("serialChart")
                .build();
    }

    @Bean
    public ComponentRegistration ganttChart() {
        return ComponentRegistrationBuilder.create(GanttChart.NAME)
                .withComponentClass(GanttChartImpl.class)
                .withComponentLoaderClass(GanttChartLoader.class)
                .withTag("ganttChart")
                .build();
    }

    @Bean
    public ComponentRegistration xyChart() {
        return ComponentRegistrationBuilder.create(XYChart.NAME)
                .withComponentClass(XYChartImpl.class)
                .withComponentLoaderClass(XYChartLoader.class)
                .withTag("xyChart")
                .build();
    }

    @Bean
    public ComponentRegistration stockChart() {
        return ComponentRegistrationBuilder.create(StockChart.NAME)
                .withComponentClass(StockChartImpl.class)
                .withComponentLoaderClass(StockChartLoader.class)
                .withTag("stockChart")
                .build();
    }

    @Bean
    public ComponentRegistration customChart() {
        return ComponentRegistrationBuilder.create(CustomChart.NAME)
                .withComponentClass(CustomChartImpl.class)
                .withComponentLoaderClass(CustomChartLoader.class)
                .withTag("customChart")
                .build();
    }
}
