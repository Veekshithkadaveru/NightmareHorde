package app.krafted.nightmarehorde.di

import app.krafted.nightmarehorde.engine.rendering.Camera
import app.krafted.nightmarehorde.engine.rendering.SpriteRenderer
import app.krafted.nightmarehorde.game.data.AssetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GameModule {
    
    // Dependencies with @Inject constructors (Camera, SpriteRenderer) are automatically provided by Hilt

}
