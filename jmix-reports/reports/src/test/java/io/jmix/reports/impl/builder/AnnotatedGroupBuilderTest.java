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

package io.jmix.reports.impl.builder;

import io.jmix.core.MetadataTools;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.outside_reports.CorrectReportGroup;
import io.jmix.outside_reports.WrongUuidReportGroup;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportSource;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.test_support.CurrentAuthenticationMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class AnnotatedGroupBuilderTest {

    @Autowired
    private AnnotatedGroupBuilder annotatedGroupBuilder;

    @Autowired
    private MetadataTools metadataTools;

    @TestBean(name = "core_CurrentAuthentication", methodName = "currentAuthenticationMock")
    private CurrentAuthentication currentAuthentication;

    static CurrentAuthentication currentAuthenticationMock() {
        return new CurrentAuthenticationMock();
    }

    @Test
    public void testSuccessfulGroupImport() {
        // given
        CorrectReportGroup definition = new CorrectReportGroup();

        // when
        ReportGroup group = annotatedGroupBuilder.createGroupFromDefinition(definition);

        // then
        assertThat(group).isNotNull();
        assertThat(group.getCode()).isEqualTo(CorrectReportGroup.CODE);
        assertThat(group.getId()).isEqualTo(UUID.fromString("12424a52-09fc-4de7-e08a-b8abf3155f15"));
        assertThat(group.getSource()).isEqualTo(ReportSource.ANNOTATED_CLASS);
    }

    @ParameterizedTest
    @CsvSource({"en,Test group", "fr,Groupe de test"})
    public void testLocalizedTitle(String localeCode, String expectedCaption) {
        // given
        ((CurrentAuthenticationMock) currentAuthentication).setLocale(Locale.forLanguageTag(localeCode));
        CorrectReportGroup definition = new CorrectReportGroup();

        // when
        ReportGroup group = annotatedGroupBuilder.createGroupFromDefinition(definition);

        // then
        assertThat(metadataTools.getInstanceName(group))
                .isEqualTo(expectedCaption);
    }

    @Test
    public void testTitleTakenFromDefaultLocale() {
        // given
        CorrectReportGroup definition = new CorrectReportGroup();

        // when
        ReportGroup group = annotatedGroupBuilder.createGroupFromDefinition(definition);

        // then
        assertThat(group.getTitle())
                .isEqualTo("Test group");
    }

    @Test
    public void testWrongUuid() {
        // given
        WrongUuidReportGroup definition = new WrongUuidReportGroup();

        // when+then
        assertThatThrownBy(() -> annotatedGroupBuilder.createGroupFromDefinition(definition))
                .isInstanceOf(InvalidReportDefinitionException.class)
                .hasMessageContaining("Invalid UUID");
    }
}
