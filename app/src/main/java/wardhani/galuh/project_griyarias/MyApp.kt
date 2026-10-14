package wardhani.galuh.project_griyarias

import android.app.Application
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import wardhani.galuh.project_griyarias.util.ThemeHelper

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseInAppMessaging.getInstance().setMessagesSuppressed(true)
        ThemeHelper.applySavedTheme(this)
    }
}