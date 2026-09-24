package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.delulu.ui.navigation.DeluluApp
import com.example.delulu.viewmodel.DeluluViewModel
import com.example.ui.theme.DeluluTheme

class MainActivity : ComponentActivity() {
  private val viewModel: DeluluViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      DeluluTheme {
        DeluluApp(viewModel = viewModel)
      }
    }
  }
}

