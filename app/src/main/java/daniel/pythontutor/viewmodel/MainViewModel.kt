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

package daniel.pythontutor.viewmodel

import androidx.lifecycle.*
import daniel.pythontutor.lib.WebService
import daniel.pythontutor.model.OrderedMap
import daniel.pythontutor.model.PythonVisualization
import daniel.pythontutor.model.UncaughtException
import org.apache.commons.text.StringEscapeUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

// TODO https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
// TODO handle code exception
class MainViewModel @Inject constructor(private val mService: WebService) : ViewModel() {
    private var mToSubmit = false
    private val mText = MutableLiveData<String>()
    private val mVisualResult = MutableLiveData<PythonVisualization?>()
    private val mUncaughtException = MutableLiveData<UncaughtException?>()
    private val mIsLoading = MutableLiveData<Boolean>()
    private val mError = MutableLiveData<Boolean>()
    private val mGoToHeap = MutableLiveData<Int?>()

    private val mCurrentStep = MutableLiveData<Int>()

    init {
        mCurrentStep.value = 0
    }

    private val mCurrentLine = Transformations.distinctUntilChanged(
        MediatorLiveData<Int>().apply {
            value = -1
            addSource(mCurrentStep) {
                value = mVisualResult.value?.trace?.get(it)?.line?.minus(1) ?: 0
            }
        }
    )

    private val mPrevLine = Transformations.distinctUntilChanged(
        MediatorLiveData<Int>().apply {
            value = -1
            addSource(mCurrentStep) {
                if (it > 0) {
                    var prev = mVisualResult.value?.trace?.get(it - 1)
                    if (mVisualResult.value?.trace?.get(it - 1)?.event == PythonVisualization.Event.Return) {
                        val stack = mVisualResult.value?.trace?.get(it - 1)?.stack_to_render
                        if (stack?.size!! > 0) {
                            var i = it - 1
                            while (i >= 0) {
                                if (mVisualResult.value?.trace?.get(i)?.event == PythonVisualization.Event.Call &&
                                    mVisualResult.value?.trace?.get(i)?.stack_to_render?.isNotEmpty() == true &&
                                    mVisualResult.value?.trace?.get(i)?.stack_to_render?.last()?.frame_id == prev?.stack_to_render?.last()?.frame_id
                                ) {
                                    break
                                }
                                i--
                            }
                            if (i > 0) {
                                prev = mVisualResult.value?.trace?.get(i - 1)
                            }
                        }
                    }
                    value = prev?.line?.minus(1) ?: 0
                } else {
                    value = -1
                }
            }
        }
    )

    private val mStdout = Transformations.distinctUntilChanged(
        MediatorLiveData<String>().apply {
            value = ""
            addSource(mCurrentStep) {
                value = mVisualResult.value?.trace?.get(it)?.stdout ?: ""
            }
        }
    )

    private val mStack = Transformations.distinctUntilChanged(
        MediatorLiveData<List<Pair<String, OrderedMap<String, Any>>>>().apply {
            value = Collections.emptyList()
            addSource(mCurrentStep) {
                mVisualResult.value?.let { v ->
                    val stack = v.trace[it].stack_to_render
                    val list = ArrayList<Pair<String, OrderedMap<String, Any>>>()
                    for (item in stack) {
                        list.add(item.func_name to OrderedMap(item.encoded_locals, item.ordered_varnames))
                    }
                    value = list.apply { reverse() }
                }
            }
        }
    )

    private val mGlobals = Transformations.distinctUntilChanged(
        MediatorLiveData<OrderedMap<String, Any>>().apply {
            value = null
            addSource(mCurrentStep) {
                mVisualResult.value?.let { v ->
                    value = OrderedMap(
                        v.trace[it].globals,
                        v.trace[it].ordered_globals
                    )
                }
            }
        }
    )

    private val mHeapRoot = Transformations.distinctUntilChanged(
        MediatorLiveData<List<PythonVisualization.EncodedObject.Ref>>().apply {
            value = emptyList()
            addSource(mCurrentStep) {
                mVisualResult.value?.let { v ->
                    val mutableList = v.trace[it].globals
                        .filter {
                            it.value is List<*> && (it.value as List<*>).size != 0 && (it.value as List<*>)[0] == "REF"
                        }
                        .map {
                            require((it.value as List<*>).size == 2)
                            PythonVisualization.EncodedObject.Ref(((it.value as List<*>)[1] as Double).toInt())
                        }
                        .toMutableList()
                    for (stack in v.trace[it].stack_to_render) {
                        mutableList.addAll(
                            stack.encoded_locals
                                .filter {
                                    it.value is List<*> && (it.value as List<*>).size != 0 && (it.value as List<*>)[0] == "REF"
                                }
                                .map {
                                    require((it.value as List<*>).size == 2)
                                    PythonVisualization.EncodedObject.Ref(((it.value as List<*>)[1] as Double).toInt())
                                })
                    }
                    value = mutableList.distinct()
                }
            }
        }
    )

    private val mHeap = Transformations.distinctUntilChanged(
        MediatorLiveData<Map<Int, PythonVisualization.EncodedObject>>().apply {
            value = emptyMap()
            addSource(mCurrentStep) {
                mVisualResult.value?.let { v ->
                    value = v.trace[it].heap
                }
            }
        }
    )

    fun setText(value: String) {
        mText.value = StringEscapeUtils.escapeJava(value)

        if (mToSubmit && mIsLoading.value != true) {
            mToSubmit = false
            mIsLoading.value = true
            goToStart()
            mService.execPy3(StringEscapeUtils.unescapeJava(mText.value ?: ""))
                .enqueue(object : Callback<PythonVisualization> {
                    override fun onFailure(call: Call<PythonVisualization>, t: Throwable) {
                        mVisualResult.value = null
                        mIsLoading.value = false
                        mError.value = true
                    }

                    override fun onResponse(
                        call: Call<PythonVisualization>,
                        response: Response<PythonVisualization>
                    ) {
                        if (response.body()?.trace?.get(0)?.event == PythonVisualization.Event.Uncaught_Exception) {
                            mUncaughtException.value = UncaughtException(
                                response.body()?.trace?.get(0)?.line,
                                response.body()?.trace?.get(0)?.offset,
                                response.body()?.trace?.get(0)?.exception_msg
                            )
                        } else {
                            mVisualResult.value = response.body()
                        }
                        mIsLoading.value = false
                    }
                })
        }
    }

    fun getText() = mText as LiveData<String>

    fun getLines() = StringEscapeUtils.unescapeJava(mText.value ?: "").split("\n")

    fun prepareSubmission() {
        mToSubmit = true
    }

    fun startEdit() {
        mVisualResult.value = null
    }

    fun getVisualization() = mVisualResult as LiveData<PythonVisualization?>

    fun goToStart() {
        mCurrentStep.value = 0
    }

    fun goToEnd() {
        mCurrentStep.value = mVisualResult.value?.trace?.lastIndex
    }

    fun prev() {
        if (mCurrentStep.value == 0) {
            return
        }
        val tmp = mCurrentStep.value?.dec() ?: 0
        mCurrentStep.value = if (tmp < 0) 0 else tmp
    }

    fun next() {
        if (mVisualResult.value?.trace?.size == 0 || mCurrentStep.value?.plus(1) == mVisualResult.value?.trace?.size) {
            return
        }
        val tmp = mCurrentStep.value?.inc() ?: 0
        mCurrentStep.value =
            if (tmp > mVisualResult.value?.trace?.lastIndex ?: 0) mVisualResult.value?.trace?.lastIndex
                ?: 0 else tmp
    }

    fun uncaughtExceptionHandled() {
        mUncaughtException.value = null
    }

    fun errorHandled() {
        mError.value = false
    }

    fun goToHeapAt(ref: Int) {
        mGoToHeap.value = ref
    }

    fun goToHeapHandled() {
        mGoToHeap.value = null
    }

    fun getCurrentLine(): LiveData<Int> = mCurrentLine
    fun getPrevLine(): LiveData<Int> = mPrevLine
    fun getStdOut(): LiveData<String> = mStdout
    fun getStack(): LiveData<List<Pair<String, OrderedMap<String, Any>>>> = mStack
    fun getGlobals(): LiveData<OrderedMap<String, Any>> = mGlobals
    fun getHeapRoot(): LiveData<List<PythonVisualization.EncodedObject.Ref>> = mHeapRoot
    fun getHeap(): LiveData<Map<Int, PythonVisualization.EncodedObject>> = mHeap
    fun getUncaughtException() = mUncaughtException as LiveData<UncaughtException?>
    fun getLoadingState() = mIsLoading as LiveData<Boolean>
    fun getErrorState() = mError as LiveData<Boolean>
    fun getGoToHeapState() = mGoToHeap as LiveData<Int?>
}
