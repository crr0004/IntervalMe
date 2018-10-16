package io.github.crr0004.intervalme

import android.content.Context
import android.widget.ExpandableListAdapter

class DragDropAnimationController<T>(context: Context, private val mViewSource: DragDropViewSource<T>) {

    fun swapItems(item1: T, item2: T){
        mViewSource.swapItems(item1, item2)
    }

    interface DragDropViewSource<T>{
        fun getAdapter(): ExpandableListAdapter
        fun swapItems(item1: T, item2: T)
    }
}
