package io.sulek.ssml

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class SSMLLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    override fun canScrollVertically() = !SimpleSwipeMenuLayout.isSwiping
}