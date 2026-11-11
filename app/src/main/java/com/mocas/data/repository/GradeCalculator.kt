package com.mocas.data.repository

import com.mocas.data.local.GradeCategoryEntity
import com.mocas.data.local.GradeItemEntity
import com.mocas.data.local.GradeUnitCategoryWeightEntity
import com.mocas.data.local.GradeUnitEntity

data class GradeSummary(
    val currentAverage: Double?,
    val weightedPoints: Double,
    val gradedWeight: Double,
    val neededToPass: Double?
)

object GradeCalculator {
    fun categoryAverage(categoryId: Long, items: List<GradeItemEntity>): Double? =
        items.filter { it.categoryId == categoryId }.map { it.score }.takeIf { it.isNotEmpty() }?.average()

    fun resolveCategoriesForUnit(
        categories: List<GradeCategoryEntity>,
        unitId: Long,
        unitCategoryWeights: List<GradeUnitCategoryWeightEntity>
    ): List<GradeCategoryEntity> {
        val customWeights = unitCategoryWeights.filter { it.unitId == unitId }
        if (customWeights.isEmpty()) return categories
        return categories.map { cat ->
            val custom = customWeights.firstOrNull { it.categoryId == cat.id }
            cat.copy(weightPercent = custom?.weightPercent ?: 0.0)
        }
    }

    fun hasCustomUnitWeights(
        unitId: Long,
        unitCategoryWeights: List<GradeUnitCategoryWeightEntity>
    ): Boolean = unitCategoryWeights.any { it.unitId == unitId }

    fun summarize(
        categories: List<GradeCategoryEntity>,
        items: List<GradeItemEntity>,
        passingGrade: Double = 70.0
    ): GradeSummary {
        val graded = categories.mapNotNull { category ->
            categoryAverage(category.id, items)?.let { category to it }
        }
        val gradedWeight = graded.sumOf { it.first.weightPercent }
        val points = graded.sumOf { (category, average) -> average * category.weightPercent / 100.0 }
        val current = if (gradedWeight > 0) points * 100.0 / gradedWeight else null
        val remainingWeight = (100.0 - gradedWeight).coerceAtLeast(0.0)
        val needed = when {
            current == null -> passingGrade
            remainingWeight <= 0 -> if (points >= passingGrade) 0.0 else null
            else -> ((passingGrade - points) * 100.0 / remainingWeight).coerceAtLeast(0.0)
        }
        return GradeSummary(current, points, gradedWeight, needed)
    }

    fun simulate(
        categories: List<GradeCategoryEntity>,
        items: List<GradeItemEntity>,
        categoryId: Long,
        hypotheticalScore: Double
    ): Double? = summarize(
        categories,
        items + GradeItemEntity(categoryId = categoryId, name = "Simulación", score = hypotheticalScore)
    ).currentAverage

    fun unitAverage(
        categories: List<GradeCategoryEntity>,
        items: List<GradeItemEntity>,
        unitId: Long,
        unitCategoryWeights: List<GradeUnitCategoryWeightEntity> = emptyList()
    ): Double? {
        val effectiveCategories = resolveCategoriesForUnit(categories, unitId, unitCategoryWeights)
        return summarize(effectiveCategories, items.filter { it.unitId == unitId }).currentAverage
    }

    fun subjectAverage(
        subjectId: Long,
        categories: List<GradeCategoryEntity>,
        items: List<GradeItemEntity>,
        units: List<GradeUnitEntity> = emptyList(),
        unitCategoryWeights: List<GradeUnitCategoryWeightEntity> = emptyList()
    ): Double? {
        val ownCategories = categories.filter { it.subjectId == subjectId }
        val ownUnits = units.filter { it.subjectId == subjectId }
        if (ownUnits.isNotEmpty()) {
            val unitAverages = ownUnits.mapNotNull { unit ->
                unitAverage(ownCategories, items, unit.id, unitCategoryWeights)
            }
            if (unitAverages.isNotEmpty()) {
                return unitAverages.average()
            }
        }
        val ids = ownCategories.mapTo(mutableSetOf()) { it.id }
        return summarize(ownCategories, items.filter { it.categoryId in ids }).currentAverage
    }

    fun periodAverage(
        subjectIds: Collection<Long>,
        categories: List<GradeCategoryEntity>,
        items: List<GradeItemEntity>,
        units: List<GradeUnitEntity> = emptyList(),
        unitCategoryWeights: List<GradeUnitCategoryWeightEntity> = emptyList()
    ): Double? = subjectIds.mapNotNull { subjectAverage(it, categories, items, units, unitCategoryWeights) }
        .takeIf { it.isNotEmpty() }
        ?.average()
}
