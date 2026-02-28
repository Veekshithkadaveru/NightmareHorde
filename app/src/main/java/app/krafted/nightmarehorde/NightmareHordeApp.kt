package app.krafted.nightmarehorde

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import java.util.UUID

@HiltAndroidApp
class NightmareHordeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val crashlytics = FirebaseCrashlytics.getInstance()

        // Set a per-install session ID to correlate crashes from the same device
        val prefs = getSharedPreferences("crashlytics_prefs", MODE_PRIVATE)
        val installId = prefs.getString("install_id", null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString("install_id", it).apply()
        }
        crashlytics.setUserId(installId)

        // Track app lifecycle state so crash reports show foreground vs background
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                crashlytics.setCustomKey("app_state", "foreground")
                crashlytics.setCustomKey("current_activity", activity.javaClass.simpleName)
            }

            override fun onActivityPaused(activity: Activity) {
                crashlytics.setCustomKey("app_state", "background")
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }
}
