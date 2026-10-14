package wardhani.galuh.project_griyarias

import android.app.Application
import com.google.firebase.inappmessaging.FirebaseInAppMessaging

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Default: pop-up FIAM ditekan, jadi tidak muncul di halaman login.
        // Baru dibuka di ClientDashboardActivity.
        FirebaseInAppMessaging.getInstance().setMessagesSuppressed(true)
    }
}