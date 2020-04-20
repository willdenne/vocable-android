package com.willowtree.vocable.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.willowtree.vocable.BaseFragment
import com.willowtree.vocable.BaseViewModelFactory
import com.willowtree.vocable.R
import com.willowtree.vocable.databinding.CategoryEditButtonBinding
import com.willowtree.vocable.databinding.FragmentEditCategoriesListBinding
import com.willowtree.vocable.room.Category

class EditCategoriesListFragment : BaseFragment() {

    companion object {
        private const val KEY_START_POSITION = "KEY_START_POSITION"
        private const val KEY_END_POSITION = "KEY_END_POSITION"

        fun newInstance(
            startPosition: Int,
            endPosition: Int
        ): EditCategoriesListFragment {
            return EditCategoriesListFragment().apply {
                arguments = Bundle().apply {
                    putInt(KEY_START_POSITION, startPosition)
                    putInt(KEY_END_POSITION, endPosition)
                }
            }
        }
    }

    private var binding: FragmentEditCategoriesListBinding? = null
    private lateinit var editCategoriesViewModel: EditCategoriesViewModel
    private var maxEditCategories = 1

    private var startPosition = 0
    private var endPosition = 0

    private val editButtonList = mutableListOf<CategoryEditButtonBinding>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEditCategoriesListBinding.inflate(inflater, container, false)
        maxEditCategories = resources.getInteger(R.integer.max_edit_categories)

        startPosition = arguments?.getInt(KEY_START_POSITION, 0) ?: 0
        endPosition = arguments?.getInt(KEY_END_POSITION, 0) ?: 0

        val numberOfButtons = endPosition - startPosition

        for (i in 0 until numberOfButtons) {
            val categoryView =
                CategoryEditButtonBinding.inflate(
                    inflater,
                    binding?.categoryEditButtonContainer,
                    false
                )
            binding?.categoryEditButtonContainer?.addView(categoryView.root)

            editButtonList.add(categoryView)
        }

        // Add invisible views to fill out the rest of the space
        for (i in 0 until maxEditCategories - numberOfButtons) {
            val hiddenButton =
                CategoryEditButtonBinding.inflate(
                    inflater,
                    binding?.categoryEditButtonContainer,
                    false
                )
            binding?.categoryEditButtonContainer?.addView(hiddenButton.root.apply {
                isEnabled = false
                visibility = View.INVISIBLE
            })
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editCategoriesViewModel =
            ViewModelProviders.of(
                requireActivity(),
                BaseViewModelFactory(
                    getString(R.string.category_123_id),
                    getString(R.string.category_my_sayings_id)
                )
            ).get(EditCategoriesViewModel::class.java)
        subscribeToViewModel()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun getAllViews(): List<View> {
        return emptyList()
    }

    private fun subscribeToViewModel() {
        editCategoriesViewModel.orderCategoryList.observe(viewLifecycleOwner, Observer {
            it?.let { overallList ->
                if (startPosition >= 0 && endPosition <= overallList.size) {
                    var firstHiddenIndex = overallList.indexOfFirst { category -> category.hidden }
                    // Just default to list size if no categories are hidden
                    if (firstHiddenIndex == -1) {
                        firstHiddenIndex = overallList.size
                    }

                    overallList.subList(startPosition, endPosition)
                        .forEachIndexed { index, category ->
                            bindCategoryEditButton(
                                editButtonList[index],
                                category,
                                startPosition + index,
                                firstHiddenIndex
                            )
                        }
                }
            }
        })
    }

    private fun bindCategoryEditButton(
        editButtonBinding: CategoryEditButtonBinding,
        category: Category,
        overallIndex: Int,
        firstHiddenIndex: Int
    ) {
        with(editButtonBinding) {
            if (category.hidden) {
                categoryName.text = category.getLocalizedText()
            } else {
                categoryName.text = getString(
                    R.string.edit_categories_button_number,
                    overallIndex + 1,
                    category.getLocalizedText()
                )
            }

            hiddenView.isVisible = category.hidden

            moveCategoryUpButton.isEnabled = !category.hidden && overallIndex > 0
            moveCategoryDownButton.isEnabled =
                !category.hidden && overallIndex + 1 < firstHiddenIndex

            moveCategoryUpButton.action = {
                editCategoriesViewModel.moveCategoryUp(category)
            }
            moveCategoryDownButton.action = {
                editCategoriesViewModel.moveCategoryDown(category)
            }

            if (category?.categoryId == getString(R.string.category_my_sayings_id)) {
                editCategorySelectButton.isEnabled = false
            }

            editCategorySelectButton.action = {
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.settings_fragment_container,
                        EditCategoryOptionsFragment.newInstance(category)
                    )
                    .addToBackStack(null)
                    .commit()
                editCategoriesViewModel.onCategorySelected(category)
            }
        }
    }
}