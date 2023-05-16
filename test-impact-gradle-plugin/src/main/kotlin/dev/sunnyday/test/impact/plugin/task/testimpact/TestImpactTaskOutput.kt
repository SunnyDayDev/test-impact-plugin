package dev.sunnyday.test.impact.plugin.task.testimpact

import dev.sunnyday.test.impact.plugin.domain.graph.ImpactProjectGraph
import java.io.File
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

internal class TestImpactTaskOutput(
    val file: File,
) {

    fun writeImpactGraph(graph: ImpactProjectGraph) {
        if (!file.parentFile.mkdirs()) {
            throw IOException("Can't create output file directory: ${file.parentFile}")
        }

        ObjectOutputStream(file.outputStream()).use { stream ->
            stream.writeObject(graph)
        }
    }

    fun readImpactGraph(): ImpactProjectGraph {
        return ObjectInputStream(file.inputStream()).use { stream ->
            stream.readObject() as ImpactProjectGraph
        }
    }
}