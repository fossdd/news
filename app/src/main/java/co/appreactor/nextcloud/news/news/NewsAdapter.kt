package co.appreactor.nextcloud.news.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import co.appreactor.nextcloud.news.R
import kotlinx.android.synthetic.main.row_item.view.*

class NewsAdapter(
    private val rows: MutableList<NewsAdapterRow> = mutableListOf(),
    var onClick: ((NewsAdapterRow) -> Unit)? = null
) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    class ViewHolder(
        private val view: View,
        private val onClick: ((NewsAdapterRow) -> Unit)?
    ) :
        RecyclerView.ViewHolder(view) {
        fun bind(row: NewsAdapterRow, isFirst: Boolean) {
            view.apply {
                topOffset.isVisible = isFirst

                image.isVisible = false // TODO

                primaryText.text = row.title
                secondaryText.text = row.subtitle
                supportingText.text = row.summary

                primaryText.isEnabled = row.unread
                secondaryText.isEnabled = row.unread
                supportingText.isEnabled = row.unread

                card.setOnClickListener {
                    onClick?.invoke(row)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.row_item,
            parent, false
        )

        return ViewHolder(view, onClick)

    }

    override fun getItemCount(): Int {
        return rows.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            row = rows[position],
            isFirst = position == 0
        )
    }

    fun swapRows(rows: List<NewsAdapterRow>) {
        this.rows.clear()
        this.rows += rows
        notifyDataSetChanged()
    }
}