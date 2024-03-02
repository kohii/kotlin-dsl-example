package tokyo.kohii.example.workflow.advanced

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Task(private val action: () -> Unit) {
    fun run() = action()
}

class Dependency(val from: Task, val to: Task)

class Workflow(
    private val tasks: List<Task>,
    private val dependencies: List<Dependency>,
    private val maxConcurrency: Int?
) {
    private val taskFutures = ConcurrentHashMap<Task, CompletableFuture<*>>()

    fun run() {
        val executorService = if (maxConcurrency != null) {
            Executors.newFixedThreadPool(maxConcurrency)
        } else {
            Executors.newCachedThreadPool()
        }
        val futures = tasks.map { it.runAsyncWithDependencies(executorService) }
        futures.forEach { it.get() }
        executorService.shutdown()
    }

    private fun Task.runAsyncWithDependencies(executorService: ExecutorService): CompletableFuture<*> {
        val future = taskFutures.computeIfAbsent(this) {
            CompletableFuture.supplyAsync({
                // Wait for all dependencies to complete
                dependencies.filter { it.from == this }
                    .map { it.to.runAsyncWithDependencies(executorService) }
                    .forEach { it.get() }
                this.run()
            }, executorService)
        }
        return future
    }
}

class WorkflowBuilder {
    private val tasks = mutableListOf<Task>()
    private val dependencies = mutableListOf<Dependency>()

    fun task(action: () -> Unit): Task {
        return Task(action).also { tasks.add(it) }
    }

    infix fun Task.dependsOn(other: Task) {
        dependencies.add(Dependency(this, other))
    }

    infix fun Collection<Task>.dependsOn(other: Task) {
        forEach { it.dependsOn(other) }
    }

    infix fun Task.dependsOn(others: Collection<Task>) {
        others.forEach { dependsOn(it) }
    }

    fun build(maxConcurrency: Int?): Workflow {
        return Workflow(tasks, dependencies, maxConcurrency)
    }
}

fun buildWorkflow(maxConcurrency: Int? = null, block: WorkflowBuilder.() -> Unit): Workflow {
    val builder = WorkflowBuilder()
    builder.block()
    return builder.build(maxConcurrency)
}
