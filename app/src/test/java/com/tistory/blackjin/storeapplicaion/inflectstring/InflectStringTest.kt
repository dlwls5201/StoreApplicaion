package com.tistory.blackjin.storeapplicaion.inflectstring

import org.junit.Assert
import org.junit.Test

class InflectStringTest {

    @Test
    fun `test case 1`() {
        val input = "Product"
        val solution = InflectStringSolution.getSnakeCaseLetter(input)
        Assert.assertEquals(solution, "product")
    }

    @Test
    fun `test case 2`() {
        val input = "SpecialGuest"
        val solution = InflectStringSolution.getSnakeCaseLetter(input)
        Assert.assertEquals(solution, "special_guest")
    }

    @Test
    fun `test case 3`() {
        val input = "Donald E. Knuth"
        val solution = InflectStringSolution.getSnakeCaseLetter(input)
        Assert.assertEquals(solution, "donald_e_knuth")
    }

    @Test
    fun `test case 4`() {
        val input = ""
        val solution = InflectStringSolution.getSnakeCaseLetter(input)
        Assert.assertEquals(solution, "")
    }

    @Test
    fun `test case 5`() {
        val input = "123!@#"
        val solution = InflectStringSolution.getSnakeCaseLetter(input)
        Assert.assertEquals(solution, "")
    }
}