package com.jacquessmuts.database

import androidx.room.TypeConverter

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