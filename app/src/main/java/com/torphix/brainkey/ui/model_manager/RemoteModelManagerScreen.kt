package com.torphix.brainkey.ui.model_manager

import android.app.DownloadManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.torphix.brainkey.DownloadableLlm
import com.torphix.brainkey.repository.KeyboardSettingsRepository
import com.torphix.brainkey.services.llm.RemoteLlm
import com.torphix.brainkey.ui.home.CustomText
import com.torphix.brainkey.ui.home.ModelStatus
import com.torphix.brainkey.ui.main.MainViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteModelManager(
    viewModel: MainViewModel,
    navController: NavController,
    models: List<RemoteLlm>,
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint= Color.Black)
                    }
                }
            )
        },
        content = { innerPadding ->
            ScaffoldContent(
                viewModel,
                paddingValues = innerPadding,
                models=models,
                keyboardSettingsRepository=keyboardSettingsRepository,
            )
        })
}


@Composable
private  fun ScaffoldContent(
    viewModel: MainViewModel,
    paddingValues: PaddingValues,
    models: List<RemoteLlm>,
    keyboardSettingsRepository: KeyboardSettingsRepository,
) {


    Column(modifier = Modifier
        .padding(paddingValues)
        .padding(16.dp)) {
        val scrollState = rememberLazyListState()

        val textInputValue = remember { mutableStateOf("") }

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(state = scrollState) {
                item {
                    // Text and info
                    CustomText("Select a model from below. (Warning: Your text is sent to the API provider, use a local LLM for offline and data privacy)")
                    Spacer(modifier= Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier= Modifier.height(8.dp))
                    // Model Status
                    ModelStatus(viewModel = viewModel)
                    Spacer(modifier= Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier= Modifier.height(16.dp))
                    RemoteModelSelector(
                        viewModel,
                        models,
                        keyboardSettingsRepository
                    )
                    Spacer(modifier= Modifier.height(16.dp))
                    TextField(
                        placeholder = { Text("Write me an email") },
                        value = textInputValue.value,
                        onValueChange = { textInputValue.value = it },
                        label = { Text("Test it out here") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier= Modifier.height(16.dp))

                }
            }
        }
    }
}

@Composable
fun RemoteModelSelector(
    viewModel: MainViewModel,
    remoteModels: List<RemoteLlm>,
    keyboardSettingsRepository: KeyboardSettingsRepository
) {

    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(remoteModels.firstOrNull() ?: "") }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Select a model",
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Dropdown Icon"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            remoteModels.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model.name) },
                    onClick = {
                        expanded = false
                        viewModel.updateActiveModel(model.name, keyboardSettingsRepository)
                    }
                )
            }
        }
    }
}