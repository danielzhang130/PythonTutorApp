package daniel.pythontutor.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import daniel.pythontutor.R
import daniel.pythontutor.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.stdout.*

class FragmentStdout : Fragment() {
    private lateinit var mViewModel: MainViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mViewModel = ViewModelProviders.of(context as FragmentActivity).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.stdout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mViewModel.getStdOut().observe(this, Observer {
            stdout.text = it
        })
    }
}
