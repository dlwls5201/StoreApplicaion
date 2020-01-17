package com.tistory.blackjin.storeapplicaion.flattentree

import com.google.gson.JsonParser
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class FlattenTreeTest {

    @Test
    fun main() {
        val categories = getJsonFile()
        val categoryGroup = JsonParser().parse(categories).asJsonObject
        val solution = FlattenTreeSolution().getClassifiedCategory(categoryGroup)
        println("solution $solution")
    }

    private fun getJsonFile(): String {
        this.javaClass.classLoader?.getResourceAsStream("categories.json")?.let {
            return it.bufferedReader().use { reader -> reader.readText() }
        }
        return ""
    }

    @Test
    fun `test case 1`() {
        val categories = "{A:{B:{C:{},D:{}}}}"
        val categoryGroup = JsonParser().parse(categories).asJsonObject
        val solution = FlattenTreeSolution().getClassifiedCategory(categoryGroup)
        Assert.assertEquals(solution.toString(), "[A > B > C, A > B > D]")
    }

    @Test
    fun `test case 2`() {
        val categories = "{A:{A1:{B1:{},B2:{}}}}"
        val categoryGroup = JsonParser().parse(categories).asJsonObject
        val solution = FlattenTreeSolution().getClassifiedCategory(categoryGroup)
        Assert.assertEquals(solution.toString(), "[A > A1 > B1, A > A1 > B2]")
    }

    @Test
    fun `test case 3`() {
        val categories = "{A:{B:{C:{},D:{}},E:{},F:{}}}"
        val categoryGroup = JsonParser().parse(categories).asJsonObject
        val solution = FlattenTreeSolution().getClassifiedCategory(categoryGroup)
        Assert.assertEquals(solution.toString(), "[A > B > C, A > B > D, A > E, A > F]")
    }

    @Test
    fun `test case 4`() {
        val categories = "{A:{B:{C:{},D:{}},E:{F:{},G:{}}}}"
        val categoryGroup = JsonParser().parse(categories).asJsonObject
        val solution = FlattenTreeSolution().getClassifiedCategory(categoryGroup)
        Assert.assertEquals(
            solution.toString(),
            "[A > B > C, A > B > D, A > E > F, A > E > G]"
        )
    }

    @Test
    fun `test case 5`() {
        val categories = "{A:{}}"
        val categoryGroup = JsonParser().parse(categories).asJsonObject
        val solution = FlattenTreeSolution().getClassifiedCategory(categoryGroup)
        Assert.assertEquals(solution.toString(), "[A]")
    }
}