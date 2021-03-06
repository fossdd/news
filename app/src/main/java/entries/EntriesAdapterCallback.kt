package entries

interface EntriesAdapterCallback {

    fun onItemClick(item: EntriesAdapterItem)

    fun onDownloadPodcastClick(item: EntriesAdapterItem)

    fun onPlayPodcastClick(item: EntriesAdapterItem)
}