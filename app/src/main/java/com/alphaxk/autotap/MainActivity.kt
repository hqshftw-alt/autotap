package com.alphaxk.autotap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = Prefs(applicationContext)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Ui(prefs, applicationContext)
                }
            }
        }
    }
}

@Composable
fun Ui(prefs: Prefs, ctx: Context) {
    val scope = rememberCoroutineScope()

    var enabled by remember { mutableStateOf(false) }
    var watchPattern by remember { mutableStateOf("") }
    var buttonText by remember { mutableStateOf("حجز فترة الدوام") }
    var confirmButtonText by remember { mutableStateOf("") }
    var packageFilter by remember { mutableStateOf("") }
    var cooldownMs by remember { mutableStateOf("2000") }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val current = prefs.configFlow.first()
        enabled = current.enabled
        watchPattern = current.watchPattern
        buttonText = current.buttonText
        confirmButtonText = current.confirmButtonText
        packageFilter = current.packageFilter
        cooldownMs = current.cooldownMs.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("AutoTap", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            "يراقب الشاشة، ولما يلقى النص المحدد، يضغط زر الحجز تلقائياً.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("تفعيل الأتمتة")
            Switch(checked = enabled, onCheckedChange = { enabled = it })
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = watchPattern,
            onValueChange = { watchPattern = it },
            label = { Text("النص/الوقت اللي نراقبه (مثال: 07:01)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = buttonText,
            onValueChange = { buttonText = it },
            label = { Text("نص الزر اللي يُضغط تلقائياً") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmButtonText,
            onValueChange = { confirmButtonText = it },
            label = { Text("نص زر تأكيد الحجز (اختياري، مثال: تأكيد)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = packageFilter,
            onValueChange = { packageFilter = it },
            label = { Text("حزمة التطبيق (اختياري، مثال: com.almanhal.app)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = cooldownMs,
            onValueChange = { cooldownMs = it.filter(Char::isDigit) },
            label = { Text("مدة الانتظار بين الضغطات (ميلي ثانية)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    prefs.save(
                        TriggerConfig(
                            enabled = enabled,
                            watchPattern = watchPattern.trim(),
                            buttonText = buttonText.trim(),
                            confirmButtonText = confirmButtonText.trim(),
                            packageFilter = packageFilter.trim(),
                            cooldownMs = cooldownMs.toLongOrNull() ?: 2000L
                        )
                    )
                    saved = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حفظ الإعدادات")
        }

        if (saved) {
            Spacer(Modifier.height(8.dp))
            Text("تم الحفظ ✓", style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("فتح إعدادات إمكانية الوصول لتفعيل الخدمة")
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "بعد فتح الإعدادات: دوّر على AutoTap في القائمة وفعّله يدوياً (خطوة تسويها مرة وحدة فقط).",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
