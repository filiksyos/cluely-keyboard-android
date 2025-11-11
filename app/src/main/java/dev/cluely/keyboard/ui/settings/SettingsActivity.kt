package dev.cluely.keyboard.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import dev.cluely.keyboard.data.storage.ApiKeyStore
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : ComponentActivity() {

    @Inject
    lateinit var apiKeyStore: ApiKeyStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val apiKey = remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    apiKeyStore.apiKeyFlow.collect { key ->
                        apiKey.value = key
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Cluely Keyboard Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "OpenRouter API Key",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TextField(
                    value = apiKey.value,
                    onValueChange = { newValue ->
                        apiKey.value = newValue
                        lifecycleScope.launch {
                            apiKeyStore.saveApiKey(newValue)
                        }
                    },
                    label = { Text("Enter your OpenRouter API key") },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}