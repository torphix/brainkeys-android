package com.torphix.brainkey.ui.home

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
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
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.torphix.brainkey.ui.main.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    context: Context,
    navController: NavController
) {

    Scaffold(
    topBar = {
        TopAppBar(
            title = { Text(text = "BrainKey", color= Color.Black) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor= MaterialTheme.colorScheme.primary,
            )
        )
    },
    content = { innerPadding ->
        ScaffoldContent(
            viewModel,
            paddingValues = innerPadding,
            context = context,
            navController = navController,
        )
    })
}



@Composable
fun ScaffoldContent(
    viewModel: MainViewModel,
    paddingValues: PaddingValues,
    context: Context,
    navController: NavController
){
    Column(modifier = Modifier
        .padding(paddingValues)
        .padding(16.dp)) {
        val scrollState = rememberLazyListState()


        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(state = scrollState) {
                item {
                    // Text and info
                    CustomText("1. Activate the keyboard in settings (first button)")
                    CustomText("2. Select the keyboard")
                    CustomText("3. Select model in local model manager")
                    CustomText("4. Open up a keyboard and use the new buttons at the top")
                    SettingsStatus(viewModel = viewModel, context=context)
                    KeyboardSettingButtons(
                        viewModel,
                        context,
                    )
                    Spacer(modifier= Modifier.height(8.dp))
                    ModelSettingButtons(navController = navController)
                    Spacer(modifier= Modifier.height(8.dp))
                    Button(onClick = { navController.navigate("promptManager") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(50.dp)
                            .padding(3.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Prompt Manager")
                    }
                }
            }
        }
    }
}

@Composable
fun ModelSettingButtons( navController: NavController) {
    Button(onClick = { navController.navigate("localModelManager") },
        modifier = Modifier
            .fillMaxWidth()
            .size(50.dp)
            .padding(3.dp),
        shape = RoundedCornerShape(10.dp)
    )
    {
        Text("Local Model Manager")
    }
}

@Composable
fun SettingsStatus(viewModel: MainViewModel, context:Context) {
    // Keyboard status
    val isKeyboardEnabled by viewModel.isKeyboardEnabled.observeAsState(false)
    val isUsingKeyboard by viewModel.isUsingKeyboard.observeAsState(false)

    val statusText = when {
        !isKeyboardEnabled -> "Keyboard Not Active"
        isUsingKeyboard -> "Keyboard Active!"
        else -> "Keyboard Activated But Not Selected"
    }
    val statusColor = when {
        !isKeyboardEnabled -> Color.Red // Example color for not active
        isUsingKeyboard -> Color.Green // Example color for active
        else -> Color.Yellow // Example color for enabled but not active
    }
    Spacer(modifier= Modifier.height(16.dp))
    Divider()
    Spacer(modifier= Modifier.height(8.dp))
    // Keyboard status
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp)
    ) {
        Text(
            text = statusText,
            color = statusColor,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(3.dp)
        )
    }
    Spacer(modifier= Modifier.height(8.dp))
    Divider()
    Spacer(modifier= Modifier.height(16.dp))
    // Model Status
    ModelStatus(viewModel)
    Spacer(modifier= Modifier.height(8.dp))
    Divider()
    Spacer(modifier= Modifier.height(16.dp))
}


@Composable
fun ModelStatus(
    viewModel: MainViewModel
){
    val activeModel = viewModel.activeModel.observeAsState("")

    // Model Status
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp)
    ) {
        Text(
            text = if (activeModel.value == "") "No Model Selected" else "Using Model: ${activeModel.value}",
            color = if (activeModel.value == "") Color.Yellow else Color.Green,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(3.dp)
        )
    }

}
@Composable
fun KeyboardSettingButtons(viewModel: MainViewModel,
                           context: Context,
) {
    // Enable keyboard button
    Button(onClick = {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        context.startActivity(intent)
    },
        modifier = Modifier
            .fillMaxWidth()
            .size(50.dp)
            .padding(3.dp),
        shape = RoundedCornerShape(10.dp)
    )
    {
        Text("Activate keyboard (Open Settings)")
    }
    Spacer(modifier= Modifier.height(8.dp))

    // Activate keyboard button
    Button(onClick = {
        val imeManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imeManager.showInputMethodPicker()
    },
        modifier = Modifier
            .fillMaxWidth()
            .size(50.dp)
            .padding(3.dp),
        shape = RoundedCornerShape(10.dp) )
    {
        Text("Select Keyboard")
    }
}

@Composable
fun CustomText(
    text: String,
    isHeader: Boolean = false
) {
    val textStyle = if (isHeader) {
        TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    } else {
        TextStyle(
            fontSize = 16.sp
        )
    }
    Text(
        text,
        style = textStyle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}