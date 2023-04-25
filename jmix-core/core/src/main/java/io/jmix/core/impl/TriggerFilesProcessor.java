/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.core.impl;

import com.google.common.collect.Iterables;
import io.jmix.core.CoreProperties;
import io.jmix.core.common.util.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("core_TriggerFilesProcessor")
public class TriggerFilesProcessor implements ApplicationContextAware {
    @Autowired
    protected CoreProperties coreProperties;
    protected ApplicationContext applicationContext;

    private final Logger log = LoggerFactory.getLogger(TriggerFilesProcessor.class);
    protected static final Pattern FILE_NAME_PATTERN = Pattern.compile("(.+?)#(.+?)(\\(.+?\\))?$");

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void init() {
        if (coreProperties.isTriggerFilesEnabled()) {
            List<Path> paths = findTriggerFiles();

            for (Path path : paths) {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    log.error("Unable to delete trigger file {}", path);
                }
            }
        }
    }

    public void process() {
        if (coreProperties.isTriggerFilesEnabled()) {
            log.trace("Processing trigger files");

            for (Path path : findTriggerFiles()) {
                if (Files.isDirectory(path)) {
                    continue;
                }

                Path fileName = path.getFileName();
                if (fileName == null) {
                    continue;
                }

                try {
                    processFile(fileName.toString());
                } catch (Exception e) {
                    log.error("Trigger file {} processing error: {}", path, e);
                }

                try {
                    Files.delete(path);
                } catch (IOException e) {
                    log.error("Unable to delete trigger file {}", path);
                }
            }
        }
    }

    protected void processFile(String fileName) throws Exception {
        Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
        if (matcher.find()) {
            String beanClassName = matcher.group(1);
            String methodName = matcher.group(2);
            String paramsStr = matcher.groupCount() < 3 ? null : matcher.group(3);

            String[] paramsArray = null;
            Class<?>[] typesArray = null;

            if (paramsStr != null) {
                paramsArray = paramsStr.substring(1, paramsStr.length() - 1).split(",");
                typesArray = new Class[paramsArray.length];

                for (int i = 0, paramsArrayLength = paramsArray.length; i < paramsArrayLength; i++) {
                    String param = paramsArray[i];
                    typesArray[i] = String.class;
                    paramsArray[i] = param.replace("'", "");
                }
            }

            Object bean = getBean(beanClassName);
            if (bean == null) {
                return;
            }

            Class<?> beanClass = bean.getClass();
            Method method = typesArray == null ?
                    beanClass.getMethod(methodName) :
                    beanClass.getMethod(methodName, typesArray);

            log.info("Calling {}", fileName);

            Object result = paramsArray == null ?
                    method.invoke(bean) :
                    method.invoke(bean, (Object[]) paramsArray);
            log.debug("Result {}", result);
        }
    }

    @Nullable
    protected Object getBean(String beanClassName) {
        Object bean;
        try {
            bean = applicationContext.getBean(ReflectionHelper.loadClass(beanClassName));
        } catch (NoSuchBeanDefinitionException | ClassNotFoundException e) {
            log.warn("Bean \"{}\" is not found.", beanClassName);
            return null;
        }
        return bean;
    }

    protected List<Path> findTriggerFiles() {
        List<Path> paths = new ArrayList<>();

        Path triggersDir = Paths.get(coreProperties.getTempDir(), "triggers");
        if (Files.exists(triggersDir)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(triggersDir)) {
                Iterables.addAll(paths, directoryStream);
            } catch (IOException e) {
                log.error("Unable to read trigger files:", e);
                return paths;
            }
        }

        return paths;
    }
}