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
 * A complication provider that supports only [ComplicationData.TYPE_SMALL_IMAGE] and cycles
 * between the different image styles on tap.
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
class SmallImageProviderService : SuspendingComplicationProviderService() {
    override suspend fun onComplicationUpdateImpl(complicationId: Int, type: Int, manager: ComplicationManager) {
        if (type != ComplicationData.TYPE_SMALL_IMAGE) {
            manager.noUpdateRequired(complicationId)
            return
        }
        val args = ComplicationToggleArgs(
            providerComponent = ComponentName(this, javaClass),
            complicationId = complicationId
        )
        val complicationTogglePendingIntent =
            ComplicationToggleReceiver.getComplicationToggleIntent(
                context = this,
                args = args
            )
        // Suspending function to retrieve the complication's state
        val state = args.getState(this)
        val data = ComplicationData.Builder(type)
            .setTapAction(complicationTogglePendingIntent)
            .apply {
                when (state.mod(2)) {
                    0 -> {
                        // An image using IMAGE_STYLE_PHOTO may be cropped to fill the space given to it.
                        setSmallImage(Icon.createWithResource(this@SmallImageProviderService, R.drawable.aquarium))
                    }
                    1 -> {
                        // An image using IMAGE_STYLE_ICON must not be cropped, and should fit within the
                        // space given to it.
                        setSmallImage(
                            Icon.createWithResource(
                                this@SmallImageProviderService,
                                R.drawable.ic_launcher
                            )
                        )
                        setImageStyle(ComplicationData.IMAGE_STYLE_ICON)
                    }
                }
            }
            .build()
        manager.updateComplicationData(complicationId, data)
    }
}
