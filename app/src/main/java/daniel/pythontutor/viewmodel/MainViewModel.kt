package daniel.pythontutor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import daniel.pythontutor.lib.WebService
import daniel.pythontutor.model.OrderedMap
import daniel.pythontutor.model.PythonVisualization
import org.apache.commons.text.StringEscapeUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// TODO https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate
class MainViewModel @Inject constructor(private val mService: WebService) : ViewModel() {
    private var mToSubmit = false
    private val mText = MutableLiveData<String>()
    private val mVisualResult = MutableLiveData<PythonVisualization?>()

    private val mCurrentStep = MutableLiveData<Int>()

    init {
        mCurrentStep.value = 0
    }

    private val mCurrentLine = MediatorLiveData<Int>()

    init {
        mCurrentLine.value = -1
        mCurrentLine.addSource(mCurrentStep) {
            mCurrentLine.value = mVisualResult.value?.trace?.get(it)?.line?.minus(1) ?: 0
        }
    }

    private val mPrevLine = MediatorLiveData<Int>()

    init {
        mPrevLine.value = -1
        mPrevLine.addSource(mCurrentStep) {
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
                mPrevLine.value = prev?.line?.minus(1) ?: 0
            } else {
                mPrevLine.value = -1
            }
        }
    }

    private val mStdout = MediatorLiveData<String>()

    init {
        mStdout.value = ""
        mStdout.addSource(mCurrentStep) {
            mStdout.value = mVisualResult.value?.trace?.get(it)?.stdout ?: ""
        }
    }

    private val mStack = MediatorLiveData<OrderedMap<String, OrderedMap<String, Any>>>()

    init {
        mStack.value = OrderedMap(Collections.emptyMap(), Collections.emptyList())
        mStack.addSource(mCurrentStep) {
            mVisualResult.value?.let { v ->
                val stack = v.trace[it].stack_to_render
                val map = HashMap<String, OrderedMap<String, Any>>()
                val order = ArrayList<String>()
                for (item in stack) {
                    order.add(item.func_name)
                    map[item.func_name] = OrderedMap(item.encoded_locals, item.ordered_varnames)
                }
                mStack.value = OrderedMap(map, order)
            }
        }
    }

    private val mGlobals = MediatorLiveData<OrderedMap<String, Any>>()

    init {
        mGlobals.value = null
        mGlobals.addSource(mCurrentStep) {
            mVisualResult.value?.let { v ->
                mGlobals.value = OrderedMap(
                    v.trace[it].globals,
                    v.trace[it].ordered_globals
                )
            }
        }
    }

    private val mHeapRoot = MediatorLiveData<List<PythonVisualization.EncodedObject.Ref>>()

    init {
        mHeapRoot.value = emptyList()
        mHeapRoot.addSource(mCurrentStep) {
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
                mHeapRoot.value = mutableList
            }
        }
    }

    private val mHeap = MediatorLiveData<Map<Int, PythonVisualization.EncodedObject>>()

    init {
        mHeap.value = emptyMap()
        mHeap.addSource(mCurrentStep) {
            mVisualResult.value?.let { v ->
                mHeap.value = v.trace[it].heap
            }
        }
    }

    fun setText(value: String) {
        mText.value = StringEscapeUtils.escapeJava(value)

        if (mToSubmit) {
            mToSubmit = false
            goToStart()
            mService.execPy3(StringEscapeUtils.unescapeJava(mText.value ?: ""))
                .enqueue(object : Callback<PythonVisualization> {
                    override fun onFailure(call: Call<PythonVisualization>, t: Throwable) {
                        mVisualResult.value = null
                    }

                    override fun onResponse(
                        call: Call<PythonVisualization>,
                        response: Response<PythonVisualization>
                    ) {
                        mVisualResult.value = response.body()
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

    fun getCurrentLine() = mCurrentLine as LiveData<Int>
    fun getPrevLine() = mPrevLine as LiveData<Int>
    fun getStdOut() = mStdout as LiveData<String>
    fun getStack() = mStack as LiveData<OrderedMap<String, OrderedMap<String, Any>>>
    fun getGlobals() = mGlobals as LiveData<OrderedMap<String, Any>>
    fun getHeapRoot() = mHeapRoot as LiveData<List<PythonVisualization.EncodedObject.Ref>>
    fun getHeap() = mHeap as LiveData<Map<Int, PythonVisualization.EncodedObject>>
}
