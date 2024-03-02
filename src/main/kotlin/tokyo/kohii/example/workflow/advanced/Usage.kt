package tokyo.kohii.example.workflow.advanced

fun main() {
    val workflow = workflow {
        +{ println("Task 1") }
        parallel(maxConcurrency = 2) {
            sequential {
                +{ println("Task 2") }
                +{ println("Task 3") }
            }
            sequential {
                +{ println("Task 4") }
                parallel {
                    +{ println("Task 5") }
                    +{ println("Task 6") }
                }
            }
            parallel(maxConcurrency = 3) {
                (1..10).forEach { i ->
                    +{ println("Task 7 - $i") }
                }
            }
        }
        +{ println("Task 8") }
    }
    workflow.run()
}
