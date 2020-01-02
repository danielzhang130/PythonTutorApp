package daniel.pythontutor.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import daniel.pythontutor.ui.FragmentVisualization

@Module
abstract class FragmentVisualizationModule {
    @Suppress("unused")
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun fragmentVisualization(): FragmentVisualization
}