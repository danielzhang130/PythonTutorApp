/*
 *     Copyright (c) 2020 danielzhang130
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.sort_it.pythontutor.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import ca.sort_it.pythontutor.R
import ca.sort_it.pythontutor.viewmodel.MainViewModel
import com.susmit.aceeditor.AceEditor
import kotlinx.android.synthetic.main.edit_fragment.*
import javax.inject.Inject

class FragmentEdit @Inject constructor() : BaseFragment() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mViewModel: MainViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mViewModel = ViewModelProviders.of(context as FragmentActivity, mViewModelFactory).get(MainViewModel::class.java)
        mViewModel.startEdit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.edit_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        code_view.setOnLoadedEditorListener {
            code_view.setTheme(AceEditor.Theme.SOLARIZED_LIGHT)
            code_view.setMode(AceEditor.Mode.Python)
            code_view.setText(mViewModel.getText().value?:"")
        }

        code_view.setResultReceivedListener { FLAG_VALUE, results ->
            if (FLAG_VALUE == AceEditor.Request.TEXT_REQUEST) {
                mViewModel.setText(results[0])
            }
        }

    }

    fun getText() = code_view.requestText()
    fun setText(code: String) {
        code_view.setText(code)
    }
}
