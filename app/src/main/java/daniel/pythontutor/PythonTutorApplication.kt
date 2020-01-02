package daniel.pythontutor

import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import daniel.pythontutor.di.DaggerApplicationComponent


class PythonTutorApplication : DaggerApplication() {
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.factory().create(applicationContext)
    }
}