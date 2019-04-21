package com.jacquessmuts.database.util

import androidx.room.TypeConverter
import com.jacquessmuts.core.model.TeaStrength

internal class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromTeaStrength(value: TeaStrength): Int {
            return value.strength
        }

        @TypeConverter
        @JvmStatic
        fun toTeaStrength(value: Int): TeaStrength {
            return TeaStrength(value)
        }
    }
}