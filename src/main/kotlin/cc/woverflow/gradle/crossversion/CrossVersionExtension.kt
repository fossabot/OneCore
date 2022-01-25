package cc.woverflow.gradle.crossversion

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property

open class CrossVersionExtension(objects: ObjectFactory, val mcVersion: Int) {
    val vars = objects.mapProperty<String, Int>().convention(mutableMapOf(
            "MC" to mcVersion
    ))
    val keywords = objects.mapProperty<String, Keywords>().convention(mutableMapOf(
            ".java" to CrossVersionTask.DEFAULT_KEYWORDS,
            ".kt" to CrossVersionTask.DEFAULT_KEYWORDS,
            ".gradle" to CrossVersionTask.DEFAULT_KEYWORDS,
            ".json" to CrossVersionTask.DEFAULT_KEYWORDS,
            ".mcmeta" to CrossVersionTask.DEFAULT_KEYWORDS,
            ".cfg" to CrossVersionTask.CFG_KEYWORDS
    ))
    val patternAnnotation = objects.property<String>()
}