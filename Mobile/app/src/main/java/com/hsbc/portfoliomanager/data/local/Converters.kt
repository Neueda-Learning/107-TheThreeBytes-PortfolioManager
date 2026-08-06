package com.hsbc.portfoliomanager.data.local

import androidx.room.TypeConverter
import com.hsbc.portfoliomanager.data.model.AssetType
import java.math.BigDecimal

class Converters {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.toBigDecimalOrNull()
    }

    @TypeConverter
    fun fromAssetType(value: AssetType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAssetType(value: String?): AssetType? {
        return value?.let { AssetType.valueOf(it) }
    }
    
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus? {
        return value?.let { SyncStatus.valueOf(it) }
    }
}
