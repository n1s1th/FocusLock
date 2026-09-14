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
import com.focuslock.app.data.database.dao.ReelBlockAppDao
import com.focuslock.app.data.database.entities.BagEntity
import com.focuslock.app.data.database.entities.ReelBlockAppEntity
import com.focuslock.app.data.database.entities.RoutineEntity
import com.focuslock.app.data.database.entities.SessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.room.migration.Migration
import kotlinx.coroutines.launch

@Database(
    entities = [BagEntity::class, RoutineEntity::class, SessionEntity::class, ReelBlockAppEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FocusDatabase : RoomDatabase() {

    abstract fun bagDao(): BagDao
    abstract fun routineDao(): RoutineDao
    abstract fun sessionDao(): SessionDao
    abstract fun reelBlockAppDao(): ReelBlockAppDao

    companion object {
        @Volatile
        private var INSTANCE: FocusDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `reel_block_apps` (" +
                            "`packageName` TEXT NOT NULL, " +
                            "`appName` TEXT NOT NULL, " +
                            "`blockReels` INTEGER NOT NULL, " +
                            "`blockStories` INTEGER NOT NULL, " +
                            "`blockMarketplace` INTEGER NOT NULL, " +
                            "`blockGaming` INTEGER NOT NULL, " +
                            "`dailyLimitMinutes` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`packageName`))"
                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): FocusDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FocusDatabase::class.java,
                    "focus_lock_db"
                )
                    .addMigrations(MIGRATION_1_2)
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
                // Pre-seed 3 empty default Bags
                bagDao.insertBag(
                    BagEntity(
                        name = "Bag 1",
                        iconName = "Work",
                        colorHex = "#FF5500",
                        allowedPackages = emptyList(),
                        isDefault = true
                    )
                )

                bagDao.insertBag(
                    BagEntity(
                        name = "Bag 2",
                        iconName = "Book",
                        colorHex = "#1F1E1D",
                        allowedPackages = emptyList(),
                        isDefault = false
                    )
                )

                bagDao.insertBag(
                    BagEntity(
                        name = "Bag 3",
                        iconName = "Lock",
                        colorHex = "#1F1E1D",
                        allowedPackages = emptyList(),
                        isDefault = false
                    )
                )
            }
        }
    }
}
