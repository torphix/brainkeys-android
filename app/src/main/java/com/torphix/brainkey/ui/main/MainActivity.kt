package com.torphix.brainkey.ui.main
import android.app.ActivityManager
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.getSystemService
import com.torphix.brainkey.DownloadableLlm
import com.torphix.brainkey.ui.theme.RootAndroidTheme
import java.io.File
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.torphix.brainkey.ui.model_manager.LocalModelManagerScreen
import androidx.compose.material3.ExperimentalMaterial3Api
import com.torphix.brainkey.repository.KeyboardSettingsRepository
import com.torphix.brainkey.services.llm.RemoteLlm
import com.torphix.brainkey.ui.home.HomeScreen
import com.torphix.brainkey.ui.prompts.PromptManagerScreen


class MainActivity(
    activityManager: ActivityManager? = null,
    downloadManager: DownloadManager? = null,
//    clipboardManager: ClipboardManager? = null,
): ComponentActivity() {
    private val tag: String? = this::class.simpleName

    private val activityManager by lazy { activityManager ?: getSystemService<ActivityManager>()!! }
    private val downloadManager by lazy { downloadManager ?: getSystemService<DownloadManager>()!! }
    private val keyboardSettingsRepository = KeyboardSettingsRepository(context=this)

    private val viewModel: MainViewModel by viewModels()

    // Get a MemoryInfo object for the device's current memory status.
    private fun availableMemory(): ActivityManager.MemoryInfo {
        return ActivityManager.MemoryInfo().also { memoryInfo ->
            activityManager.getMemoryInfo(memoryInfo)
        }
    }
    private fun isUsingKeyboard(): Boolean {
        val currentKeyboard = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        val pianoKeyboard = "$packageName/.services.KeyboardIME"
        return currentKeyboard == pianoKeyboard
    }
    private fun isKeyboardEnabled(): Boolean {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledKeyboards = inputMethodManager.enabledInputMethodList
        return enabledKeyboards.any {
            it.serviceInfo.packageName == packageName
        }
    }
    private  fun getActiveModel(): String {
        return keyboardSettingsRepository.getActiveModel()
    }

    private fun getUserPrompts(): List<String> {
        return keyboardSettingsRepository.getUserPrompts()
    }
    override fun onResume() {
        super.onResume()
        viewModel.setLatestState(
                isKeyboardEnabled = isKeyboardEnabled(),
                isUsingKeyboard = isUsingKeyboard(),
                totalMemory = availableMemory().totalMem,
                availableMemory = availableMemory().availMem,
                activeModel = getActiveModel(),
                userPrompts = getUserPrompts(),
                keyboardSettingsRepository=keyboardSettingsRepository,
            )
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Delay the execution to ensure the keyboard status is accurately captured
            // after the input method picker is closed.
            Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.setLatestState(
                        isKeyboardEnabled = isKeyboardEnabled(),
                        isUsingKeyboard = isUsingKeyboard(),
                        totalMemory = availableMemory().totalMem,
                        availableMemory = availableMemory().availMem,
                        activeModel = getActiveModel(),
                        userPrompts = getUserPrompts(),
                        keyboardSettingsRepository = keyboardSettingsRepository,
                    )
            }, 500) // A short delay like 100ms should be sufficient
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.updateKeyboardStatus(
            isEnabled = isKeyboardEnabled(),
            isUsing = isUsingKeyboard()
        )
        StrictMode.setVmPolicy(
            VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build()
        )

        val extFilesDir = getExternalFilesDir(null)

        val localModels = listOf(
            DownloadableLlm(
                "StableLM-2 Zephyr 1.6 (Q5 1.19GB)",
                Uri.parse("https://huggingface.co/stabilityai/stablelm-2-zephyr-1_6b/resolve/main/stablelm-2-zephyr-1_6b-Q5_K_M.gguf?download=true"),
                File(extFilesDir, "stablelm-2-zephyr-1_6b-Q5_K_M.gguf"),
            ),
            DownloadableLlm(
                "Phi-2 7B (Q4_0, 1.6 GiB)",
                Uri.parse("https://huggingface.co/ggml-org/models/resolve/main/phi-2/ggml-model-q4_0.gguf?download=true"),
                File(extFilesDir, "phi-2-q4_0.gguf"),
            ),
//            OfflineLlm(
//                "TinyLlama 1.1B (f16, 2.2 GiB)",
//                Uri.parse("https://huggingface.co/ggml-org/models/resolve/main/tinyllama-1.1b/ggml-model-f16.gguf?download=true"),
//                File(extFilesDir, "tinyllama-1.1-f16.gguf"),
//            ),
        )

        val remoteModels = listOf(
            RemoteLlm(
                "gpt-3.5-turbo",
                "https://api.openai.com/v1/chat/completions",
                "OpenAI",
            ),
            RemoteLlm(
                "gpt-4",
                "https://api.openai.com/v1/chat/completions",
                "OpenAI",
            )
        )

        setContent {
            RootAndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    MainCompose(
                        viewModel,
                        downloadManager,
                        localModels,
                        remoteModels,
                        this@MainActivity,
                        keyboardSettingsRepository=keyboardSettingsRepository,
                    )
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCompose(
    viewModel: MainViewModel,
    dm: DownloadManager,
    localModels: List<DownloadableLlm>,
    remoteModels: List<RemoteLlm>,
    context: Context,
    keyboardSettingsRepository: KeyboardSettingsRepository
) {

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                context = context,
                navController=navController,
                )
        }
        composable("promptManager") {
            PromptManagerScreen(
                viewModel = viewModel,
                navController=navController,
                keyboardSettingsRepository=keyboardSettingsRepository,
            )
        }
        composable("localModelManager") {
            LocalModelManagerScreen(
                context=context,
                viewModel = viewModel,
                dm=dm,
                models=localModels,
                navController=navController,
                keyboardSettingsRepository=keyboardSettingsRepository,
            )
        }
    }
}
