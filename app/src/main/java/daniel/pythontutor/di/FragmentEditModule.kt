package daniel.pythontutor.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import daniel.pythontutor.ui.FragmentEdit

@Module
abstract class FragmentEditModule {
    @Suppress("unused")
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun fragmentEdit(): FragmentEdit
}