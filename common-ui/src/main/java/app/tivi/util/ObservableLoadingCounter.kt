/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.util

import app.tivi.base.InvokeFinished
import app.tivi.base.InvokeStarted
import app.tivi.base.InvokeStatus
import app.tivi.base.InvokeTimeout
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map

class ObservableLoadingCounter {
    private val loadingState = ConflatedBroadcastChannel(0)

    val observable: Flow<Boolean>
        get() = loadingState.asFlow().map { it > 0 }

    fun addLoader() {
        loadingState.sendBlocking(loadingState.value + 1)
    }

    fun removeLoader() {
        loadingState.sendBlocking(loadingState.value - 1)
    }
}

suspend fun ObservableLoadingCounter.collectFrom(statuses: Flow<InvokeStatus>) {
    statuses.collect {
        if (it == InvokeStarted) {
            addLoader()
        } else if (it == InvokeFinished || it == InvokeTimeout) {
            removeLoader()
        }
    }
}