package com.jacquessmuts.positivitea.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jacquessmuts.positivitea.model.TeaBag.Companion.TEABAG_TABLE

/**
 * Created by jacquessmuts on 2019-03-06
 * This is the primary model for the app and the messages delivered to the user.
 * The words "Message" and "Notification" are so overused I'm giving this a strongly unique name.
 * Renaming fields is a bad idea unless you also provide @ColumnInfo(name = "old_field_name") or do
 * a migration.
 */
@Entity(tableName = TEABAG_TABLE)
data class TeaBag(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val score: Long
) {

    companion object {
        const val TEABAG_TABLE = "teabag_table"
    }
}