package io.github.crr0004.intervalme

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.selection.*
import io.github.crr0004.intervalme.database.IntervalData


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [IntervalSimpleGroupList.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [IntervalSimpleGroupList.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class IntervalSimpleGroupList : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private lateinit var mViewManager: RecyclerView.LayoutManager
    private lateinit var mAdapter: IntervalSimpleGroupAdapter
    private lateinit var mRecycleListView: RecyclerView
    private lateinit var mTracker: SelectionTracker<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_interval_simple_group_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mViewManager = LinearLayoutManager(view.context)
        mAdapter = IntervalSimpleGroupAdapter(view.context)
        mAdapter.setHasStableIds(true)

        mRecycleListView = view.findViewById(R.id.clockSampleRecycleList)

        mRecycleListView.apply {
            layoutManager = mViewManager
            adapter = mAdapter
        }

        mTracker = SelectionTracker.Builder(
                "my-selection-id",
                mRecycleListView,
                MyKeyProvider(mAdapter),
                MyDetailsLookup(mRecycleListView),
                StorageStrategy.createLongStorage())
                .withOnItemActivatedListener(myItemActivatedListener)
                .withSelectionPredicate(SelectionPredicates.createSelectSingleAnything())
                .build()
        mAdapter.setTracker(mTracker)
        mTracker.addObserver(object : SelectionTracker.SelectionObserver<Long>(){
            override fun onItemStateChanged(key: Long, selected: Boolean) {
                super.onItemStateChanged(key, selected)
                listener?.onItemSelected(mAdapter.getItemAt(key)!!, selected)
            }
        })
        super.onViewCreated(view, savedInstanceState)
    }

    private val myItemActivatedListener = fun(item: ItemDetailsLookup.ItemDetails<Long>, e: MotionEvent): Boolean{
        mTracker.select(item.selectionKey!!)
        mAdapter.notifyItemChanged(item.position)

        return true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        fun onItemSelected(interval: IntervalData, isSelected: Boolean)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment IntervalSimpleGroupList.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                IntervalSimpleGroupList().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}

class MyKeyProvider(val mAdapter: IntervalSimpleGroupAdapter) : ItemKeyProvider<Long>(1){
    override fun getKey(p0: Int): Long? {
        return mAdapter.getItemId(p0)
    }

    override fun getPosition(p0: Long): Int {
        return mAdapter.getPositionOfId(p0).toInt()
    }

}

class MyDetailsLookup(private val recycleListView: RecyclerView?) : ItemDetailsLookup<Long>() {

    override fun getItemDetails(p0: MotionEvent): ItemDetails<Long>? {
        var itemDetails: ItemDetails<Long>? = null
        val pressedView = recycleListView?.findChildViewUnder(p0.x, p0.y)
        if (pressedView != null) {
            val data = (recycleListView!!.findContainingViewHolder(pressedView) as IntervalSimpleGroupAdapter.SimpleGroupViewHolder)
            itemDetails = data.itemDetails
        }
        return itemDetails
    }

}


