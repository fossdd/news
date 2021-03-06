package entry

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import co.appreactor.news.R
import common.showDialog
import co.appreactor.news.databinding.FragmentEntryBinding
import db.Entry
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class EntryFragment : Fragment() {

    private val args: EntryFragmentArgs by navArgs()

    private val model: EntryFragmentModel by viewModel()

    private var _binding: FragmentEntryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NewApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.toolbar.menu.findItem(R.id.share).isVisible = false
        binding.toolbar.menu.findItem(R.id.toggleBookmarked).isVisible = false

        binding.progress.isVisible = true

        lifecycleScope.launchWhenResumed {
            val entry = model.getEntry(args.entryId)

            if (entry == null) {
                showDialog(
                    R.string.error,
                    getString(R.string.cannot_find_entry_with_id_s, args.entryId)
                ) {
                    findNavController().popBackStack()
                }

                return@launchWhenResumed
            }

            viewEntry(entry)
        }

        binding.scrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            binding.fab.isVisible = binding.scrollView.canScrollVertically(1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun viewEntry(entry: Entry) {
        binding.toolbar.title =
            model.getFeed(entry.feedId)?.title ?: getString(R.string.unknown_feed)

        binding.toolbar.menu.findItem(R.id.share).isVisible = true
        binding.toolbar.menu.findItem(R.id.toggleBookmarked).isVisible = true
        updateBookmarkedButton(entry.bookmarked)

        binding.title.text = entry.title
        binding.date.text = model.getDate(entry)

        runCatching {
            fillSummary(entry)
            binding.progress.isVisible = false
        }.onFailure {
            binding.progress.isVisible = false

            showDialog(R.string.error, R.string.cannot_show_content) {
                findNavController().popBackStack()
            }

            return
        }

        model.markAsOpened(entry)

        lifecycleScope.launchWhenResumed {
            model.getBookmarked(entry).collect { bookmarked ->
                updateBookmarkedButton(bookmarked)
            }
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.share -> {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, entry.title)
                        putExtra(Intent.EXTRA_TEXT, entry.link)
                    }

                    startActivity(Intent.createChooser(intent, ""))
                }

                R.id.toggleBookmarked -> lifecycleScope.launchWhenResumed {
                    model.toggleBookmarked(args.entryId)
                }
            }

            true
        }

        binding.fab.setOnClickListener {
            lifecycleScope.launch {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(entry.link)
                startActivity(intent)
            }
        }
    }

    private fun fillSummary(entry: Entry) {
        val summary = HtmlCompat.fromHtml(
            entry.content,
            HtmlCompat.FROM_HTML_MODE_LEGACY,
            TextViewImageGetter(binding.summaryView),
            null
        ) as SpannableStringBuilder

        if (summary.isBlank()) {
            return
        }

        summary.applyStyle(binding.summaryView)

        while (summary.contains("\u00A0")) {
            val index = summary.indexOfFirst { it == '\u00A0' }
            summary.delete(index, index + 1)
        }

        while (summary.contains("\n\n\n")) {
            val index = summary.indexOf("\n\n\n")
            summary.delete(index, index + 1)
        }

        while (summary.startsWith("\n\n")) {
            summary.delete(0, 1)
        }

        while (summary.endsWith("\n\n")) {
            summary.delete(summary.length - 2, summary.length - 1)
        }

        binding.summaryView.text = summary
        binding.summaryView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun SpannableStringBuilder.applyStyle(textView: TextView) {
        val spans = getSpans(0, length - 1, Any::class.java)

        spans.forEach {
            when (it) {
                is BulletSpan -> {
                    val radius = resources.getDimension(R.dimen.bullet_radius).toInt()
                    val gap = resources.getDimension(R.dimen.bullet_gap).toInt()

                    val span = if (Build.VERSION.SDK_INT >= 28) {
                        BulletSpan(gap, textView.currentTextColor, radius)
                    } else {
                        BulletSpan(gap, textView.currentTextColor)
                    }

                    setSpan(
                        span,
                        getSpanStart(it),
                        getSpanEnd(it),
                        0
                    )

                    removeSpan(it)
                }

                is QuoteSpan -> {
                    val color = binding.date.currentTextColor
                    val stripe = resources.getDimension(R.dimen.quote_stripe_width).toInt()
                    val gap = resources.getDimension(R.dimen.quote_gap).toInt()

                    val span = if (Build.VERSION.SDK_INT >= 28) {
                        QuoteSpan(color, stripe, gap)
                    } else {
                        QuoteSpan(color)
                    }

                    setSpan(
                        span,
                        getSpanStart(it),
                        getSpanEnd(it),
                        0
                    )

                    removeSpan(it)
                }

                is URLSpan -> {
                    if (it.url.startsWith("#")) {
                        removeSpan(it)
                    }
                }
            }
        }
    }

    private fun updateBookmarkedButton(bookmarked: Boolean) {
        val menuItem = binding.toolbar.menu.findItem(R.id.toggleBookmarked)

        if (bookmarked) {
            menuItem.setIcon(R.drawable.ic_baseline_bookmark_24)
            menuItem.setTitle(R.string.remove_bookmark)
        } else {
            menuItem.setIcon(R.drawable.ic_baseline_bookmark_border_24)
            menuItem.setTitle(R.string.bookmark)
        }
    }
}