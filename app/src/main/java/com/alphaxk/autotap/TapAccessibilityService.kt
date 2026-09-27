package com.alphaxk.autotap

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TapAccessibilityService : AccessibilityService() {

    private val TAG = "AutoTapService"

    private var job: Job = SupervisorJob()
    private lateinit var scope: CoroutineScope
    private lateinit var prefs: Prefs

    @Volatile
    private var config: TriggerConfig = TriggerConfig()

    @Volatile
    private var lastTapAtMs: Long = 0L

    @Volatile
    private var lastConfirmTapAtMs: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = Prefs(applicationContext)
        job = SupervisorJob()
        scope = CoroutineScope(Dispatchers.Default + job)

        // يبقى يراقب أي تغيير بالإعدادات من واجهة التطبيق مباشرة، بدون إعادة تشغيل الخدمة
        scope.launch {
            prefs.configFlow.collect { updated ->
                config = updated
                Log.d(TAG, "config updated: enabled=${updated.enabled} pattern='${updated.watchPattern}'")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val cfg = config
        if (!cfg.enabled || event == null) return

        // فلترة حسب الحزمة إذا محدد المستخدم تطبيق معيّن
        val pkg = event.packageName?.toString().orEmpty()
        if (cfg.packageFilter.isNotBlank() && pkg != cfg.packageFilter) return

        val root = rootInActiveWindow ?: return

        try {
            val now = SystemClock.elapsedRealtime()

            // المرحلة 1: يضغط زر الحجز بمجرد ظهور النص/الوقت المطلوب مراقبته
            if (cfg.watchPattern.isNotBlank() && now - lastTapAtMs >= cfg.cooldownMs) {
                val watchNode = Matchers.findNodeContainingText(root, cfg.watchPattern)
                if (watchNode != null) {
                    val buttonNode = Matchers.findNodeContainingText(root, cfg.buttonText)
                    val clickable = buttonNode?.let { Matchers.findClickableSelfOrAncestor(it) }
                    if (clickable != null &&
                        clickable.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                    ) {
                        lastTapAtMs = now
                        Log.i(TAG, "tapped '${cfg.buttonText}' after seeing '${cfg.watchPattern}'")
                    }
                }
            }

            // المرحلة 2: زر تأكيد الحجز — يُضغط تلقائياً بمجرد ظهوره بالشاشة
            // (يحدث عادة بعد المرحلة 1 مباشرة، على شكل نافذة/dialog تأكيد)
            if (cfg.confirmButtonText.isNotBlank() && now - lastConfirmTapAtMs >= cfg.cooldownMs) {
                val confirmNode = Matchers.findNodeContainingText(root, cfg.confirmButtonText)
                val confirmClickable = confirmNode?.let { Matchers.findClickableSelfOrAncestor(it) }
                if (confirmClickable != null &&
                    confirmClickable.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                ) {
                    lastConfirmTapAtMs = now
                    Log.i(TAG, "tapped confirm button '${cfg.confirmButtonText}'")
                }
            }
        } finally {
            root.recycle()
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
