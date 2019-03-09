package com.jacquessmuts.positivitea.database

import androidx.room.TypeConverter
import com.jacquessmuts.positivitea.model.TeaStrength

class Converters {
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