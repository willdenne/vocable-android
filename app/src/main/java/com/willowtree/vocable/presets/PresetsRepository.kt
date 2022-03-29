package com.willowtree.vocable.presets

import android.content.Context
import com.willowtree.vocable.room.Category
import com.willowtree.vocable.room.Phrase
import com.willowtree.vocable.room.VocableDatabase
import org.koin.core.KoinComponent
import org.koin.core.get
import timber.log.Timber
import java.util.*

class PresetsRepository(context: Context) : KoinComponent {

    private val database = VocableDatabase.getVocableDatabase(context)

    suspend fun getAllCategories(): List<Category> {
        return database.categoryDao().getAllCategories()
    }

    suspend fun getUserGeneratedCategories(): List<Category> {
        return database.categoryDao().getUserGeneratedCategories()
    }

    suspend fun getPhrasesForCategory(categoryId: String): List<Phrase> {
        Timber.d("WILL: get phrases for $categoryId")
        return database.categoryDao().getCategoryWithPhrases(categoryId)?.phrases ?: listOf()
    }

    suspend fun addPhrase(phrase: Phrase) {
        database.phraseDao().insertPhrase(phrase)
    }

    suspend fun addPhraseToRecents(phrase: Phrase) {
        Timber.d("WILL: adding phrase called ${phrase.localizedUtterance}")
        addPhrase(
            Phrase(
                phraseId = 0L,
                parentCategoryId = PresetCategories.RECENTS.id,
                creationDate = Calendar.getInstance().timeInMillis,
                isUserGenerated = phrase.isUserGenerated,
                lastSpokenDate = Calendar.getInstance().timeInMillis,
                resourceId = phrase.resourceId,
                localizedUtterance = phrase.localizedUtterance,
                sortOrder = phrase.sortOrder
            )
        )

        /**
         * TODO: WILL:
         * Check if it exists, and if it does, update the time stamp
         * Check if there's more than X (9 or 18) recents, and if so remove the last one
         */
    }

    suspend fun addCategory(category: Category) {
        database.categoryDao().insertCategory(category)
    }

    suspend fun populateCategories(categories: List<Category>) {
        database.categoryDao().insertCategories(*categories.toTypedArray())
    }

    suspend fun populatePhrases(phrases: List<Phrase>) {
        database.phraseDao().insertPhrases(*phrases.toTypedArray())
    }

    suspend fun getPhraseById(id: String): Phrase {
        return database.phraseDao().getPhraseById(id)
    }

    suspend fun deletePhrase(phrase: Phrase) {
        database.phraseDao().deletePhrase(phrase)
    }

    suspend fun deletePhrases(phrases: List<Phrase>) {
        database.phraseDao().deletePhrases(*phrases.toTypedArray())
    }

    suspend fun deleteCategory(category: Category) {
        database.categoryDao().deleteCategory(category)
    }

    suspend fun updatePhrase(phrase: Phrase) {
        database.phraseDao().updatePhrase(phrase)
    }

    suspend fun updateCategory(category: Category) {
        database.categoryDao().updateCategory(category)
    }

    suspend fun deleteNonUserGeneratedPhrases() {
        database.phraseDao().deleteNonUserGeneratedPhrases()
    }

    suspend fun updateCategories(categories: List<Category>) {
        database.categoryDao().updateCategories(*categories.toTypedArray())
    }

    suspend fun getCategoryById(categoryId: String): Category {
        return database.categoryDao().getCategoryById(categoryId)
    }

    suspend fun getNumberOfShownCategories(): Int {
        return database.categoryDao().getNumberOfShownCategories()
    }

    //Initial DB populate
    suspend fun populateDatabase() {
        Timber.d("WILL: 2")
        PresetCategories.values().forEach { presetCategory ->
            if (presetCategory == PresetCategories.RECENTS || presetCategory == PresetCategories.USER_FAVORITES) {
                Timber.d("WILL: recent user")
            } else {
                val phrasesIds =
                    get<Context>().resources.obtainTypedArray(presetCategory.getArrayId())
                val phraseObjects = mutableListOf<Phrase>()
                for (index in 0 until phrasesIds.length()) {
                    phraseObjects.add(
                        Phrase(
                            0L,
                            presetCategory.id,
                            System.currentTimeMillis(),
                            false,
                            System.currentTimeMillis(),
                            phrasesIds.getResourceId(index, -1),
                            null,
                            phraseObjects.size
                        )
                    )
                }
                phrasesIds.recycle()
                populatePhrases(phraseObjects)
            }
            Timber.d("WILL: test boy")
            database.categoryDao().insertCategories(
                Category(
                    presetCategory.id,
                    System.currentTimeMillis(),
                    false,
                    presetCategory.getNameId(),
                    null,
                    false,
                    presetCategory.initialSortOrder
                )

            )
        }
    }
}
