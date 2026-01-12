// SmartSpendDatabase.kt
package com.example.smartspend2.storage

/*import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.smartspend2.models.Transaction
import com.example.smartspend2.dao.TransactionDao

@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class SmartSpendDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: SmartSpendDatabase? = null

        fun getDatabase(context: Context): SmartSpendDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartSpendDatabase::class.java,
                    "smart_spend_database"
                )
                    .fallbackToDestructiveMigration(false)  // Handles schema changes without crashing
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}*/
