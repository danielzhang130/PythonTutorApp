package daniel.pythontutor.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import daniel.pythontutor.R
import daniel.pythontutor.adapter.StackAdapter
import daniel.pythontutor.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.stack.*

class FragmentStack : Fragment() {
    private lateinit var mViewModel: MainViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mViewModel = ViewModelProviders.of(context as FragmentActivity).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.stack, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val stackAdapter = StackAdapter(context!!)
        stack_layout.layoutManager = LinearLayoutManager(context)
        stack_layout.adapter = stackAdapter

        mViewModel.getGlobals().observe(this, Observer {
            stackAdapter.setGlobal(it)
            stack_layout.scrollToPosition(0)
        })
        mViewModel.getStack().observe(this, Observer {
            stackAdapter.setStack(it)
            stack_layout.scrollToPosition(0)
        })
    }
}
