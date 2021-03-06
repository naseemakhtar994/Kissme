package com.netguru.kissme

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import com.ironz.binaryprefs.encryption.AesValueEncryption
import com.ironz.binaryprefs.encryption.XorKeyEncryption


/**
 * [com.ironz.binaryprefs.Preferences] provider.
 * Provides proper instance of [com.ironz.binaryprefs.Preferences] based on [name] parameter
 */
@SuppressLint("StaticFieldLeak")
internal object AndroidStorageProvider {

    private const val DEFAULT_PREFERENCES_NAME = "default"

    /**
     * Application [Context] provided by [StorageInitializer],
     * needed for proper [com.ironz.binaryprefs.Preferences] initialization
     */
    internal lateinit var appContext: Context

    /**
     * Whether [name] parameter is specified, it returns named [com.ironz.binaryprefs.Preferences] instance.
     * In other cases, it uses default instance.
     */
    internal fun preferences(name: String? = null): Preferences {
        // Generate a new encryption key with initialization vector.
        val encryptionKeysStorage = EncryptionKeysStorageProvider.provide()

        val valuesKey = encryptionKeysStorage.getKey(EncryptionKey.VALUES_KEY, appContext)
        val valuesIv = encryptionKeysStorage.getKey(EncryptionKey.VALUES_IV, appContext)
        val keysKey = encryptionKeysStorage.getKey(EncryptionKey.KEYS_KEY, appContext)

        return BinaryPreferencesBuilder(appContext)
            .migrateFrom(getSharedPreferences(name))
            .keyEncryption(XorKeyEncryption(keysKey))
            .valueEncryption(AesValueEncryption(valuesKey, valuesIv))
            .name(name ?: DEFAULT_PREFERENCES_NAME)
            .build()
    }

    private fun getSharedPreferences(name: String?) = if (name == null) {
        PreferenceManager.getDefaultSharedPreferences(appContext)
    } else {
        appContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
}
