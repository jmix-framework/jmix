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
import org.springframework.stereotype.Component;

@Component("reports_AnnotatedBuilderUtils")
public class AnnotatedBuilderUtils {

    /**
     * Extract message key from the message reference.
     * Reference can be in one of three formats, like in view descriptors:
     * <li>with implicit group - starting with msg://, group must be taken from<code>defaultGroup</code></li>
     * <li>with explicitly specified group: msg://group/key</li>
     * <li>without a group, starting with msg:///</li>
     * @param reference message reference in one of 3 formats
     * @param declarationOwner class defining default group for the message - which is its package name
     *
     * @return message key ready to be passed to {@link io.jmix.core.Messages#getMessage(String)}
     */
    public String extractMessageKey(String reference, Class<?> declarationOwner) {
        if (!reference.startsWith(MessageTools.MARK)) {
            return reference;
        }
        String path = reference.substring(MessageTools.MARK.length());
        if (path.startsWith("/")) {
            // message without a group
            return path.substring(1);
        } else if (path.contains("/")) {
            // reference already contains group, keep as is
            return path;
        } else {
            // add default message group
            return getPackage(declarationOwner) + "/" + path;
        }
    }

    // copy of io.jmix.flowui.view.ViewControllerUtils.getPackage
    protected String getPackage(Class<?> ownerClass) {
        Package javaPackage = ownerClass.getPackage();
        if (javaPackage != null) {
            return javaPackage.getName();
        }

        // infer from FQN, hot-deployed classes do not have package
        // see JDK-8189231
        String canonicalName = ownerClass.getCanonicalName();
        int dotIndex = canonicalName.lastIndexOf('.');

        if (dotIndex >= 0) {
            return canonicalName.substring(0, dotIndex);
        }

        return "";
    }
}
