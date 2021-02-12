package logging

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import db.LoggedException
import db.LoggedExceptionQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoggedExceptionsRepository(
    private val db: LoggedExceptionQueries
) {

    suspend fun insertOrReplace(exception: LoggedException) = withContext(Dispatchers.IO) {
        db.insertOrReplace(exception)
    }

    suspend fun selectAll() = withContext(Dispatchers.IO) {
        db.selectAll().executeAsList()
    }

    suspend fun selectCount() = withContext(Dispatchers.IO) {
        db.selectCount().asFlow().mapToOne()
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        db.deleteAll()
    }
}