package daniel.pythontutor.repository

import daniel.pythontutor.lib.WebService
import javax.inject.Inject

class Repo @Inject constructor(private val mService: WebService) {
    fun visualize(code: String) = mService.execPy3(code)
}

