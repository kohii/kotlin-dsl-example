package tokyo.kohii.example.workflow.poormans

sealed interface WorkflowNode {
    fun run()
}

class Task(
    private val action: () -> Unit
) : WorkflowNode {
    override fun run() = action()
}

class Sequential(
    private val nodes: List<WorkflowNode>
) : WorkflowNode {
    constructor(vararg nodes: WorkflowNode) : this(nodes.toList())

    override fun run() {
        nodes.forEach { it.run() }
    }
}

class Parallel(
    private val nodes: List<WorkflowNode>,
    private val maxConcurrency: Int? = null,
) : WorkflowNode {
    constructor(vararg nodes: WorkflowNode, maxConcurrency: Int? = null) : this(nodes.toList(), maxConcurrency)

    override fun run() {
        val executor = if (maxConcurrency != null) {
            java.util.concurrent.Executors.newFixedThreadPool(maxConcurrency)
        } else {
            java.util.concurrent.Executors.newCachedThreadPool()
        }

        val futures = nodes.map { action ->
            executor.submit {
                action.run()
            }
        }

        futures.forEach { it.get() }
        executor.shutdown()
    }
}
