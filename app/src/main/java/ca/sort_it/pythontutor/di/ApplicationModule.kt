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

import ca.sort_it.pythontutor.lib.EncodedObjectDeserializer
import ca.sort_it.pythontutor.lib.EventDeserializer
import ca.sort_it.pythontutor.lib.WebService
import ca.sort_it.pythontutor.model.PythonVisualization
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
object ApplicationModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideWebService(): WebService {
        val gson: Gson
        val builder = GsonBuilder()
        builder
            .registerTypeAdapter(PythonVisualization.Event::class.java, EventDeserializer())
            .registerTypeHierarchyAdapter(PythonVisualization.EncodedObject::class.java, EncodedObjectDeserializer())
        gson = builder.create()

        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://www.pythontutor.com/")
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        return retrofit.create(WebService::class.java)
    }

}