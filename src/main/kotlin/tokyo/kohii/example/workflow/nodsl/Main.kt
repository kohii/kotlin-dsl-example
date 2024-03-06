package tokyo.kohii.example.workflow.nodsl

import java.util.concurrent.Executors

fun main() {
    println("Task 1")

    val executorService = Executors.newCachedThreadPool()
    val future1 = executorService.submit {
        println("Task 2")
        println("Task 3")
    }
    val future2 = executorService.submit {
        println("Task 4")
        val executor2 = Executors.newCachedThreadPool()
        val future5 = executor2.submit { println("Task 5") }
        val future6 = executor2.submit { println("Task 6") }
        listOf(future5, future6).forEach { it.get() }
        executor2.shutdown()
    }
    val future3 = executorService.submit {
        val executor3 = Executors.newFixedThreadPool(3)
        val futures = (1..10).map {
            executor3.submit { println("Task 7 - $it") }
        }
        futures.forEach { it.get() }
        executor3.shutdown()
    }
    listOf(future1, future2, future3).forEach { it.get() }

    println("Task 8")
}
