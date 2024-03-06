package tokyo.kohii.example.workflow.poormans

fun main() {
    val workflow = Sequential(
        Task { println("Task 1") },
        Parallel(
            Sequential(
                Task { println("Task 2") },
                Task { println("Task 3") },
            ),
            Sequential(
                Task { println("Task 4") },
                Parallel(
                    Task { println("Task 5") },
                    Task { println("Task 6") },
                ),
            ),
            Parallel(
                (1..10).map { i ->
                    Task { println("Task 7 - $i") }
                },
                maxConcurrency = 3,
            ),
        ),
        Task { println("Task 8") },
    )
    workflow.run()
}
