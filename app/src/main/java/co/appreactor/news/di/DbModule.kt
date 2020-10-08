package co.appreactor.news.di

import co.appreactor.news.Database
import co.appreactor.news.common.App
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import org.koin.dsl.module

val dbModule = module {

    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = Database.Schema,
            context = get(),
            name = App.DB_FILE_NAME
        )
    }

    single { Database(get()) }
    single { get<Database>().feedQueries }
    single { get<Database>().entryQueries }
    single { get<Database>().entryImagesMetadataQueries }
    single { get<Database>().entryImageQueries }
    single { get<Database>().entryEnclosureQueries }
    single { get<Database>().preferenceQueries }
    single { get<Database>().loggedExceptionQueries }
}