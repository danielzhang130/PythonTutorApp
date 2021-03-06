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

package ca.sort_it.pythontutor.di

import androidx.lifecycle.ViewModel
import ca.sort_it.pythontutor.ui.FragmentVisualization
import ca.sort_it.pythontutor.viewmodel.FragmentVisualizationViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class FragmentVisualizationModule {
    @Suppress("unused")
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun fragmentVisualization(): FragmentVisualization

    @Suppress("unused")
    @Binds
    @IntoMap
    @ViewModelKey(FragmentVisualizationViewModel::class)
    abstract fun bindFragmentVisualizationMainViewModel(viewmodel: FragmentVisualizationViewModel): ViewModel
}