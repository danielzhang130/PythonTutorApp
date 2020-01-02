package daniel.pythontutor.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import daniel.pythontutor.lib.EncodedObjectDeserializer
import daniel.pythontutor.lib.EventDeserializer
import daniel.pythontutor.lib.WebService
import daniel.pythontutor.model.PythonVisualization
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