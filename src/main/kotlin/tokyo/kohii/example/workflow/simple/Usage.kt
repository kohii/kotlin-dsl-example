package tokyo.kohii.example.workflow.simple

fun main() {
    val workflow = workflow {
        task { println("Task 1") }
        parallel {
            sequential {
                task { println("Task 2") }
                task { println("Task 3") }
            }
            sequential {
                task { println("Task 4") }
                parallel {
                    task { println("Task 5") }
                    task { println("Task 6") }
                }
            }
            parallel(maxConcurrency = 3) {
                (1..10).forEach { i ->
                    task { println("Task 7 - $i") }
                }
            }
        }
        task { println("Task 8") }
    }
    workflow.run()
}