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

package io.jmix.hibernate.impl.enhancing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.spi.ClassTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

public class JmixHibernateEnhancingClassTransformerImpl implements ClassTransformer {

    private static final Logger log = LoggerFactory.getLogger(JmixHibernateEnhancingClassTransformerImpl.class);

    protected ClassTransformer delegate;
    protected List<String> managedClassNames;

    public JmixHibernateEnhancingClassTransformerImpl(ClassTransformer delegate, List<String> managedClassNames) {
        this.delegate = delegate;
        this.managedClassNames = managedClassNames;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (className == null) {
            return null;
        }
        final String safeClassName = className.replace('/', '.');
        if (managedClassNames.contains(safeClassName)) {
            log.info("Enhance persistent class {}", safeClassName);
            return delegate.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        } else {
            return null;
        }
    }
}
