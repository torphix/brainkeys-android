package com.torphix.brainkey.ui.prompts

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.torphix.brainkey.repository.KeyboardSettingsRepository
import com.torphix.brainkey.ui.main.MainViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptManagerScreen(
    viewModel: MainViewModel,
    navController: NavController,
    keyboardSettingsRepository: KeyboardSettingsRepository,
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Prompt Manager", color= Color.Black) },
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
                viewModel,
                paddingValues = innerPadding,
                keyboardSettingsRepository=keyboardSettingsRepository,
            )
        })
}

@Composable
private fun ScaffoldContent(
    viewModel: MainViewModel,
    paddingValues: PaddingValues,
    keyboardSettingsRepository: KeyboardSettingsRepository,
) {
    // Observe LiveData and convert it to a state
    val userPrompts by viewModel.userPrompts.observeAsState(initial = emptyList())



    Column(modifier = Modifier
        .padding(paddingValues)
        .padding(16.dp)) {

        AddPromptTextBox(
            viewModel = viewModel,
            userPrompts = userPrompts,
            keyboardSettingsRepository = keyboardSettingsRepository)
        Spacer(modifier = Modifier.height(16.dp))
        val scrollState = rememberLazyListState()
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(state = scrollState) {
                // Now userPrompts is a List<String>, not LiveData
                itemsIndexed(userPrompts) { index, userPrompt ->
                    // Render each userPrompt as a Text composable
                    UserPromptCard(
                        userPrompt = userPrompt,
                        onEdit = { editedPrompt ->
                            var newPrompts = userPrompts.toMutableList()
                            newPrompts[index] = editedPrompt
                            viewModel.updateUserPrompts(newPrompts.toList(), keyboardSettingsRepository)
                        },
                        onDelete = {
                            var newPrompts = userPrompts.toMutableList()
                            newPrompts.removeAt(index)
                            viewModel.updateUserPrompts(newPrompts.toList(), keyboardSettingsRepository)
                        }
                    )
                }
            }
        }
    }
}




@Composable
fun AddPromptTextBox(
    viewModel: MainViewModel,
    userPrompts: List<String>,
    keyboardSettingsRepository: KeyboardSettingsRepository
){
    var textInput by remember { mutableStateOf("") }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = textInput,
            onValueChange = { textInput = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Enter text") },
            singleLine = true
        )
        IconButton(onClick = {
            var newPrompts = userPrompts.toMutableList()
            newPrompts.add(0, textInput)
            viewModel.updateUserPrompts(newPrompts.toList(), keyboardSettingsRepository)
            textInput = ""
        }) {
            Icon(Icons.Default.AddCircle, contentDescription = "Add")
        }
    }

}
@Composable
fun UserPromptCard(
    userPrompt: String,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editableText by remember { mutableStateOf(userPrompt) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isEditing) {
                TextField(
                    value = editableText,
                    onValueChange = { editableText = it },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                )
            } else {
                Text(text = userPrompt)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp)
        ) {
            if (isEditing) {
                Button(onClick = {
                    isEditing = false
                    onEdit(editableText)
                }) {
                    Text("Save")
                }
            } else {
                IconButton(onClick = { isEditing = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}