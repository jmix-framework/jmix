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

package io.jmix.graphql.schema;

import io.jmix.graphql.NamingUtils;
import io.leangen.graphql.metadata.messages.MessageBundle;
import io.leangen.graphql.metadata.strategy.type.DefaultTypeInfoGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.AnnotatedType;

@Component
public class JmixTypeInfoGenerator extends DefaultTypeInfoGenerator {

    @Override
    public String generateInputTypeName(AnnotatedType type, MessageBundle messageBundle) {
        return NamingUtils.INPUT_TYPE_PREFIX + generateTypeName(type, messageBundle);
    }
}
