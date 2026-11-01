package app.overwinter

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.overwinter.data.Settings
import app.overwinter.ui.Root
import app.overwinter.ui.theme.OverwinterTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handle(intent)
        setContent {
            val settings: Settings? by container.settings.settings.collectAsStateWithLifecycle(initialValue = null)
            val ready by container.ready.collectAsStateWithLifecycle()
            if (ready) settings?.let { s -> OverwinterTheme(s.theme) { Root(s) } }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    /** A term notification opens the observation straight away. */
    private fun handle(i: Intent?) {
        if (i?.getBooleanExtra(OBSERVE, false) == true) container.observe.value = true
    }

    companion object {
        const val OBSERVE = "observe"
    }
}
