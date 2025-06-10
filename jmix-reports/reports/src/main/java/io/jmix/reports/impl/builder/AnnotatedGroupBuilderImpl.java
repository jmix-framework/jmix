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

import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.reports.annotation.ReportGroupDef;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportSource;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("reports_AnnotatedGroupBuilder")
public class AnnotatedGroupBuilderImpl implements AnnotatedGroupBuilder {

    protected final Metadata metadata;
    protected final AnnotatedBuilderUtils annotatedBuilderUtils;

    public AnnotatedGroupBuilderImpl(Metadata metadata, AnnotatedBuilderUtils annotatedBuilderUtils) {
        this.metadata = metadata;
        this.annotatedBuilderUtils = annotatedBuilderUtils;
    }

    @Override
    public ReportGroup createGroupFromDefinition(Object groupDefinition) {
        Class<?> definitionClass = groupDefinition.getClass();
        ReportGroupDef annotation = definitionClass.getAnnotation(ReportGroupDef.class);

        ReportGroup group = metadata.create(ReportGroup.class);
        group.setSource(ReportSource.ANNOTATED_CLASS);
        group.setCode(annotation.code());

        if (!annotation.uuid().isEmpty()) {
            try {
                group.setId(UUID.fromString(annotation.uuid()));
            } catch (IllegalArgumentException e) {
                throw new InvalidReportDefinitionException(e);
            }
        } // else keep random auto-generated id

        String titleValue = annotation.title();
        if (titleValue.startsWith(MessageTools.MARK)) {
            group.setTitleMessageKey(annotatedBuilderUtils.extractMessageKey(titleValue, definitionClass));
        } else {
            group.setTitle(titleValue);
        }

        return group;
    }
}
