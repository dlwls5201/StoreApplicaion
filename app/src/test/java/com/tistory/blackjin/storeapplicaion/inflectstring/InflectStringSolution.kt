package com.tistory.blackjin.storeapplicaion.inflectstring

import java.util.regex.Pattern

object InflectStringSolution {

    fun getSnakeCaseLetter(letter: String): String {

        val snakeCaseResult = StringBuilder()

        letter.filter { Pattern.matches("^[a-zA-Z]*$", it.toString()) }
            .forEach { character ->
                snakeCaseResult.append(
                    when (character) {
                        in 'A'..'Z' -> {
                            "_${character.toLowerCase()}"
                        }
                        else -> {
                            character.toString()
                        }
                    }
                )
            }

        return if (snakeCaseResult.isNotEmpty() && snakeCaseResult.first().toString() == "_") {
            snakeCaseResult.drop(1).toString()
        } else {
            snakeCaseResult.toString()
        }
    }
}