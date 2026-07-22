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

package io.jmix.aitools.tool.impl;

import io.jmix.aitools.tool.AiToolDescriptor;
import io.jmix.aitools.tool.AiToolDescriptorProvider;
import io.jmix.aitools.tool.AiToolRegistry;
import io.jmix.aitools.tool.JmixAiTool;
import io.jmix.aitools.tool.ResolvedAiTool;
import io.jmix.aitools.tool.ToolOverride;
import io.jmix.core.common.util.Preconditions;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Default {@link AiToolRegistry}. On startup:
 * <ul>
 *     <li>
 *         it scans all {@link JmixAiTool} beans for {@code @Tool}-annotated methods
 *     </li>
 *     <li>
 *         resolves {@link ToolOverride @ToolOverride} conflicts
 *     </li>
 *     <li>
 *         publishes an immutable set of {@link ResolvedAiTool}s.
 *     </li>
 * </ul>
 */
@Component("aitls_AiToolRegistryImpl")
public class AiToolRegistryImpl implements AiToolRegistry, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(AiToolRegistryImpl.class);

    @Autowired(required = false)
    protected List<JmixAiTool> aiTools = List.of();

    @Autowired
    protected AiToolDescriptorProvider aiToolDescriptorProvider;

    protected volatile ResolvedAiTools resolvedAiTools = ResolvedAiTools.EMPTY;

    @Override
    public void afterPropertiesSet() {
        refresh();
    }

    protected synchronized void refresh() {
        List<JmixAiTool> uniqueTools = deduplicate(aiTools);

        // Collect candidates, splitting them into regular and overrides.
        // Overrides are routed into the bucket of their target tool name; regular candidates use
        // their own @Tool name as the bucket key. This way an override and its target end up in
        // the same bucket and can be resolved together, while a dangling override (no target)
        // still gets attached to a meaningful name afterwards.
        Map<String, List<ToolCandidate>> regulars = new LinkedHashMap<>();
        Map<String, List<ToolCandidate>> overrideBuckets = new LinkedHashMap<>();
        for (JmixAiTool tool : uniqueTools) {
            collectCandidates(tool, regulars, overrideBuckets);
        }

        // Merge each override bucket into the regulars map. Dangling overrides (no
        // matching target in regulars) fall back to their own @Tool name with a warning, unless
        // that name is already taken by a regular tool — in which case the fallback would silently
        // replace an unrelated tool, so we fail loudly instead.
        Set<String> regularNamesBeforeMerge = Set.copyOf(regulars.keySet());
        for (Map.Entry<String, List<ToolCandidate>> entry : overrideBuckets.entrySet()) {
            String targetName = entry.getKey();
            List<ToolCandidate> overrides = entry.getValue();
            if (regulars.containsKey(targetName)) {
                regulars.get(targetName).addAll(overrides);
            } else {
                for (ToolCandidate dangling : overrides) {
                    String fallbackName = dangling.descriptor.getName();
                    if (regularNamesBeforeMerge.contains(fallbackName)) {
                        throw new IllegalStateException(String.format(
                                "Tool method '%s#%s' declares @%s(\"%s\") but no tool with that name exists, " +
                                        "and its own @Tool name '%s' is already used by another tool. " +
                                        "Either fix the @%s target or rename the @Tool.",
                                getTargetClass(dangling.source).getName(), dangling.method.getName(),
                                ToolOverride.class.getSimpleName(), dangling.overrideName,
                                fallbackName,
                                ToolOverride.class.getSimpleName()));
                    }
                    log.warn("Tool method '{}#{}' declares @{}(\"{}\") but no tool with that name exists. " +
                                    "Treating it as a new tool registered under name '{}'.",
                            getTargetClass(dangling.source).getName(), dangling.method.getName(),
                            ToolOverride.class.getSimpleName(),
                            dangling.overrideName, fallbackName);
                    regulars.computeIfAbsent(fallbackName, k -> new ArrayList<>())
                            .add(dangling);
                }
            }
        }

        // Resolve winner per name and build the resolved entry. Markers are unioned
        // across all candidates in the bucket, so an override does not strip a tool of marker
        // memberships that the original tool had.
        List<ResolvedAiTool> result = new ArrayList<>(regulars.size());
        for (Map.Entry<String, List<ToolCandidate>> entry : regulars.entrySet()) {
            String registryKey = entry.getKey();
            List<ToolCandidate> bucket = entry.getValue();
            ToolCandidate winner = resolveWinner(registryKey, bucket);
            Set<Class<? extends JmixAiTool>> mergedMarkers = unionMarkers(bucket);
            result.add(buildResolvedTool(registryKey, winner, mergedMarkers));
        }

        List<ResolvedAiTool> resolvedList = List.copyOf(result);

        Map<String, ResolvedAiTool> index = new LinkedHashMap<>();
        for (ResolvedAiTool t : resolvedList) {
            index.put(t.getName(), t);
        }
        Map<String, ResolvedAiTool> byNameMap = Map.copyOf(index);

        List<ToolCallback> callbacks = new ArrayList<>(resolvedList.size());
        for (ResolvedAiTool t : resolvedList) {
            callbacks.add(t.getCallback());
        }
        List<ToolCallback> allCallbacks = List.copyOf(callbacks);

        // Publish the new state as a single volatile write so concurrent readers always see
        // resolvedTools/toolsByName/allToolCallbacks from the same refresh invocation, never a torn mix.
        this.resolvedAiTools = new ResolvedAiTools(resolvedList, byNameMap, allCallbacks);

        log.debug("AI tool registry rebuilt with {} tool(s): {}", resolvedList.size(), byNameMap.keySet());
    }

    @Override
    public List<ResolvedAiTool> getAll() {
        return resolvedAiTools.resolvedTools();
    }

    @Override
    public Optional<ResolvedAiTool> findByName(String name) {
        return Optional.ofNullable(resolvedAiTools.toolsByName().get(name));
    }

    @Override
    public List<ResolvedAiTool> findByMarker(Class<? extends JmixAiTool> marker) {
        Preconditions.checkNotNullArgument(marker);

        List<ResolvedAiTool> filtered = new ArrayList<>();
        for (ResolvedAiTool t : resolvedAiTools.resolvedTools()) {
            if (t.getMarkers().contains(marker)) {
                filtered.add(t);
            }
        }
        return List.copyOf(filtered);
    }

    @Override
    public List<ToolCallback> getAllCallbacks() {
        return resolvedAiTools.allToolCallbacks();
    }

    /**
     * Removes references to the same bean instance appearing multiple times in the autowired list
     * (this can happen when a single bean is exposed via several {@link JmixAiTool} sub-interfaces
     * and Spring resolves the injection through each of them). Deduplication is based on object
     * identity, so multiple distinct beans of the same class are preserved.
     *
     * @param tools autowired tool beans, possibly containing the same instance more than once
     * @return tools with duplicate instances removed, preserving first-seen order
     */
    protected List<JmixAiTool> deduplicate(List<JmixAiTool> tools) {
        Set<JmixAiTool> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<JmixAiTool> unique = new ArrayList<>(tools.size());
        for (JmixAiTool tool : tools) {
            if (seen.add(tool)) {
                unique.add(tool);
            } else {
                log.debug("AI tool bean '{}' is autowired multiple times (likely implements several {} sub-interfaces). " +
                                "It will be included only once in the registry.",
                        getTargetClass(tool).getName(), JmixAiTool.class.getSimpleName());
            }
        }
        return unique;
    }

    protected void collectCandidates(JmixAiTool tool,
                                     Map<String, List<ToolCandidate>> regulars,
                                     Map<String, List<ToolCandidate>> overrideBuckets) {
        Class<?> targetClass = getTargetClass(tool);

        Set<Method> toolMethods = MethodIntrospector.selectMethods(targetClass, this::isToolAnnotatedMethod);

        for (Method method : toolMethods) {
            AiToolDescriptor descriptor = aiToolDescriptorProvider.getDescriptor(tool, method);
            String overrideName = readOverrideValue(method);
            ToolCandidate candidate = new ToolCandidate(tool, method, descriptor, overrideName);
            if (overrideName != null) {
                overrideBuckets.computeIfAbsent(overrideName, k -> new ArrayList<>())
                        .add(candidate);
            } else {
                regulars.computeIfAbsent(descriptor.getName(), k -> new ArrayList<>())
                        .add(candidate);
            }
        }
    }

    protected ToolCandidate resolveWinner(String name, List<ToolCandidate> candidates) {
        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        List<ToolCandidate> overrides = candidates.stream()
                .filter(c -> c.overrideName != null)
                .toList();

        if (overrides.isEmpty()) {
            throw new IllegalStateException("Multiple AI tools share the same name '" + name + "': " +
                    candidates.stream()
                            .map(c -> getTargetClass(c.source).getName() + "#" + c.method.getName())
                            .toList());
        }
        if (overrides.size() > 1) {
            // The bucket key 'name' is not necessarily the @ToolOverride value: for dangling
            // overrides, the bucket key is the fallback (own @Tool.name), while each method's
            // actual @ToolOverride target may be entirely different. Quote each candidate's own
            // @ToolOverride value so the user can navigate straight to the offending lines.
            throw new IllegalStateException("Multiple AI tool methods resolve to the same name '" + name + "': " +
                    overrides.stream()
                            .map(c -> getTargetClass(c.source).getName() + "#" + c.method.getName()
                                    + " (@ToolOverride(\"" + c.overrideName + "\"))")
                            .toList());
        }

        ToolCandidate winner = overrides.get(0);
        List<ToolCandidate> losers = candidates.stream().filter(c -> c != winner).toList();
        for (ToolCandidate loser : losers) {
            log.debug("AI tool '{}' (method {}#{}) is overridden by {}#{}",
                    name,
                    getTargetClass(loser.source).getName(), loser.method.getName(),
                    getTargetClass(winner.source).getName(), winner.method.getName());
        }
        return winner;
    }

    /**
     * Union of marker sub-interfaces from every candidate in the bucket. Ensures that an override
     * does not silently drop a tool from a {@link AiToolRegistry#findByMarker(Class) marker-specific}
     * view that the original implementation participated in.
     *
     * @param candidates all candidates grouped under the same tool name
     * @return union of marker sub-interfaces across the candidates' source beans
     */
    protected Set<Class<? extends JmixAiTool>> unionMarkers(List<ToolCandidate> candidates) {
        Set<Class<? extends JmixAiTool>> markers = new LinkedHashSet<>();
        for (ToolCandidate candidate : candidates) {
            markers.addAll(collectMarkers(candidate.source));
        }
        return markers;
    }

    /**
     * Builds the resolved tool entry. The {@code registryKey} is the bucket name under which all
     * candidates were grouped: it is the override target when the winner is an override resolved
     * against an existing original, or the winner's own {@code @Tool} name otherwise. Either way,
     * this is the name the LLM will see, so we use it both as the {@link ResolvedAiTool#getName()
     * registry name} and as the underlying {@link ToolDefinition} name.
     * <p>
     * The {@link ToolCallback#getToolMetadata() metadata} (description and other attributes) comes
     * from the winner's own {@code @Tool} annotation via {@link AiToolDescriptor}, which means an
     * override author can change the description while keeping the tool name stable.
     *
     * @param registryKey the name under which the tool is registered and exposed to the LLM
     * @param candidate   the winning candidate the entry is built from
     * @param markers     marker sub-interfaces this tool participates in
     * @return the resolved tool entry
     */
    protected ResolvedAiTool buildResolvedTool(String registryKey,
                                               ToolCandidate candidate,
                                               Set<Class<? extends JmixAiTool>> markers) {
        ToolDefinition toolDefinition = ToolDefinitions.builder(candidate.method)
                .name(registryKey)
                .description(candidate.descriptor.getDescription())
                .build();

        ToolCallback callback = MethodToolCallback.builder()
                .toolDefinition(toolDefinition)
                .toolMetadata(ToolMetadata.from(candidate.method))
                .toolMethod(candidate.method)
                .toolObject(candidate.source)
                .toolCallResultConverter(ToolUtils.getToolCallResultConverter(candidate.method))
                .build();

        return new ResolvedAiTool(
                registryKey,
                candidate.descriptor.getDescription(),
                callback,
                candidate.source,
                candidate.method,
                markers
        );
    }

    protected Set<Class<? extends JmixAiTool>> collectMarkers(JmixAiTool tool) {
        Class<?> targetClass = getTargetClass(tool);
        Set<Class<? extends JmixAiTool>> markers = new LinkedHashSet<>();
        collectMarkersRecursive(targetClass, markers);
        return markers;
    }

    @SuppressWarnings("unchecked")
    protected void collectMarkersRecursive(@Nullable Class<?> type, Set<Class<? extends JmixAiTool>> acc) {
        if (type == null || type == Object.class) {
            return;
        }
        for (Class<?> iface : type.getInterfaces()) {
            if (JmixAiTool.class.isAssignableFrom(iface)) {
                acc.add((Class<? extends JmixAiTool>) iface);
                collectMarkersRecursive(iface, acc);
            }
        }
        collectMarkersRecursive(type.getSuperclass(), acc); // type.getSuperclass() can return null
    }

    protected boolean isToolAnnotatedMethod(Method method) {
        return AnnotationUtils.findAnnotation(method, Tool.class) != null;
    }

    @Nullable
    protected String readOverrideValue(Method method) {
        ToolOverride override = AnnotationUtils.findAnnotation(method, ToolOverride.class);
        if (override == null) {
            return null;
        }
        String value = override.value();
        return value.isBlank() ? null : value;
    }

    protected Class<?> getTargetClass(JmixAiTool tool) {
        return AopUtils.isAopProxy(tool) ? AopUtils.getTargetClass(tool) : tool.getClass();
    }

    /**
     * Immutable consistent registry state.
     *
     * @param resolvedTools    all resolved tools in registration order
     * @param toolsByName      resolved tools indexed by their registry name
     * @param allToolCallbacks callbacks of all resolved tools, in the same order as {@code resolvedTools}
     */
    protected record ResolvedAiTools(List<ResolvedAiTool> resolvedTools,
                                     Map<String, ResolvedAiTool> toolsByName,
                                     List<ToolCallback> allToolCallbacks) {

        protected static final ResolvedAiTools EMPTY = new ResolvedAiTools(List.of(), Map.of(), List.of());

        protected ResolvedAiTools(List<ResolvedAiTool> resolvedTools,
                                  Map<String, ResolvedAiTool> toolsByName,
                                  List<ToolCallback> allToolCallbacks) {
            this.resolvedTools = Collections.unmodifiableList(resolvedTools);
            this.toolsByName = Collections.unmodifiableMap(toolsByName);
            this.allToolCallbacks = Collections.unmodifiableList(allToolCallbacks);
        }
    }

    /**
     * Internal candidate gathered during registry build, before override resolution.
     *
     * @param source       the tool bean declaring the method
     * @param method       the {@code @Tool}-annotated method
     * @param descriptor   the resolved name and description
     * @param overrideName the {@link ToolOverride @ToolOverride} target name, or {@code null} for a regular tool
     */
    protected record ToolCandidate(JmixAiTool source,
                                   Method method,
                                   AiToolDescriptor descriptor,
                                   @Nullable String overrideName) {

        protected ToolCandidate(JmixAiTool source, Method method, AiToolDescriptor descriptor, @Nullable String overrideName) {
            this.source = Objects.requireNonNull(source);
            this.method = Objects.requireNonNull(method);
            this.descriptor = Objects.requireNonNull(descriptor);
            this.overrideName = overrideName;
        }
    }
}
