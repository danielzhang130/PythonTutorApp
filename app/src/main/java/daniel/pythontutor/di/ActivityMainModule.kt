package daniel.pythontutor.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import daniel.pythontutor.ui.ActivityMain
import daniel.pythontutor.viewmodel.MainViewModel

@Module
abstract class ActivityMainModule {
    @Suppress("unused")
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun activityMain(): ActivityMain

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewmodel: MainViewModel): ViewModel
}