package org.graclj.language.clj.plugins;

import org.graclj.internal.DontUnderstand;
import org.graclj.internal.InternalUse;
import org.graclj.language.clj.ClojureSourceSet;
import org.graclj.language.clj.internal.DefaultClojureSourceSet;
import org.graclj.language.clj.tasks.PlatformClojureCompile;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.jvm.JvmBinarySpec;
import org.gradle.jvm.JvmByteCode;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.language.base.internal.SourceTransformTaskConfig;
import org.gradle.language.base.internal.registry.LanguageTransform;
import org.gradle.language.base.internal.registry.LanguageTransformContainer;
import org.gradle.language.base.plugins.ComponentModelBasePlugin;
import org.gradle.language.jvm.plugins.JvmResourcesPlugin;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.model.internal.manage.schema.ModelSchemaStore;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.LanguageType;
import org.gradle.platform.base.LanguageTypeBuilder;

import java.util.Collections;
import java.util.Map;

public class ClojureLanguagePlugin implements Plugin<Project> {
    public void apply(Project project) {
        project.getPluginManager().apply(ComponentModelBasePlugin.class);
        project.getPluginManager().apply(JvmResourcesPlugin.class);
    }

    @DontUnderstand("How does this get applied? It's not part of the plugin apply method.")
    static class Rules extends RuleSource {
        @LanguageType
        void registerLanguage(LanguageTypeBuilder<ClojureSourceSet> builder) {
            builder.setLanguageName("clojure");
            builder.defaultImplementation(DefaultClojureSourceSet.class);
        }

        @InternalUse("Language transform is internal")
        @Mutate
        void registerLanguageTransform(LanguageTransformContainer languages) {
            languages.add(new Clojure());
        }
    }

    @InternalUse("Language transform is internal")
    private static class Clojure implements LanguageTransform<ClojureSourceSet, JvmByteCode> {

        @Override
        public Class<ClojureSourceSet> getSourceSetType() {
            return ClojureSourceSet.class;
        }

        @Override
        public Class<JvmByteCode> getOutputType() {
            return JvmByteCode.class;
        }

        @DontUnderstand("What can this be used for?")
        @Override
        public Map<String, Class<?>> getBinaryTools() {
            return Collections.emptyMap();
        }

        @Override
        public SourceTransformTaskConfig getTransformTask() {
            return new SourceTransformTaskConfig() {
                @Override
                public String getTaskPrefix() {
                    return "compile";
                }

                @Override
                public Class<? extends DefaultTask> getTaskType() {
                    return PlatformClojureCompile.class;
                }

                @Override
                public void configureTask(Task task, BinarySpec binarySpec, LanguageSourceSet languageSourceSet, ServiceRegistry serviceRegistry) {
                    PlatformClojureCompile compile = (PlatformClojureCompile) task;
                    JvmBinarySpec binary = (JvmBinarySpec) binarySpec;
                    ClojureSourceSet sourceSet = (ClojureSourceSet) languageSourceSet;

                    compile.setDescription(String.format("AOT compiles %s", sourceSet));
                    compile.setDestinationDir(binary.getClassesDir());
                    compile.setClasspath(sourceSet.getCompileClasspath().getFiles());
                    binary.getTasks().getJar().dependsOn(compile);
                }
            };
        }

        @Override
        public boolean applyToBinary(BinarySpec binarySpec) {
            return binarySpec instanceof JvmBinarySpec;
        }
    }
}
