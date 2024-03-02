package tokyo.kohii.example.workflow.advanced

fun main() {
    val workflow = buildWorkflow {
        val task1 = task { println("Task 1") }
        val task2 = task { println("Task 2") }
        val task3 = task { println("Task 3") }
        val task4 = task { println("Task 4") }
        val task5 = task { println("Task 5") }
        val task6 = task { println("Task 6") }
        val task7s = (1..10).map { i ->
            task { println("Task 7 - $i") }
        }
        val task8 = task { println("Task 8") }

        task2 dependsOn task1
        task3 dependsOn task2
        task4 dependsOn task1
        task5 dependsOn task4
        task6 dependsOn task4
        task7s dependsOn task1
        task8 dependsOn listOf(task3, task5, task6) + task7s
    }
    workflow.run()
}
