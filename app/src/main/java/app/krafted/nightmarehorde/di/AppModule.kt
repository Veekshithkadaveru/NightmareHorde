package app.krafted.nightmarehorde.di

import android.content.Context
import app.krafted.nightmarehorde.NightmareHordeApp
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Add global providers here as needed
}
