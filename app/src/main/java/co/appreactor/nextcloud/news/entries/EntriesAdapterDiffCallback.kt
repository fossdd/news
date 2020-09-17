package co.appreactor.nextcloud.news.entries

import androidx.recyclerview.widget.DiffUtil

class EntriesAdapterDiffCallback : DiffUtil.ItemCallback<EntriesAdapterItem>() {

    override fun areItemsTheSame(oldItem: EntriesAdapterItem, newItem: EntriesAdapterItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: EntriesAdapterItem, newItem: EntriesAdapterItem): Boolean {
        return oldItem.title == newItem.title
                && oldItem.subtitle == newItem.subtitle
                && oldItem.unread == newItem.unread
                && oldItem.podcast == newItem.podcast
                && oldItem.showImage == newItem.showImage
                && oldItem.cropImage == newItem.cropImage
    }
}