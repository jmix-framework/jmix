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

package io.jmix.gradle

import groovy.xml.XmlParser
import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

import static io.jmix.gradle.DescriptorGenerationUtils.CONVERTERS_LIST_PROPERTY
import static io.jmix.gradle.MetaModelUtil.*

class EnhancingAction implements Action<Task> {

    static final String MAIN_SET_NAME = "main"
    static final String TEST_SET_NAME = "test"

    static final String MAIN_STORE_NAME = "main"

    static final String APP_PROPERTIES_FILE = "application.properties"
    static final String ADDITIONAL_STORE_PROPERTY = "jmix.core.additional-stores"

    private String sourceSetName

    EnhancingAction(String sourceSetName) {
        this.sourceSetName = sourceSetName
    }

    static String enhancedClassesDir(Project project, String sourceSetName) {
        return "$project.buildDir/classes/jmix/$sourceSetName"
    }

    @Override
    void execute(Task task) {
        Project project = task.getProject()
        project.logger.lifecycle "Enhancing entities in $project for source set '$sourceSetName'"
        SourceSet sourceSet = project.sourceSets.findByName(sourceSetName)

        String compiledDir = sourceSet.java.destinationDirectory.get().getAsFile().absolutePath
        String enhancedDir = enhancedClassesDir(project, sourceSetName)

        if (!hasCompiledClasses(project, compiledDir)) {
            project.logger.info "No compiled classes in $compiledDir; entity enhancing for source set '$sourceSetName' is not required"
            return
        }

        ClassesInfo classesInfo = collectClasses(project, sourceSet, compiledDir)

        addAbsentEmptyStores(classesInfo, sourceSet, project)

        constructDescriptors(project, sourceSet, classesInfo, compiledDir)

        Set<String> enhancedClassNames = classesInfo.getAllEntities() as Set

        reconcileEnhancedDir(project, compiledDir, enhancedDir, enhancedClassNames)

        boolean entitiesEnhancingRequired = !project.jmix.entitiesEnhancing.skipUnmodifiedEntitiesEnhancing ||
                entityClassesChangedSinceLastBuild(project, classesInfo, compiledDir) ||
                enhancedEntitiesMissing(compiledDir, enhancedDir, enhancedClassNames)

        if (entitiesEnhancingRequired) {
            copyClasses(compiledDir, enhancedDir, enhancedClassNames)
            persistenceProviderEnhancing(project).run(project, sourceSet, enhancedDir, classesInfo.allStores())
            runJmixEnhancing(project, sourceSet, enhancedDir, classesInfo)
            if (project.jmix.entitiesEnhancing.skipUnmodifiedEntitiesEnhancing) {
                saveEntityClassesChecksumForNextBuild(project, classesInfo, compiledDir)
            }
        } else {
            project.logger.lifecycle "Entities enhancing was skipped, because entity classes haven't been changed since the last build"
        }
    }

    /**
     * Reconciles the enhanced output directory against the compiled (input) directory:
     * <ul>
     *     <li>removes stale {@code *.class} files in {@code enhancedDir} whose <em>outer</em> class has no
     *         counterpart in {@code compiledDir};</li>
     *     <li>copies every non-enhanced class (not in {@code enhancedClassNames}) from {@code compiledDir} to {@code enhancedDir}.</li>
     * </ul>
     * Staleness is keyed on the outer class so that classes the enhancer <em>generates</em> (e.g.
     * {@code SomeEntity$JmixEntityEntry}), which have no compiled counterpart of their own, are kept as long
     * as their entity is still compiled, yet a removed class drops all of its (inner and generated) artifacts.
     */
    protected void reconcileEnhancedDir(Project project, String compiledDir, String enhancedDir, Set<String> enhancedClassNames) {
        File compiledRoot = new File(compiledDir)
        File enhancedRoot = new File(enhancedDir)

        Set<String> enhancedRelativePaths = enhancedClassNames.collect { it.replace('.', '/') + '.class' } as Set

        if (enhancedRoot.exists()) {
            project.fileTree(enhancedRoot).each { File file ->
                if (file.name.endsWith('.class')) {
                    String relativePath = enhancedRoot.toPath().relativize(file.toPath()).toString().replace(File.separator, '/')
                    File compiledOuterClass = new File(compiledRoot, outerClassFile(relativePath))
                    if (!compiledOuterClass.exists()) {
                        project.logger.info "Removing stale enhanced class: $relativePath"
                        file.delete()
                    }
                }
            }
        }

        if (compiledRoot.exists()) {
            project.fileTree(compiledRoot).each { File file ->
                if (file.name.endsWith('.class')) {
                    String relativePath = compiledRoot.toPath().relativize(file.toPath()).toString().replace(File.separator, '/')
                    if (!enhancedRelativePaths.contains(relativePath)) {
                        File target = new File(enhancedRoot, relativePath)
                        target.getParentFile().mkdirs()
                        Files.copy(file.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
                    }
                }
            }
        }
    }

    /**
     * Returns {@code true} if the compiled-classes directory exists and contains at least one {@code *.class}
     * file. A source set with no compiled classes (e.g. a module with no test sources) has nothing to enhance,
     * so enhancement is skipped to avoid running the persistence provider weaver with no input.
     */
    protected boolean hasCompiledClasses(Project project, String compiledDir) {
        File compiledRoot = new File(compiledDir)
        return compiledRoot.exists() && !project.fileTree(compiledRoot).matching { include '**/*.class' }.empty
    }

    /**
     * Maps a class-file relative path to the relative path of its outer (top-level) class file, e.g.
     * {@code a/b/Entity$JmixEntityEntry.class} -> {@code a/b/Entity.class}. Returns the input unchanged for
     * a top-level class.
     */
    protected static String outerClassFile(String relativeClassFile) {
        int dollarIndex = relativeClassFile.indexOf('$')
        return dollarIndex < 0 ? relativeClassFile : relativeClassFile.substring(0, dollarIndex) + '.class'
    }

    /**
     * Returns {@code true} if any entity class that belongs to this project (i.e. present in {@code compiledDir})
     * is absent from {@code enhancedDir}. Guards the {@code skipUnmodifiedEntitiesEnhancing} optimization: the
     * enhanced output dir and the saved checksum live in different {@code build} subtrees, so the enhanced dir
     * can be cleared (e.g. {@code rm -rf build/classes}, an IDE rebuild, a partial clean) while the checksum
     * survives. Without this check the task would run but skip weaving, leaving entities missing from the
     * enhanced dir that downstream modules consume.
     */
    protected boolean enhancedEntitiesMissing(String compiledDir, String enhancedDir, Set<String> enhancedClassNames) {
        File compiledRoot = new File(compiledDir)
        File enhancedRoot = new File(enhancedDir)
        for (String className : enhancedClassNames) {
            String relativePath = className.replace('.', '/') + '.class'
            if (new File(compiledRoot, relativePath).exists() && !new File(enhancedRoot, relativePath).exists()) {
                return true
            }
        }
        return false
    }

    /**
     * Copies fresh, un-enhanced copies of the given classes from {@code compiledDir} to {@code enhancedDir},
     * overwriting any previously enhanced copies, so they can be (re-)woven.
     */
    protected void copyClasses(String compiledDir, String enhancedDir, Set<String> classNames) {
        File compiledRoot = new File(compiledDir)
        File enhancedRoot = new File(enhancedDir)
        for (String className : classNames) {
            String relativePath = className.replace('.', '/') + '.class'
            File source = new File(compiledRoot, relativePath)
            if (source.exists()) {
                File target = new File(enhancedRoot, relativePath)
                target.getParentFile().mkdirs()
                Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    /**
     * Completes {@code classesInfo} with "empty" data stores in order to `persistence.xml` and `orm.xml` descriptors will be generated for them.
     * An "Empty" store is a store without entities. Such stores will not be added to {@code classesInfo} by
     * the {@link io.jmix.gradle.EnhancingAction#collectClasses} discovery method.
     */
    protected void addAbsentEmptyStores(ClassesInfo classesInfo, SourceSet sourceSet, Project project) {
        def appPropertyFiles = new HashSet<File>()
        for (File srcDir : sourceSet.getResources().getSrcDirs()) {
            if (srcDir.exists()) {
                File candidate = new File(srcDir, APP_PROPERTIES_FILE)
                if (candidate.exists()){
                    appPropertyFiles.add(candidate)
                }
            }
        }

        def additionalStores = []

        for (File file : appPropertyFiles) {
            project.logger.debug "Looking for '$ADDITIONAL_STORE_PROPERTY' in ${file.getPath()} ..."
            def properties = new Properties()
            file.withInputStream { properties.load(it) }

            if (properties.containsKey(ADDITIONAL_STORE_PROPERTY)) {
                project.logger.debug "Additional datastores found in '${file.getPath()}': ${properties.getProperty(ADDITIONAL_STORE_PROPERTY)}"
                additionalStores.addAll(properties.getProperty(ADDITIONAL_STORE_PROPERTY)
                        .split(',')
                        .collect { it.trim() })
            }
        }

        def allStores = classesInfo.allStores()

        for (String additionalStore : additionalStores) {
            if (!allStores.contains(additionalStore)) {
                project.logger.info "Store $additionalStore has no entities. Empty 'persistence.xml' and 'orm.xml' will be generated for it.";
                classesInfo.classesByStores.get(additionalStore)
            }
        }
    }

    protected ClassesInfo collectClasses(Project project, SourceSet sourceSet, String projectClassesDir) {
        ClassesInfo classesInfo = new ClassesInfo()

        if (sourceSetName == TEST_SET_NAME) {
            def mainSourceSet = project.sourceSets.findByName(MAIN_SET_NAME)
            String mainClassesDir = mainSourceSet.java.destinationDirectory.get().getAsFile().absolutePath
            collectEntitiesFromSources(project, mainSourceSet, classesInfo, mainClassesDir)
        }

        collectEntitiesFromSources(project, sourceSet, classesInfo, projectClassesDir)

        if (sourceSetName == TEST_SET_NAME && classesInfo.modulePaths.size() > 1) {
            //test sourceSet can have 0 or more Configurations. Production Configuration path should be removed in case of test Configuration presence
            classesInfo.modulePaths.remove(0);
        }

        project.logger.lifecycle("Project entities:\n    JPA: ${classesInfo.getJpaEntities()};\n    DTO: ${classesInfo.getDtoEntities()};\n" +
                "Project converters: ${classesInfo.getConverters()}.")

        project.jmix.entitiesEnhancing.jpaConverters.each {
            classesInfo.converters.add(it)
        }

        collectEntitiesFromClasspathes(project, sourceSet, classesInfo, projectClassesDir)

        project.logger.info("Found entities:\n    JPA: ${classesInfo.getJpaEntities()};\n    DTO: ${classesInfo.getDtoEntities()}.\n" +
                "Converters: ${classesInfo.getConverters()}")

        return classesInfo
    }

    protected void collectEntitiesFromSources(Project project, sourceSet, ClassesInfo classesInfo, String projectClassesDir) {
        ClassPool classPool = createClassPool(project, sourceSet, projectClassesDir)

        sourceSet.allJava.getSrcDirs().each { File srcDir ->
            project.fileTree(srcDir).each { File file ->
                if (file.name.endsWith('.java') || file.name.endsWith('.kt')) {
                    String ext = file.name.endsWith('.java') ? '.java' : '.kt'
                    String pathStr = srcDir.toPath().relativize(file.toPath()).join('.')
                    String className = pathStr.substring(0, pathStr.length() - ext.length())

                    try {
                        CtClass ctClass = classPool.get(className)

                        examineClass(ctClass, classesInfo)

                        if (isModuleConfig(ctClass)) {
                            classesInfo.modulePaths.add(ctClass.getPackageName().replace('.', '/'));
                        }
                    } catch (NotFoundException e) {
                        project.logger.info "Cannot find $className in ${project} for enhancing: $e"
                    }
                }
            }
        }
    }

    protected void collectEntitiesFromClasspathes(Project project, sourceSet, ClassesInfo classesInfo, String projectClassesDir) {
        ClassPool classPool = createClassPool(project, sourceSet, projectClassesDir)

        ClassesInfo compileClasses = collectEntitiesFromClasspath(project, sourceSet.compileClasspath, classPool)
        ClassesInfo runtimeClasses = collectEntitiesFromClasspath(project, sourceSet.runtimeClasspath, classPool)

        classesInfo.addClassesIntersection(runtimeClasses, compileClasses)
    }

    protected ClassesInfo collectEntitiesFromClasspath(Project project, FileCollection currentClasspath, ClassPool classPool) {
        ClassesInfo classesInfo = new ClassesInfo()

        def jars = currentClasspath.asList().findAll { it.name.endsWith('.jar') }
        jars.each { lib ->
            if (lib.exists()) {
                project.zipTree(lib).matching { include "**/*persistence.xml" }.each {
                    Node doc = new XmlParser().parse(it)
                    def docPu = doc.'persistence-unit'[0]
                    List<String> currentEntities = docPu.'class'.collect { it.text() }
                    String storeName = docPu.@name
                    classesInfo.classesByStores[storeName ?: MAIN_STORE_NAME].addAll(currentEntities)

                    String converters = docPu.'properties'.'*'.find { it.@name == CONVERTERS_LIST_PROPERTY }?.@value

                    converters?.split(';')?.each { classesInfo.converters.add(it) }

                    project.logger.debug("Found $it.name in $lib.name. Entities: $currentEntities.\n Converters: $converters")
                }
            }
        }

        def folders = currentClasspath.asList().findAll { it.isDirectory() }
        folders.each { File folder ->
            project.fileTree(folder).each {
                if (it.name.endsWith('.class')) {
                    String pathStr = folder.toPath().relativize(it.toPath()).join('.')
                    String className = pathStr.substring(0, pathStr.length() - '.class'.length())

                    try {
                        CtClass ctClass = classPool.get(className)
                        examineClass(ctClass, classesInfo)
                    } catch (NotFoundException e) {
                        project.logger.info "Cannot determine $className in classpath folder '${folder}': $e"
                    }
                }
            }
        }
        return classesInfo
    }

    protected void examineClass(CtClass ctClass, ClassesInfo classesInfo) {
        if (isJpaEntity(ctClass) || isJpaMappedSuperclass(ctClass) || isJpaEmbeddable(ctClass)) {
            classesInfo.classesByStores[findStoreName(ctClass) ?: MAIN_STORE_NAME].add(ctClass.getName())
        } else if (isJpaConverter(ctClass)) {
            classesInfo.converters.add(ctClass.getName())
        } else if (isJmixEntity(ctClass)) {
            classesInfo.nonMappedClasses[findStoreName(ctClass) ?: MAIN_STORE_NAME].add(ctClass.getName())
        }
    }

    protected void constructDescriptors(Project project, sourceSet, ClassesInfo classesInfo, String projectClassesDir) {
        for (String storeName : classesInfo.allStores()) {
            for (String modulePath : classesInfo.modulePaths) {

                String storePrefix = storeName == MAIN_STORE_NAME ? "" : (storeName + '-')
                String enhancingWorkDir = "$project.buildDir/tmp/entitiesEnhancing/$sourceSetName/${storePrefix}persistence"

                String persistenceFileName = "$enhancingWorkDir/META-INF/persistence.xml"

                String ormRelativeFileName = "$modulePath/${storePrefix}orm.xml";
                String ormFileName = "$enhancingWorkDir/$ormRelativeFileName"

                Set<String> jpaClasses = filterNonJpaClasses(project, classesInfo.getJpaEntitiesAndConverters(storeName))

                //create persistence.xml in tmp work directory to be processed by orm-provider weaving (e.g. EclipseLink accepts '*/META-INF/persistence.xml' file path only)
                DescriptorGenerationUtils.constructPersistenceXml(
                        persistenceFileName,
                        storeName,
                        ormRelativeFileName,
                        jpaClasses,
                        classesInfo.getConverters())

                DescriptorGenerationUtils.constructOrmXml(
                        ormFileName,
                        jpaClasses,
                        createClassPool(project, sourceSet, projectClassesDir))

                //write generated persistence/orm xml into a dedicated generated resources dir,
                //which is registered as a resources srcDir and consumed by processResources (single owner of build/resources)
                String generatedPath = "${generatedDescriptorsDir(project, sourceSetName)}/$modulePath"
                project.logger.info "Copying files $persistenceFileName and $ormFileName to $generatedPath"
                project.copy {
                    from persistenceFileName, ormFileName
                    into generatedPath
                    rename "persistence.xml", "${storePrefix}persistence.xml"
                }
            }
        }
    }

    static String generatedDescriptorsDir(Project project, String sourceSetName) {
        return "$project.buildDir/generated/jmix-descriptors/$sourceSetName"
    }

    protected void runJmixEnhancing(Project project, sourceSet, String enhancedDir, ClassesInfo classesInfo) {
        if (!classesInfo.getAllEntities().isEmpty()) {
            project.logger.lifecycle "Running Jmix enhancer in $project for $sourceSet"

            for (EnhancingStep step : enhancingSteps()) {

                ClassPool classPool = createClassPool(project, sourceSet, enhancedDir)

                step.classPool = classPool
                step.outputDir = enhancedDir
                step.logger = project.logger

                for (className in classesInfo.getAllEntities()) {
                    def classFileName = className.replace('.', '/') + '.class'
                    def classFile = new File(enhancedDir, classFileName)

                    if (classFile.exists()) {
                        // skip files from dependencies, enhance only classes from the enhanced output dir
                        step.execute(className)
                    }
                }
            }
        }
    }

    /**
     * Evaluates a checksum of enhanced entity classes and saves it to the file.
     * <p>
     * On the next project build, the checksum of entities classes from the "build" directory will be compared with the
     * checksum saved to the file to find out if entities have been changed since the last compilation.
     */
    protected void saveEntityClassesChecksumForNextBuild(Project project, ClassesInfo classesInfo, String compiledDir) {
        def allEntityClassesChecksum = evaluateEntityClassesChecksum(project, classesInfo, compiledDir)
        Path checksumFilePath = getEntitiesChecksumFilePath(project)
        if (!Files.exists(checksumFilePath.getParent())) {
            Files.createDirectories(checksumFilePath.getParent())
        }
        Files.write(checksumFilePath, allEntityClassesChecksum.getBytes(StandardCharsets.UTF_8))
    }

    /**
     * Evaluates a single checksum for all entity classes (of the current project) extracted from the ClassesInfo.
     */
    protected String evaluateEntityClassesChecksum(Project project, ClassesInfo classesInfo, String compiledDir) {
        StringBuilder sb = new StringBuilder()
        Collection<String> allEntities = classesInfo.getAllEntities()
        allEntities.sort()
        for (className in allEntities) {
            def classFileName = className.replace('.', '/') + '.class'
            def classFile = new File(compiledDir, classFileName)

            // skip files from dependencies, consider only compiled classes from `compiledDir`
            if (classFile.exists()) {
                def fileChecksum = checkSum(classFile)
                sb.append(fileChecksum)
                project.logger.debug("Entity file: {}, checksum: {}", classFileName, fileChecksum)
            }
        }
        return checkSum(sb.toString().getBytes(StandardCharsets.UTF_8))
    }

    /**
     * Checks if entity classes in a project build directory and entity classes from last project build are not
     * the same. Method compares the checksum of entity classes saved during previous compilation with the checksum of
     * actual entity classes collection.
     */
    protected boolean entityClassesChangedSinceLastBuild(Project project, ClassesInfo classesInfo, String compiledDir) {
        Path entitiesChecksumFilePath = getEntitiesChecksumFilePath(project)
        if (!Files.exists(entitiesChecksumFilePath)) {
            //previous entity classes checksum was not saved
            return true
        }
        def prevChecksum = Files.readString(entitiesChecksumFilePath)
        def currentChecksum = evaluateEntityClassesChecksum(project, classesInfo, compiledDir)
        project.logger.debug("Previous entities checksum: {}, current entities checksum: {}", prevChecksum, currentChecksum)
        return prevChecksum != currentChecksum
    }

    protected String checkSum(File file) {
        byte[] data = Files.readAllBytes(file.toPath())
        return checkSum(data)
    }

    protected String checkSum(byte[] data) {
        byte[] hash = MessageDigest.getInstance("MD5").digest(data)
        return new BigInteger(1, hash).toString(16)
    }

    /**
     * Returns a path to a file where entity classes checksum will be stored to.
     */
    protected Path getEntitiesChecksumFilePath(Project project) {
        return Paths.get("$project.buildDir/tmp/entitiesEnhancing/$sourceSetName/entities.checksum")
    }

    static ClassPool createClassPool(Project project, sourceSet, String projectClassesDir) {
        ClassPool classPool = new ClassPool(null)
        classPool.appendSystemPath()

        for (File file in sourceSet.compileClasspath) {
            classPool.insertClassPath(file.getAbsolutePath())
        }

        classPool.insertClassPath(projectClassesDir)

        project.configurations.enhancing.files.each { File dep ->
            classPool.insertClassPath(dep.absolutePath)
        }

        return classPool
    }

    protected static PersistenceProviderEnhancing persistenceProviderEnhancing(Project project) {
        return project.objects.newInstance(EclipselinkEnhancing)
    }

    protected static List<EnhancingStep> enhancingSteps() {
        Arrays.asList(
                new JmixEntityEnhancingStep(),
                new EntityEntryEnhancingStep(),
                new SettersEnhancingStep(),
                new TransientAnnotationEnhancingStep())
    }

    static Set<String> filterNonJpaClasses(Project project, Set<String> classNames) {
        List<String> nonJpaPackages = project.jmix.entitiesEnhancing.nonJpaPackages
        List<String> nonJpaClasses = project.jmix.entitiesEnhancing.nonJpaClasses

        return classNames.findAll {className ->
            boolean result = !nonJpaClasses.contains(className) && !nonJpaPackages.find {className.startsWith(it + '.')}
            if (!result)
                project.logger.info "Entity ${className} was filtered out by nonJpaPackages or nonJpaClasses"
            return result
        }
    }

    /**
     * Stores information about entities and converters
     */
    private class ClassesInfo {
        Map<String, Set<String>> classesByStores = [:].withDefault { _ -> new HashSet<String>() }
        Set<String> converters = [] as Set<String>
        Map<String, Set<String>> nonMappedClasses = [:].withDefault { _ -> new HashSet<String>() }

        List<String> modulePaths = []

        Collection<String> getJpaEntities() {
            return classesByStores.values().flatten()
        }

        Collection<String> getDtoEntities() {
            return nonMappedClasses.values().flatten()
        }

        /**
         * @return JPA and DTO entities
         */
        Collection<String> getAllEntities() {
            return getJpaEntities() + nonMappedClasses.values().flatten()
        }

        Set<String> getJpaEntitiesAndConverters(String store) {
            return (classesByStores[store] + converters).toSet()
        }

        /**
         * @return list of store names
         */
        Set<String> allStores() {
            return classesByStores.size() > 0 ? classesByStores.keySet() :
                    converters.size() > 0 ? ['main'] : new HashSet<String>()
        }

        /**
         *  Adds classes that exists in both {@code first} and {@code second} ClassesInfos
         */
        void addClassesIntersection(ClassesInfo first, ClassesInfo second) {

            for (String store : first.classesByStores.keySet()) {
                classesByStores[store].addAll(first.classesByStores[store].intersect(second.classesByStores[store]))
            }

            for (String store : first.nonMappedClasses.keySet()) {
                nonMappedClasses[store].addAll(first.nonMappedClasses[store].intersect(second.nonMappedClasses[store]))
            }

            converters.addAll(first.converters.intersect(second.converters))
        }

    }

}
