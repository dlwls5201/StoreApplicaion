package com.tistory.blackjin.storeapplicaion

import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
data class Person(
    var name: String,
    var age: Int
)

//인라인 식에서 사용하는 return은 람다를 인자로 받는 인라인 함수도 함께 종료시킨다. 이것을 넌로컬 return 이라도 한다.
fun findPerson(op: () -> Unit) {
    println("start find")
    op()
    println("end find")
}

val people = listOf(Person("BlackJin", 21), Person("Peter", 28), Person("Bob", 7))


class ExampleUnitTest {

    @Test
    fun main() {

        /*findPerson {
            for(person in people) {
                if(person.name == "Bob") {
                    println("Find!")
                    return@findPerson //에러발생, 하지만 findPerson을 inline으로 변경하면 에러가 사라집니다.
                }
            }
            println("Not found")
        }*/

        val isPositive1 = fun(num: Int): Boolean {
            return num > 0
        }

        val isPositive2: (Int) -> Boolean = {
            it > 0
        }

        println("isPositive1 = ${isPositive1(10)}")
        println("isPositive2 = ${isPositive2(10)}")
    }
}
