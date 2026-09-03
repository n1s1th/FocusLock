package com.focuslock.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.focuslock.app.data.database.dao.BagDao
import com.focuslock.app.data.database.dao.RoutineDao
import com.focuslock.app.data.database.dao.SessionDao
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.data.database.entities.RoutineEntity
import com.focuslock.app.data.database.entities.SessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [BagEntity::class, RoutineEntity::class, SessionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusDatabase : RoomDatabase() {

    abstract fun bagDao(): BagDao
    abstract fun routineDao(): RoutineDao
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: FocusDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FocusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusDatabase::class.java,
                    "focus_lock_db"
                )
                    .addCallback(FocusDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class FocusDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.bagDao())
                    }
                }
            }

            suspend fun populateInitialData(bagDao: BagDao) {
                // Pre-seed default Bags
                bagDao.insertBag(
                    BagEntity(
                        name = "Deep Focus",
                        iconName = "Work",
                        colorHex = "#6366F1",
                        allowedPackages = listOf(
                            "com.google.android.dialer",
                            "com.android.dialer",
                            "com.google.android.deskclock",
                            "com.android.deskclock",
                            "com.google.android.calculator",
                            "com.android.calculator2"
                        ),
                        isDefault = true
                    )
                )

                bagDao.insertBag(
                    BagEntity(
                        name = "Study & Read",
                        iconName = "Book",
                        colorHex = "#10B981",
                        allowedPackages = listOf(
                            "com.google.android.apps.docs",
                            "com.google.android.keep",
                            "com.google.android.apps.books"
                        ),
                        isDefault = false
                    )
                )

                bagDao.insertBag(
                    BagEntity(
                        name = "Minimalist Phone",
                        iconName = "Lock",
                        colorHex = "#EC4899",
                        allowedPackages = listOf(
                            "com.google.android.dialer",
                            "com.android.dialer"
                        ),
                        isDefault = false
                    )
                )
            }
        }
    }
}
