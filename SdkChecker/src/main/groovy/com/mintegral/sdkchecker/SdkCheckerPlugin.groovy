package com.mintegral.sdkchecker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class SdkCheckerPlugin implements Plugin<Project> {
    static String VERSION_3_ZERO_FIELD = "com.android.builder.Version"
    static String VERSION_3_ONE_FIELD  = "com.android.builder.model.Version"
    static String AGP_VERSION_FIELD    = "ANDROID_GRADLE_PLUGIN_VERSION"

    String gradlePluginVersion
    def sdkTypeMap

    @Override
    void apply(Project project) {
        sdkTypeMap = SdkUtils.initSdkTypeMap()

        def exception
        try {
            gradlePluginVersion = Class.forName(VERSION_3_ZERO_FIELD).getDeclaredField(AGP_VERSION_FIELD).get(this).toString()
        } catch (Exception e) {
            exception = e
        }

        try {
            gradlePluginVersion = Class.forName(VERSION_3_ONE_FIELD).getDeclaredField(AGP_VERSION_FIELD).get(this).toString()
        } catch (Exception e) {
            exception = e
        }

        if (gradlePluginVersion == null && exception != null) {
            throw IllegalStateException("requires the Android plugin to be configured", exception)
        } else if (gradlePluginVersion == null) {
            throw IllegalStateException("requires the Android plugin to be configured")
        }

        project.extensions.create("mintegralSdkType", SdkType)

        project.tasks.findByName('preBuild').dependsOn(
                project.task("mintegralSdkChecker") << {
                    def type = project['mintegralSdkType'].type
                    def area = project['mintegralSdkType'].area

                    def jars = sdkTypeMap.get(type)

                    if (jars == null || jars.size() == 0) {
                        throw new RuntimeException("please set mintegralSdkType in project's build.gradle .")
                    } else {
                        if (area == "china") {
                            if (jars.contains("mintegral_common")) {
                                jars.remove("mintegral_common")
                                jars.add("mintegral_chinacommon")
                            }
                        }
                    }

                    project.configurations.each { Configuration conf ->
                        //考虑多个Flavor的情况
                        if (conf.name.toLowerCase().contains("debugcompileclasspath")) {
                            // 获取所有依赖信息
                            def list = new ArrayList<String>()
                            conf.incoming.resolutionResult.root.dependencies.each { dr ->
                                list.add(dr.requested.displayName)
                            }

                            def mintegralList = new ArrayList<String>()
                            list.each { s ->
                                if (s.contains("mintegral")) {
                                    if (s.startsWith(":")) {
                                        mintegralList.add(s.substring(1, s.length()))
                                    } else {
                                        mintegralList.add(s)
                                    }
                                }
                            }

                            def notInclude = new StringBuffer()
                            jars.each { s ->
                                if (!mintegralList.contains(s)) {
                                    notInclude.append(s).append("   ")
                                }
                            }

                            if (notInclude.size() > 0) {
                                throw new RuntimeException("${type} need \"   ${notInclude}\", please recheck your project dependencies.\n\n")
                            }
                        }
                    }

                }
        )
    }
}
