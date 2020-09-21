package co.appreactor.news.feeds

import co.appreactor.news.api.FeedJson
import co.appreactor.news.api.NewsApi
import co.appreactor.news.api.PostFeedArgs
import co.appreactor.news.api.PutFeedRenameArgs
import co.appreactor.news.db.Feed
import co.appreactor.news.db.FeedQueries
import co.appreactor.news.entries.EntriesRepository
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@Suppress("BlockingMethodInNonBlockingContext")
class FeedsRepository(
    private val db: FeedQueries,
    private val api: NewsApi,
    private val entriesRepository: EntriesRepository
) {

    suspend fun add(url: String) = withContext(Dispatchers.IO) {
        val response = kotlin.runCatching {
            api.postFeed(PostFeedArgs(url, 0)).execute().body()!!
        }.getOrThrow()

        val feed = response.feeds.single().toFeed()

        if (feed != null) {
            db.insertOrReplace(feed)
        }
    }

    suspend fun getAll() = withContext(Dispatchers.IO) {
        db.selectAll().asFlow().mapToList()
    }

    suspend fun get(id: String) = withContext(Dispatchers.IO) {
        db.selectById(id).asFlow().mapToOneOrNull()
    }

    suspend fun rename(feedId: String, newTitle: String) = withContext(Dispatchers.IO) {
        val response = api.putFeedRename(feedId.toLong(), PutFeedRenameArgs(newTitle)).execute()

        if (response.isSuccessful) {
            val feed = get(feedId).first()

            if (feed != null) {
                db.insertOrReplace(feed.copy(title = newTitle))
            }
        } else {
            throw Exception("HTTPS request failed with error code ${response.code()}")
        }
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        val response = api.deleteFeed(id.toLong()).execute()

        if (response.isSuccessful) {
            db.transaction {
                db.deleteById(id)
                entriesRepository.deleteByFeedId(id)
            }
        } else {
            throw Exception("HTTPS request failed with error code ${response.code()}")
        }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        db.deleteAll()
    }

    suspend fun sync() = withContext(Dispatchers.IO) {
        val cachedFeeds = getAll().first().sortedBy { it.id }
        val newFeeds = api.getFeeds().execute().body()!!.feeds.sortedBy { it.id }

        if (newFeeds != cachedFeeds) {
            db.transaction {
                db.deleteAll()

                newFeeds.mapNotNull { it.toFeed() }.forEach {
                    db.insertOrReplace(it)
                }
            }
        }
    }

    private fun FeedJson.toFeed(): Feed? {
        return Feed(
            id = id?.toString() ?: return null,
            title = title ?: "Untitled",
            link = url ?: "",
            alternateLink = link ?: "",
            alternateLinkType = "text/html",
        )
    }
}