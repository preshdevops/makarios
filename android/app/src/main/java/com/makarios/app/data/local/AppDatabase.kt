package com.makarios.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [KeptEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keptDao(): KeptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from v1 (no inWidget/inReminders columns) to v2.
         * Adds both new columns with DEFAULT 0 (false) so all existing rows keep their data.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE kept ADD COLUMN inWidget INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE kept ADD COLUMN inReminders INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE kept ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE kept SET updatedAt = createdAt")
                db.execSQL("ALTER TABLE kept ADD COLUMN deletedAt INTEGER")
                db.execSQL("ALTER TABLE kept ADD COLUMN syncState TEXT NOT NULL DEFAULT 'LOCAL'")
                db.execSQL("ALTER TABLE kept ADD COLUMN visibility TEXT NOT NULL DEFAULT 'PRIVATE'")
                db.execSQL("ALTER TABLE kept ADD COLUMN publicId TEXT")
            }
        }
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE kept ADD COLUMN format TEXT NOT NULL DEFAULT 'Story'")
                db.execSQL("ALTER TABLE kept ADD COLUMN styleOptionsJson TEXT NOT NULL DEFAULT '{}'")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "makarios.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

