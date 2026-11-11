package com.example.sawtflow.data.model

data class Order(
    val id: String,
    val status: String,
    val eta: String,
    val lastScan: String,
    val response: String,
    val responseAr: String? = null,
    val customerRef: String? = null,
    val totalAmount: Double? = null,
    val city: String = "Riyadh"
)

enum class IntentType(val id: String, val labelEn: String, val labelAr: String) {
    STATUS("status", "Order status", "حالة الطلب"),
    CANCEL("cancel", "Cancel order", "إلغاء الطلب"),
    MODIFY("modify", "Modify order", "تعديل الطلب")
}

enum class Dialect(val id: String, val labelEn: String, val labelAr: String, val sampleEn: String, val sampleAr: String) {
    GULF(
        id = "gulf",
        labelEn = "Gulf / Saudi",
        labelAr = "خليجي / سعودي",
        sampleEn = "I want to know where my order SF-1048 is; they said it would arrive today.",
        sampleAr = "أبغى أعرف وين وصل طلبي SF-1048، قالوا بيوصل اليوم."
    ),
    EGYPTIAN(
        id = "egyptian",
        labelEn = "Egyptian",
        labelAr = "مصري",
        sampleEn = "Can I check where order SF-2086 has reached? They told me it would arrive today.",
        sampleAr = "ممكن أعرف الطلب SF-2086 وصل لفين؟ قالولي هييجي النهارده."
    ),
    LEVANTINE(
        id = "levantine",
        labelEn = "Levantine",
        labelAr = "شامي",
        sampleEn = "I want to know where my order SF-3312 is; they told me it would arrive today.",
        sampleAr = "بدي أعرف وين صار طلبي SF-3312، حكولي بيوصل اليوم."
    ),
    MAGHREBI(
        id = "maghrebi",
        labelEn = "Maghrebi",
        labelAr = "مغاربي",
        sampleEn = "I would like to know where my order SF-4420 is; they said it would arrive today.",
        sampleAr = "بغيت نعرف فين وصل الطلب ديالي SF-4420، قالو غادي يوصل اليوم."
    )
}

enum class AppLanguage {
    EN, AR
}

data class AuditLogEntry(
    val id: String,
    val eventType: String,
    val orderId: String?,
    val actor: String,
    val details: String,
    val timestampFormatted: String
)

data class DemoInquiry(
    val name: String,
    val email: String,
    val notes: String,
    val timestamp: Long = System.currentTimeMillis()
)
