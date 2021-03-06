/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.wear.wearcomplicationproviderstestsuite

import android.content.ComponentName
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService
import androidx.datastore.core.DataStore

/**
 * A complication provider that supports only [ComplicationData.TYPE_LARGE_IMAGE] and cycles
 * between a couple of images on tap.
 *
 * Note: This subclasses [SuspendingComplicationProviderService] instead of [ComplicationProviderService] to support
 * coroutines, so data operations (specifically, calls to [DataStore]) can be supported directly in the
 * [onComplicationUpdate].
 * See [SuspendingComplicationProviderService] for the implementation details.
 *
 * If you don't perform any suspending operations to update your complications, you can subclass
 * [ComplicationProviderService] and override [onComplicationUpdate] directly.
 * (see [NoDataProviderService] for an example)
 */
class LargeImageProviderService : SuspendingComplicationProviderService() {
    override suspend fun onComplicationUpdateImpl(complicationId: Int, type: Int, manager: ComplicationManager) {
        if (type != ComplicationData.TYPE_LARGE_IMAGE) {
            manager.noUpdateRequired(complicationId)
            return
        }
        val args = ComplicationToggleArgs(
            providerComponent = ComponentName(this, javaClass),
            complicationId = complicationId
        )

        // On many watch faces a large image complication might not respond to taps as the
        // complication is used to provide the background for the watch. Providers should not rely
        // on tap functionality for large image complications, but the tap action is still included
        // here in case it is supported.
        val complicationTogglePendingIntent =
            ComplicationToggleReceiver.getComplicationToggleIntent(
                context = this,
                args = args
            )
        // Suspending function to retrieve the complication's state
        val state = args.getState(this)
        val data: ComplicationData = ComplicationData.Builder(type)
            .setTapAction(complicationTogglePendingIntent)
            .apply {
                when (state.mod(2)) {
                    0 -> {
                        setLargeImage(Icon.createWithResource(this@LargeImageProviderService, R.drawable.aquarium))
                    }
                    1 -> {
                        setLargeImage(Icon.createWithResource(this@LargeImageProviderService, R.drawable.outdoors))
                    }
                }
            }
            .build()
        manager.updateComplicationData(complicationId, data)
    }
}
