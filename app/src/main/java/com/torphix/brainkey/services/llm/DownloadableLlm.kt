package com.torphix.brainkey

import android.app.DownloadManager
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.database.getLongOrNull
import androidx.core.net.toUri
import com.torphix.brainkey.repository.KeyboardSettingsRepository
import com.torphix.brainkey.ui.main.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

data class DownloadableLlm(val name: String, val source: Uri, val destination: File,) {
    companion object {
        @JvmStatic
        private val tag: String? = this::class.qualifiedName

        sealed interface State
        data object Ready: State
        data class Downloading(val id: Long): State
        data class Downloaded(val downloadable: DownloadableLlm): State
        data class Error(val message: String): State


        @JvmStatic
        @Composable
        fun DownloadSection(
            viewModel: MainViewModel,
            dm: DownloadManager,
            downloadables: List<DownloadableLlm>,
            keyboardSettingsRepository: KeyboardSettingsRepository) {
            var selectedItem by remember { mutableStateOf(downloadables.firstOrNull()) }

            Column(
                modifier = Modifier.padding(vertical = 8.dp) // Add padding for spacing
            ) {
                ModelDropdownMenu(
                    selectedItem = selectedItem,
                    downloadables = downloadables,
                    onSelectionChange = { selectedItem = it }
                )
                Spacer(modifier = Modifier.height(16.dp)) // Add space between dropdown and button
                selectedItem?.let {
                    DownloadButton(viewModel, dm, it, keyboardSettingsRepository)
                }
            }
        }
        @Composable
        private fun ModelDropdownMenu(
            selectedItem: DownloadableLlm?,
            downloadables: List<DownloadableLlm>,
            onSelectionChange: (DownloadableLlm) -> Unit
        ) {
            var expanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedItem?.name ?: "Select a model",
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
                    downloadables.forEach { downloadable ->
                        DropdownMenuItem(
                            text= { Text(downloadable.name) },
                            onClick = {
                                onSelectionChange(downloadable)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        @JvmStatic
        @Composable
        fun DownloadButton(viewModel: MainViewModel, dm: DownloadManager, item: DownloadableLlm, keyboardSettingsRepository: KeyboardSettingsRepository) {
            var status: State by remember {
                mutableStateOf(
                    if (item.destination.exists()) Downloaded(item)
                    else Ready
                )
            }
            var progress by remember { mutableDoubleStateOf(0.0) }

            val coroutineScope = rememberCoroutineScope()

            suspend fun waitForDownload(result: Downloading, item: DownloadableLlm): State {
                while (true) {
                    val cursor = dm.query(DownloadManager.Query().setFilterById(result.id))

                    if (cursor == null) {
                        Log.e(tag, "dm.query() returned null")
                        return Error("dm.query() returned null")
                    }

                    if (!cursor.moveToFirst() || cursor.count < 1) {
                        cursor.close()
                        Log.i(tag, "cursor.moveToFirst() returned false or cursor.count < 1, download canceled?")
                        return Ready
                    }

                    val pix = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val tix = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    val sofar = cursor.getLongOrNull(pix) ?: 0
                    val total = cursor.getLongOrNull(tix) ?: 1
                    cursor.close()

                    if (sofar == total) {
                        return Downloaded(item)
                    }

                    progress = (sofar * 1.0) / total

                    delay(1000L)
                }
            }

            fun onClick() {
                when (val s = status) {
                    is Downloaded -> {
                        viewModel.updateActiveModel(item.destination.name, keyboardSettingsRepository)
                    }

                    is Downloading -> {
                        coroutineScope.launch {
                            status = waitForDownload(s, item)
                        }
                    }

                    else -> {
                        item.destination.delete()

                        val request = DownloadManager.Request(item.source).apply {
                            setTitle("Downloading model")
                            setDescription("Downloading model: ${item.name}")
                            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                            setDestinationUri(item.destination.toUri())
                        }

                        Log.i(tag, "Saving ${item.name} to ${item.destination.path}")

                        val id = dm.enqueue(request)
                        status = Downloading(id)
                        onClick()
                    }
                }
            }

            Column {
                Button(
                    onClick = { onClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(50.dp)
                        .padding(3.dp),
                    shape = RoundedCornerShape(10.dp),
                    enabled = status !is Downloading,
                ) {
                    when (status) {
                        is Downloading -> Text(text = "Downloading ${(progress * 100).toInt()}%")
                        is Downloaded -> Text("Select ${item.name}")
                        is Ready -> Text("Download ${item.name}")
                        is Error -> Text("Download ${item.name}")
                        else -> {}
                    }
                }
                Spacer(Modifier.height(24.dp))
                if (status is Downloaded) {
                    // Delete button
                    Button(onClick = {
                        // New code for deletion
                            if (item.destination.exists()) {
                                item.destination.delete()
                                viewModel.updateActiveModel("", keyboardSettingsRepository) // Update with an empty or default model
                                status = Ready
                        }},
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(50.dp)
                            .padding(3.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(contentColor=Color.White, containerColor = Color.Red) // Set the background color to red
                    ) {
                        Text("Delete Model")
                    }


                }
            }
        }

    }


}


