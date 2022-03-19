/*
 * Copyright 2019 Haulmont.
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

package io.jmix.gradle.ui;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.util.PatternFilterable;

import java.io.File;
import java.util.*;

public abstract class WidgetsTask extends DefaultTask {

    protected static final String MAIN_SOURCE_SET = "main";

    @Internal
    protected List<String> excludes = new ArrayList<>();

    @Internal
    protected List<String> excludePaths = new ArrayList<>();

    @Internal
    protected List<String> includePaths = new ArrayList<>();

    @Internal
    protected Set<String> compilerJvmArgs = new LinkedHashSet<>(Collections.singleton("-Djava.awt.headless=true"));

    public void excludeJars(String... artifacts) {
        excludes.addAll(Arrays.asList(artifacts));
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    /**
     * Set the allowable exclude patterns.
     *
     * @param excludes an Iterable providing new exclude patterns
     * @see PatternFilterable Pattern Format
     */
    public void excludePaths(String... excludes) {
        excludePaths.addAll(Arrays.asList(excludes));
    }

    public List<String> getIncludePaths() {
        return includePaths;
    }

    /**
     * Set the allowable include patterns.
     *
     * @param includes an Iterable providing new include patterns
     * @see PatternFilterable Pattern Format
     */
    public void includePaths(String... includes) {
        this.includePaths.addAll(Arrays.asList(includes));
    }

    public void jvmArgs(String... jvmArgs) {
        compilerJvmArgs.addAll(Arrays.asList(jvmArgs));
    }

    public Set<String> getCompilerJvmArgs() {
        return compilerJvmArgs;
    }

    protected Collection<File> getClassesDirs(SourceSet sourceSet) {
        return sourceSet.getOutput().getClassesDirs().getFiles();
    }

    protected void collectProjectsWithDependency(Project project, String dependencyName, Set<Project> explored) {
        Configuration compileConfiguration = project.getConfigurations().findByName("compileClasspath");
        if (compileConfiguration != null) {
            for (Dependency dependencyItem : compileConfiguration.getAllDependencies()) {
                if (dependencyItem instanceof ProjectDependency) {
                    Project dependencyProject = ((ProjectDependency) dependencyItem).getDependencyProject();

                    if (!explored.contains(dependencyProject)) {
                        Configuration dependencyCompile = dependencyProject.getConfigurations().findByName("compileClasspath");
                        if (dependencyCompile != null) {
                            Set<ResolvedArtifact> artifacts = dependencyCompile.getResolvedConfiguration().getResolvedArtifacts();

                            ResolvedArtifact vaadinArtifact = artifacts.stream()
                                    .filter(a -> a.getName().equals(dependencyName))
                                    .findFirst()
                                    .orElse(null);

                            if (vaadinArtifact != null) {
                                explored.add(dependencyProject);
                                collectProjectsWithDependency(dependencyProject, dependencyName, explored);
                            }
                        }
                    }
                }
            }
        }
    }

    protected Set<Project> collectProjectsWithDependency(String dependencyName) {
        Set<Project> result = new LinkedHashSet<>();
        collectProjectsWithDependency(getProject(), dependencyName, result);

        return result;
    }

    protected SourceSet getSourceSet(Project project, String sourceSetName) {
        return project.getConvention()
                .getPlugin(JavaPluginConvention.class)
                .getSourceSets()
                .getByName(sourceSetName);
    }

    protected boolean includedArtifact(String name) {
        return !excludes.contains(name);
    }

    @Internal
    public FileCollection getSourceFiles() {
        getProject().getLogger().info("Analyze source projects for widgetset building in {}", getProject().getName());

        List<File> sources = new ArrayList<>();
        List<File> files = new ArrayList<>();

        SourceSet mainSourceSet = getSourceSet(getProject(), MAIN_SOURCE_SET);

        sources.addAll(mainSourceSet.getJava().getSrcDirs());
        sources.addAll(getClassesDirs(mainSourceSet));
        sources.add(mainSourceSet.getOutput().getResourcesDir());

        for (Project dependencyProject : collectProjectsWithDependency("vaadin-client")) {
            getProject().getLogger().info("\tFound source project {} for widgetset building", dependencyProject.getName());

            SourceSet depMainSourceSet = getSourceSet(dependencyProject, MAIN_SOURCE_SET);

            sources.addAll(depMainSourceSet.getJava().getSrcDirs());
            sources.addAll(getClassesDirs(depMainSourceSet));
            sources.add(depMainSourceSet.getOutput().getResourcesDir());
        }

        sources.forEach(sourceDir -> {
            if (sourceDir.exists()) {
                getProject()
                        .fileTree(sourceDir, f -> {
                            List<String> excludes = new ArrayList<>();
                            // Default excludes
                            excludes.addAll(Arrays.asList("**/.*", "**/META-INF/build-info.properties"));
                            // Custom excludes
                            excludes.addAll(excludePaths);

                            f.setExcludes(excludes);

                            if (!includePaths.isEmpty()) {
                                f.setIncludes(includePaths);
                            }
                        })
                        .forEach(files::add);
            }
        });

        return getProject().files(files);
    }
}
