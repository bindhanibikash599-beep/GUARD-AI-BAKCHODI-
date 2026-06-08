package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.GuardAiAppContent
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppViewModel

class AdminActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: AppViewModel = viewModel()
        LaunchedEffect(Unit) {
          viewModel.markOpenedFromAdminLauncher()
        }
        GuardAiAppContent(viewModel = viewModel)
      }
    }
  }
}
