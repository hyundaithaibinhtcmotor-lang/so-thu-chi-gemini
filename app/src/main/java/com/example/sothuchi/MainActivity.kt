package com.example.sothuchi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.sothuchi.data.local.MoneyDatabase
import com.example.sothuchi.data.repository.MoneyRepository
import com.example.sothuchi.ui.MoneyTrackerApp
import com.example.sothuchi.ui.theme.MisaMoneyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = MoneyDatabase.getInstance(this)
        val repository = MoneyRepository(db.moneyDao())

        lifecycleScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        setContent {
            MisaMoneyTheme {
                MoneyTrackerApp(repository = repository)
            }
        }
    }
}
