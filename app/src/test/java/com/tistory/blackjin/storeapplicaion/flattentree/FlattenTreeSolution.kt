package com.tistory.blackjin.storeapplicaion.flattentree

import com.google.gson.JsonObject

class FlattenTreeSolution {

    private val result = mutableListOf<String>()

    fun getClassifiedCategory(categoryGroup: JsonObject): List<String> {
        classifyCategory(mutableListOf(), categoryGroup)
        return result
    }

    private fun classifyCategory(classifiedCategory: List<String>, categoryGroup: JsonObject) {
        val categoryNames = categoryGroup.keySet().iterator()

        while (categoryNames.hasNext()) {

            val categoryName = categoryNames.next()

            val nextCategoryGroup = categoryGroup.getAsJsonObject(categoryName)

            val nextClassifiedCategory = mutableListOf<String>()
            nextClassifiedCategory.addAll(classifiedCategory)
            nextClassifiedCategory.add(categoryName)

            if (nextCategoryGroup != null && nextCategoryGroup.keySet().size > 0) {
                classifyCategory(nextClassifiedCategory, nextCategoryGroup)
            } else {
                result.add(nextClassifiedCategory.joinToString(" > "))
            }
        }
    }
}