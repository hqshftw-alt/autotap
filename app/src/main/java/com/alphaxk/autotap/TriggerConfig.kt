package com.alphaxk.autotap

/**
 * إعدادات الأتمتة:
 * - watchPattern: النص/الرقم اللي نراقب ظهوره بالشاشة (مثال: "07:01")
 * - buttonText: نص الزر اللي نضغطه تلقائياً لما يظهر watchPattern (مثال: "حجز فترة الدوام")
 * - confirmButtonText: نص زر تأكيد ثاني يظهر بعد الضغط الأول (مثال: "تأكيد")، يُضغط تلقائياً
 *   بمجرد ما يظهر بالشاشة، بدون شرط ارتباطه بـ watchPattern
 * - packageFilter: اختياري — لو معبّى، الخدمة تشتغل بس داخل هذا التطبيق (مثال: حزمة تطبيق Al Manhal)
 * - cooldownMs: أقل فترة بين ضغطتين متتاليتين، عشان ما يضغط أكثر من مرة على نفس الحدث
 */
data class TriggerConfig(
    val enabled: Boolean = false,
    val watchPattern: String = "",
    val buttonText: String = "حجز فترة الدوام",
    val confirmButtonText: String = "",
    val packageFilter: String = "",
    val cooldownMs: Long = 2000L
)
