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
import freemarker.core.TemplateClassResolver;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.jmix.core.DevelopmentException;
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.Resources;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
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
    protected static final String LOOKUP_COMPONENT_ID = "lookupComponentId";
    protected static final String EDITED_ENTITY_CONTAINER_ID = "editedEntityContainerId";

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
        // Disable the ?new / ?api built-ins so a template body cannot instantiate or reflect over
        // arbitrary classes. View templates only need plain data rendering.
        configuration.setNewBuiltinClassResolver(TemplateClassResolver.ALLOWS_NOTHING_RESOLVER);
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

        return Objects.requireNonNull(definitions);
    }

    /**
     * Re-renders all template view descriptors from current metadata and updates the descriptor
     * registry. Used when runtime metadata changes after startup so that template-generated views
     * reflect the current entity properties.
     */
    public void refresh() {
        synchronized (this) {
            definitions = Collections.unmodifiableList(loadDefinitions());
        }
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
        title = resolveTitleMessageReference(metaClass, title);

        String routePath = resolveRoutePath(viewId, type, attributes);
        String templatePath = getStringAttribute(attributes, "path");
        Map<String, Object> templateParams = parseTemplateParams(getStringAttribute(attributes, "templateParams"));
        String descriptor = renderTemplate(metaClass, templatePath, title, templateParams);
        String descriptorPath = descriptorRegistry.createPath(viewId);
        descriptorRegistry.put(descriptorPath, descriptor);

        Class<? extends View<?>> controllerClass;
        if (type == ViewTemplateType.LIST) {
            controllerClass = controllerClassFactory.createListViewControllerClass(
                    metaClass,
                    viewId,
                    descriptorPath,
                    routePath,
                    resolveLookupComponentId(attributes)
            );
        } else {
            controllerClass = controllerClassFactory.createDetailViewControllerClass(
                    metaClass,
                    viewId,
                    descriptorPath,
                    routePath,
                    resolveEditedEntityContainerId(attributes)
            );
        }

        return new ViewTemplateDefinition(
                viewId,
                type,
                metaClass,
                descriptorPath,
                routePath,
                controllerClass,
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

    protected String renderTemplate(MetaClass metaClass,
                                    String templatePath,
                                    String title,
                                    Map<String, Object> templateParams) {
        String template = resources.getResourceAsString(templatePath);
        if (template == null) {
            throw new DevelopmentException("View template is not found", "Path", templatePath);
        }

        Map<String, Object> model = new HashMap<>(templateParams);
        model.remove(LOOKUP_COMPONENT_ID);
        model.remove(EDITED_ENTITY_CONTAINER_ID);
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

    protected String resolveLookupComponentId(Map<String, Object> attributes) {
        return resolveControllerIdAttribute(attributes, LOOKUP_COMPONENT_ID, TemplateListView.DEFAULT_LOOKUP_COMPONENT_ID);
    }

    protected String resolveEditedEntityContainerId(Map<String, Object> attributes) {
        return resolveControllerIdAttribute(attributes, EDITED_ENTITY_CONTAINER_ID,
                TemplateDetailView.DEFAULT_EDITED_ENTITY_CONTAINER_ID);
    }

    protected String resolveControllerIdAttribute(Map<String, Object> attributes,
                                                  String attributeName,
                                                  String defaultValue) {
        String value = getStringAttribute(attributes, attributeName);
        if (Strings.isNullOrEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Normalizes a {@code viewTitle} defined as a message reference so that it is resolved
     * through the {@code Messages} bean by the framework. A brief reference {@code msg://message_id}
     * is expanded to the full form {@code msg://group/message_id} using the entity package as the
     * message group. The reference itself is kept intact so the title is resolved per locale later.
     */
    protected String resolveTitleMessageReference(MetaClass metaClass, String title) {
        if (!title.startsWith(MessageTools.MARK)) {
            return title;
        }

        String reference = title.substring(MessageTools.MARK.length());
        if (reference.contains("/")) {
            return title;
        }

        String group = metaClass.getJavaClass().getPackageName();
        return MessageTools.MARK + group + "/" + reference;
    }

    protected String getStringAttribute(Map<String, Object> attributes, String name) {
        Object value = attributes.get(name);
        return value instanceof String ? (String) value : "";
    }
}
