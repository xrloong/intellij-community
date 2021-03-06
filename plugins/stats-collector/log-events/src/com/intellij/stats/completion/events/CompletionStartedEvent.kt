// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.intellij.stats.completion.events

import com.intellij.stats.completion.Action
import com.intellij.stats.completion.LogEventVisitor
import com.intellij.stats.completion.LookupEntryInfo


class CompletionStartedEvent(
        @JvmField var ideVersion: String,
        @JvmField var pluginVersion: String,
        @JvmField var mlRankingVersion: String,
        userId: String,
        sessionId: String,
        @JvmField var language: String?,
        @JvmField var performExperiment: Boolean,
        @JvmField var experimentVersion: Int,
        completionList: List<LookupEntryInfo>,
        @JvmField var userFactors: Map<String, String?>,
        @JvmField var queryLength: Int,
        selectedPosition: Int,
        timestamp: Long)

    : LookupStateLogData(
        userId,
        sessionId,
        Action.COMPLETION_STARTED,
        completionList.map { it.id },
        completionList,
        selectedPosition,
        timestamp)
{

    //seems it's not needed, remove when possible
    @JvmField var completionListLength: Int = completionList.size
    @JvmField var isOneLineMode: Boolean = false

    @JvmField var lookupShownTime: Long = -1
    @JvmField var mlTimeContribution: Long = -1
    @JvmField var isAutoPopup: Boolean? = null

    override fun accept(visitor: LogEventVisitor) {
        visitor.visit(this)
    }
}