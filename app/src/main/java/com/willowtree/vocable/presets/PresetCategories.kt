package com.willowtree.vocable.presets

import com.willowtree.vocable.R

enum class PresetCategories(val id: String) {
    GENERAL("preset_general_category_id"),
    BASIC_NEEDS("preset_basic_needs_category_id"),
    CONVERSATION("preset_conversation_category_id"),
    ENVIRONMENT("preset_environment_category_id"),
    PERSONAL_CARE("preset_personal_care_category_id");

    fun getArrayId(): Int {
        return when (this) {
            GENERAL -> R.array.category_general
            BASIC_NEEDS -> R.array.category_basic_needs
            CONVERSATION -> R.array.category_conversation
            ENVIRONMENT -> R.array.category_environment
            PERSONAL_CARE -> R.array.category_personal_care
        }
    }

    fun getNameId(): Int {
        return when (this) {
            GENERAL -> R.string.preset_general
            BASIC_NEEDS -> R.string.preset_basic_needs
            CONVERSATION -> R.string.preset_conversation
            ENVIRONMENT -> R.string.preset_environment
            PERSONAL_CARE -> R.string.preset_personal_care
        }
    }
}