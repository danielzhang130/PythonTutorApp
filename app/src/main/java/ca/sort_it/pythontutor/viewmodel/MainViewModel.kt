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

package ca.sort_it.pythontutor.viewmodel

import androidx.lifecycle.*
import ca.sort_it.pythontutor.lib.WebService
import ca.sort_it.pythontutor.model.OrderedMap
import ca.sort_it.pythontutor.model.PythonVisualization
import ca.sort_it.pythontutor.model.UncaughtException
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
    private val mText = MutableLiveData<String>()
    private val mVisualResult = MutableLiveData<PythonVisualization?>()
    private val mUncaughtException = MutableLiveData<UncaughtException?>()
    val uncaughtException = mUncaughtException as LiveData<UncaughtException?>
    private val mIsLoading = MutableLiveData<Boolean>()
    val loadingState = mIsLoading as LiveData<Boolean>
    private val mNewResult = MutableLiveData<Boolean>()
    val newResult = mNewResult as LiveData<Boolean>
    private val mError = MutableLiveData<Boolean>()
    val errorState = mError as LiveData<Boolean>
    private val mGoToHeap = MutableLiveData<Int?>()
    val goToHeapState = mGoToHeap as LiveData<Int?>

    private val mCurrentStep = MutableLiveData<Int>().apply {
        value = 0
    }
    val currentStep = mCurrentStep as LiveData<Int>

    private val mTotalSteps = MediatorLiveData<Int>().apply {
        value = -1
        addSource(mVisualResult) {
            value = it?.trace?.size
        }
    }
    val totalSteps = mTotalSteps as LiveData<Int>

    val currentLine = Transformations.distinctUntilChanged(
        MediatorLiveData<Int>().apply {
            value = -1
            addSource(mCurrentStep) {
                value = mVisualResult.value?.trace?.get(it)?.line?.minus(1) ?: 0
            }
        }
    )

    val prevLine = Transformations.distinctUntilChanged(
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

    val stdout = Transformations.distinctUntilChanged(
        MediatorLiveData<String>().apply {
            value = ""
            addSource(mCurrentStep) {
                value = mVisualResult.value?.trace?.get(it)?.stdout ?: ""
            }
        }
    )

    val stack = Transformations.distinctUntilChanged(
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

    val globals = Transformations.distinctUntilChanged(
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

    val heapRoot = Transformations.distinctUntilChanged(
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

    val heap = Transformations.distinctUntilChanged(
        MediatorLiveData<Map<Int, PythonVisualization.EncodedObject>>().apply {
            value = emptyMap()
            addSource(mCurrentStep) {
                mVisualResult.value?.let { v ->
                    value = v.trace[it].heap
                }
            }
        }
    )

    private val mCharSet = MutableLiveData<MutableSet<Char>>()
    private val mChars = MutableLiveData<LinkedList<Char>>()
    val chars = mChars as LiveData<LinkedList<Char>>
    init {
        val tmp = mutableSetOf('[', ']', '(', ')', '{', '}')
        mCharSet.value = tmp
        mChars.value = LinkedList(tmp)
    }

    fun setKeyTyped(char: Char) {
        if (char.toInt() in 33..47 || char.toInt() in 58..64 || char.toInt() in 91..96 || char.toInt() in 123..126) {
            if (mCharSet.value?.contains(char) == true) {
                if (mChars.value?.first != char) {
                    mChars.value?.let {
                        it.remove(char)
                        it.addFirst(char)
                        mChars.value = it
                    }
                }
            } else {
                mCharSet.value = mCharSet.value?.apply { add(char) }
                mChars.value = mChars.value?.apply { addFirst(char) }
                if (mChars.value?.size == 11) {
                    val value = mChars.value
                    mCharSet.value = mCharSet.value?.apply { remove(value?.removeLast()) }
                    mChars.value = value
                }
            }
        }
    }

    fun setText(value: String) {
        mText.value = StringEscapeUtils.escapeJava(value)
    }

    fun getText() = mText as LiveData<String>

    fun getLines() = StringEscapeUtils.unescapeJava(mText.value ?: "").split("\n")

    fun submit() {
        if (mText.value.isNullOrBlank()) return
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
                        mNewResult.value = true
                        mVisualResult.value = response.body()
                    }
                    mIsLoading.value = false
                }
            })
    }

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

    fun newResultReceived() {
        mNewResult.value = false
    }
}
