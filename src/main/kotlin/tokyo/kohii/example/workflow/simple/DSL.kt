package tokyo.kohii.example.workflow.simple

@DslMarker
annotation class WorkflowDsl

@WorkflowDsl
sealed interface WorkflowNode {
    fun run()
}

// This makes calling parent DSL method from `task` block impossible
@WorkflowDsl
object TaskThis

class Task(
    private val action: () -> Unit
) : WorkflowNode {
    override fun run() = action()
}

class Sequential(
    private val nodes: List<WorkflowNode>
) : WorkflowNode {
    override fun run() {
        nodes.forEach { it.run() }
    }
}

class Parallel(
    private val nodes: List<WorkflowNode>,
    private val maxConcurrency: Int?,
) : WorkflowNode {
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

class ExecutionBuilder {
    // ノードのリスト（ミュータブル）
    // sequential, parallel, task が呼ばれるたびに新しいノードが追加される
    val nodes = mutableListOf<WorkflowNode>()

    fun sequential(block: ExecutionBuilder.() -> Unit) {
        val builder = ExecutionBuilder()
        builder.block()
        nodes.add(Sequential(builder.nodes))
    }

    fun parallel(maxConcurrency: Int? = null, block: ExecutionBuilder.() -> Unit) {
        val builder = ExecutionBuilder()
        builder.block()
        nodes.add(Parallel(builder.nodes, maxConcurrency))
    }

    fun task(action: TaskThis.() -> Unit) {
        nodes.add(Task { action(TaskThis) })
    }
}

// ワークフローを宣言するためのトップレベル関数
fun workflow(block: ExecutionBuilder.() -> Unit): WorkflowNode {
    val builder = ExecutionBuilder()
    builder.block()
    return Sequential(builder.nodes)
}
