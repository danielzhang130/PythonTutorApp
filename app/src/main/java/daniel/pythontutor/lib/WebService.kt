package daniel.pythontutor.lib

import daniel.pythontutor.model.PythonVisualization
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WebService {
    @GET("web_exec_py3.py")
    fun execPy3(@Query("user_script") code: String) : Call<PythonVisualization>
}