package exceptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.appreactor.news.R
import co.appreactor.news.databinding.FragmentAppExceptionsBinding
import common.Result
import entries.EntriesAdapterDecoration
import kotlinx.coroutines.flow.collect
import org.koin.android.viewmodel.ext.android.viewModel

class AppExceptionsFragment : Fragment() {

    private val model: AppExceptionsFragmentModel by viewModel()

    private var _binding: FragmentAppExceptionsBinding? = null
    private val binding get() = _binding!!

    private val adapter = AppExceptionsAdapter {
        findNavController().navigate(
            AppExceptionsFragmentDirections.actionExceptionsFragmentToExceptionFragment(
                exceptionId = it.id
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppExceptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.apply {
            setNavigationOnClickListener {
                findNavController().popBackStack()
            }

            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.delete -> lifecycleScope.launchWhenResumed { model.deleteAllItems() }
                }

                true
            }
        }

        binding.listView.setHasFixedSize(true)
        binding.listView.layoutManager = LinearLayoutManager(requireContext())
        binding.listView.adapter = adapter
        binding.listView.addItemDecoration(
            EntriesAdapterDecoration(
                resources.getDimensionPixelSize(
                    R.dimen.entries_cards_gap
                )
            )
        )

        lifecycleScope.launchWhenResumed {
            model.onViewReady()
        }

        lifecycleScope.launchWhenResumed {
            model.items.collect { result ->
                binding.progress.isVisible = false
                binding.empty.isVisible = false

                when (result) {
                    is Result.Progress -> {
                        binding.progress.isVisible = true

                        binding.progress.alpha = 0f
                        binding.progress.animate().alpha(1f).duration = 1000
                    }

                    is Result.Success -> {
                        if (result.data.isEmpty()) {
                            binding.empty.isVisible = true
                            binding.empty.alpha = 0f
                            binding.empty.animate().alpha(1f).duration = 250
                        }

                        adapter.submitList(result.data)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}