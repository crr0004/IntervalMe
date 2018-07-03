package io.github.crr0004.intervalme

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import io.github.crr0004.intervalme.views.IntervalAddSharedModel
import kotlinx.android.synthetic.main.fragment_interval_properties_edit.view.*


class IntervalPropertiesEditFragment : Fragment() {
    private var listener: IntervalPropertiesEditFragmentInteractionI? = null
    private lateinit var mModel: IntervalAddSharedModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mModel = ViewModelProviders.of(this.activity!!).get(IntervalAddSharedModel::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_interval_properties_edit, container, false)
    }

    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mModel.mIntervalToEditProperties.observe(this, Observer {
            view.intervalPropertiesLoopsTxt.setText(it?.loops.toString())
        })
        view.findViewById<EditText>(R.id.intervalPropertiesLoopsTxt).addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                try {
                    mModel.intervalToEditProperties.loops = p0.toString().toInt()
                }catch (e: NumberFormatException){}
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IntervalPropertiesEditFragmentInteractionI) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement IntervalPropertiesEditFragmentInteractionI")
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
    interface IntervalPropertiesEditFragmentInteractionI {
        fun onFragmentInteraction(uri: Uri)
    }
}
