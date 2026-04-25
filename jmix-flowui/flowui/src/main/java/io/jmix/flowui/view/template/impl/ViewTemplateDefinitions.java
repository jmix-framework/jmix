/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.view.template.impl;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.jmix.core.DevelopmentException;
import io.jmix.core.Metadata;
import io.jmix.core.Resources;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.template.DetailViewTemplate;
import io.jmix.flowui.view.template.ListViewTemplate;
import io.jmix.flowui.view.template.ViewTemplateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Loads and caches template-generated view definitions discovered in entity metadata.
 */
@Component("flowui_ViewTemplateDefinitions")
public class ViewTemplateDefinitions {

    protected static final Type TEMPLATE_PARAMS_TYPE = new TypeToken<Map<String, Object>>() {
    }.getType();

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected Resources resources;
    @Autowired
    protected ComponentXmlFactory componentXmlFactory;
    @Autowired
    protected ViewTemplateHelper templateHelper;
    @Autowired
    protected ViewTemplateDescriptorRegistry descriptorRegistry;
    @Autowired
    protected ViewTemplateControllerClassFactory controllerClassFactory;

    protected Gson gson = new Gson();
    protected Configuration freemarkerConfiguration;

    protected volatile List<ViewTemplateDefinition> definitions;

    @PostConstruct
    protected void init() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setDefaultEncoding("UTF-8");
        this.freemarkerConfiguration = configuration;
    }

    /**
     * Returns cached template view definitions, loading them lazily on the first call.
     *
     * @return immutable list of template view definitions
     */
    public List<ViewTemplateDefinition> getDefinitions() {
        if (definitions == null) {
            synchronized (this) {
                if (definitions == null) {
                    definitions = Collections.unmodifiableList(loadDefinitions());
                }
            }
        }

        return definitions;
    }

    protected List<ViewTemplateDefinition> loadDefinitions() {
        List<ViewTemplateDefinition> result = new ArrayList<>();
        Set<String> viewIds = new HashSet<>();

        metadata.getSession().getClasses().stream()
                .sorted(Comparator.comparing(MetaClass::getName))
                .forEach(metaClass -> {
                    getTemplateAttributes(metaClass, ListViewTemplate.class)
                            .ifPresent(attributes -> addDefinition(result, viewIds,
                                    createListDefinition(metaClass, attributes)));
                    getTemplateAttributes(metaClass, DetailViewTemplate.class)
                            .ifPresent(attributes -> addDefinition(result, viewIds,
                                    createDetailDefinition(metaClass, attributes)));
                });

        return result;
    }

    protected Optional<Map<String, Object>> getTemplateAttributes(MetaClass metaClass, Class<?> annotationClass) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes =
                (Map<String, Object>) metaClass.getAnnotations().get(annotationClass.getName());
        return Optional.ofNullable(attributes);
    }

    protected void addDefinition(List<ViewTemplateDefinition> definitions,
                                 Set<String> viewIds,
                                 ViewTemplateDefinition definition) {
        if (!viewIds.add(definition.getId())) {
            throw new IllegalStateException("View template with id '%s' is already registered"
                    .formatted(definition.getId()));
        }

        definitions.add(definition);
    }

    protected ViewTemplateDefinition createListDefinition(MetaClass metaClass, Map<String, Object> attributes) {
        String viewId = getStringAttribute(attributes, "viewId");
        if (Strings.isNullOrEmpty(viewId)) {
            viewId = metaClass.getName() + ".list";
        }

        String title = getStringAttribute(attributes, "viewTitle");
        if (Strings.isNullOrEmpty(title)) {
            title = metaClass.getName() + " list";
        }

        return createDefinition(viewId, ViewTemplateType.LIST, metaClass, attributes, title);
    }

    protected ViewTemplateDefinition createDetailDefinition(MetaClass metaClass, Map<String, Object> attributes) {
        String viewId = getStringAttribute(attributes, "viewId");
        if (Strings.isNullOrEmpty(viewId)) {
            viewId = metaClass.getName() + ".detail";
        }

        String title = getStringAttribute(attributes, "viewTitle");
        if (Strings.isNullOrEmpty(title)) {
            title = metaClass.getName();
        }

        return createDefinition(viewId, ViewTemplateType.DETAIL, metaClass, attributes, title);
    }

    protected ViewTemplateDefinition createDefinition(String viewId,
                                                     ViewTemplateType type,
                                                     MetaClass metaClass,
                                                     Map<String, Object> attributes,
                                                     String title) {
        String routePath = resolveRoutePath(viewId, type, attributes);
        String descriptor = renderTemplate(metaClass, attributes, title);
        String descriptorPath = descriptorRegistry.createPath(viewId);
        descriptorRegistry.put(descriptorPath, descriptor);

        return new ViewTemplateDefinition(
                viewId,
                type,
                metaClass,
                descriptorPath,
                routePath,
                controllerClassFactory.createControllerClass(metaClass, viewId, type, descriptorPath, routePath),
                title,
                getStringAttribute(attributes, "parentMenu")
        );
    }

    protected String resolveRoutePath(String viewId, ViewTemplateType type, Map<String, Object> attributes) {
        String configuredRoute = getStringAttribute(attributes, "viewRoute");
        if (Strings.isNullOrEmpty(configuredRoute)) {
            return controllerClassFactory.createDefaultRoutePath(viewId, type);
        }

        if (type == ViewTemplateType.DETAIL) {
            String routeParamSuffix = "/:" + StandardDetailView.DEFAULT_ROUTE_PARAM;
            if (configuredRoute.endsWith(routeParamSuffix)) {
                throw new IllegalArgumentException("Detail viewRoute must not end with '" + routeParamSuffix + "'");
            }

            return configuredRoute + routeParamSuffix;
        }

        return configuredRoute;
    }

    protected String renderTemplate(MetaClass metaClass, Map<String, Object> attributes, String title) {
        String templatePath = getStringAttribute(attributes, "path");
        String template = resources.getResourceAsString(templatePath);
        if (template == null) {
            throw new DevelopmentException("View template is not found", "Path", templatePath);
        }

        Map<String, Object> model = new HashMap<>(parseTemplateParams(getStringAttribute(attributes, "templateParams")));
        model.put("entityMetaClass", metaClass);
        model.put("viewTitle", title);
        model.put("componentXmlFactory", componentXmlFactory);
        model.put("templateHelper", templateHelper);

        try (StringWriter writer = new StringWriter()) {
            new Template(templatePath, new StringReader(template), freemarkerConfiguration)
                    .process(model, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read view template '%s'".formatted(templatePath), e);
        } catch (TemplateException e) {
            throw new IllegalArgumentException("Unable to render view template '%s'".formatted(templatePath), e);
        }
    }

    protected Map<String, Object> parseTemplateParams(String templateParams) {
        if (Strings.isNullOrEmpty(templateParams)) {
            return Collections.emptyMap();
        }

        JsonElement jsonElement = JsonParser.parseString(templateParams);
        if (!jsonElement.isJsonObject()) {
            throw new IllegalArgumentException("View template parameters must be a JSON object");
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return gson.fromJson(jsonObject, TEMPLATE_PARAMS_TYPE);
    }

    protected String getStringAttribute(Map<String, Object> attributes, String name) {
        Object value = attributes.get(name);
        return value instanceof String ? (String) value : "";
    }
}
