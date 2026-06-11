package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// 1. Entities
@Entity(tableName = "custom_nades")
data class CustomNadeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val map: String,
    val type: String, // "Smoke", "Flash", "Molotov", "HE"
    val side: String, // "T", "CT"
    val title: String,
    val description: String,
    val throwType: String, // "Standing", "Jumpthrow", "Runthrow"
    val standingSpot: String,
    val aimSpot: String,
    val videoUrl: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_nades")
data class FavoriteNadeEntity(
    @PrimaryKey val nadeKey: String, // Either "default_{id}" or "custom_{id}"
    val savedAt: Long = System.currentTimeMillis()
)

// 2. DAOs
@Dao
interface CustomNadeDao {
    @Query("SELECT * FROM custom_nades ORDER BY timestamp DESC")
    fun getAllCustomNades(): Flow<List<CustomNadeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomNade(nade: CustomNadeEntity)

    @Query("DELETE FROM custom_nades WHERE id = :id")
    suspend fun deleteCustomNadeById(id: Int)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_nades")
    fun getAllFavorites(): Flow<List<FavoriteNadeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(fav: FavoriteNadeEntity)

    @Query("DELETE FROM favorite_nades WHERE nadeKey = :nakeKey")
    suspend fun deleteFavoriteByKey(nakeKey: String)
}

// 3. Database
@Database(entities = [CustomNadeEntity::class, FavoriteNadeEntity::class], version = 2, exportSchema = false)
abstract class NadeDatabase : RoomDatabase() {
    abstract fun customNadeDao(): CustomNadeDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: NadeDatabase? = null

        @Suppress("DEPRECATION")
        fun getDatabase(context: Context): NadeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NadeDatabase::class.java,
                    "cs_nades_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// 4. Repository (Abstracted Data Source)
class NadeRepository(private val db: NadeDatabase) {
    val customNades: Flow<List<CustomNadeEntity>> = db.customNadeDao().getAllCustomNades()
    val favorites: Flow<List<FavoriteNadeEntity>> = db.favoriteDao().getAllFavorites()

    suspend fun insertCustomNade(nade: CustomNadeEntity) {
        db.customNadeDao().insertCustomNade(nade)
    }

    suspend fun deleteCustomNade(id: Int) {
        db.customNadeDao().deleteCustomNadeById(id)
    }

    suspend fun toggleFavorite(nadeKey: String, isFav: Boolean) {
        if (isFav) {
            db.favoriteDao().insertFavorite(FavoriteNadeEntity(nadeKey))
        } else {
            db.favoriteDao().deleteFavoriteByKey(nadeKey)
        }
    }
}
