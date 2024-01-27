package com.torphix.brainkey.ui.model_manager

import android.app.DownloadManager
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.torphix.brainkey.DownloadableLlm
import com.torphix.brainkey.Llm
import com.torphix.brainkey.repository.KeyboardSettingsRepository
import com.torphix.brainkey.ui.home.CustomText
import com.torphix.brainkey.ui.home.ModelStatus
import com.torphix.brainkey.ui.main.MainViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalModelManagerScreen(
    context:Context,
    viewModel: MainViewModel,
    dm: DownloadManager,
    navController: NavController,
    models: List<DownloadableLlm>,
    keyboardSettingsRepository: KeyboardSettingsRepository,
) {

        Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Model Manager", color= Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor= MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint=Color.Black)
                    }
                }
            )
        },
        content = { innerPadding ->
            ScaffoldContent(
                context,
                viewModel,
                dm,
                paddingValues = innerPadding,
                models=models,
                keyboardSettingsRepository=keyboardSettingsRepository,
            )
        })
}


@Composable
private  fun ScaffoldContent(
    context: Context,
    viewModel: MainViewModel,
    dm: DownloadManager,
    paddingValues: PaddingValues,
    models: List<DownloadableLlm>,
    keyboardSettingsRepository: KeyboardSettingsRepository,
) {


    Column(modifier = Modifier
        .padding(paddingValues)
        .padding(16.dp)) {
        val scrollState = rememberLazyListState()
        val benchmarkResult by viewModel.benchmarkResults.observeAsState("")
        val textInputValue = remember { mutableStateOf("") }

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(state = scrollState) {
                item {
                    // Text and info
                    CustomText("Select a model from below. StableLM Zephyr is recommended for speed & quality")
                    Spacer(modifier= Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier= Modifier.height(8.dp))
                    // Model Status
                    ModelStatus(viewModel = viewModel)
                    Spacer(modifier= Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier= Modifier.height(16.dp))
                    DownloadableLlm.DownloadSection(viewModel, dm, models, keyboardSettingsRepository)
                    Spacer(modifier= Modifier.height(16.dp))
                    TextField(
                        placeholder = {Text("Write me an email")},
                        value = textInputValue.value,
                        onValueChange = { textInputValue.value = it },
                        label = { Text("Test it out here") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier= Modifier.height(16.dp))
                    // Benchmark model
                    Button(onClick = {
                        val extFilesDir = context.getExternalFilesDir(null)
                        viewModel.load("${extFilesDir}/${viewModel.activeModel.value}")
                        viewModel.bench(8, 4, 1)
                        viewModel.clear()
                    },
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(50.dp)
                            .padding(3.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Benchmark")
                    }
                    if (benchmarkResult.isNotEmpty()){
                        Text(benchmarkResult)
                    }
                }
            }
        }
    }

}
