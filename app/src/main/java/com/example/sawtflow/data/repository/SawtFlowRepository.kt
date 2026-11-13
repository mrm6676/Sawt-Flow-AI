package com.example.sawtflow.data.repository

import com.example.sawtflow.data.model.AuditLogEntry
import com.example.sawtflow.data.model.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SawtFlowRepository {

    private val initialOrders = listOf(
        Order(
            id = "SF-1048",
            status = "In transit",
            eta = "Today · 6:00 PM",
            lastScan = "Riyadh hub · 09:42",
            response = "Your order is in transit and is expected to arrive today by 6:00 PM.",
            responseAr = "طلبك قيد الشحن، ومن المتوقع أن يصل اليوم قبل الساعة 6:00 مساءً.",
            city = "Riyadh",
            totalAmount = 249.50
        ),
        Order(
            id = "SF-2086",
            status = "Delivered",
            eta = "Delivered · Yesterday",
            lastScan = "Jeddah customer address · 16:18",
            response = "Your order was delivered yesterday at 4:18 PM.",
            responseAr = "تم تسليم طلبك بالأمس عند الساعة 4:18 مساءً.",
            city = "Jeddah",
            totalAmount = 185.00
        ),
        Order(
            id = "SF-3312",
            status = "Preparing",
            eta = "Tomorrow · 2:00 PM",
            lastScan = "Dammam fulfillment center · 08:05",
            response = "Your order is being prepared and is expected to arrive tomorrow by 2:00 PM.",
            responseAr = "يتم تجهيز طلبك حالياً ومن المتوقع وصوله غداً الساعة 2:00 مساءً.",
            city = "Dammam",
            totalAmount = 390.00
        ),
        Order(
            id = "SF-4420",
            status = "Delayed",
            eta = "Revised · 24 Oct · 8:00 PM",
            lastScan = "Dubai hub · Weather delay · 07:35",
            response = "Your order has been delayed by a weather disruption. The revised delivery estimate is 24 October by 8:00 PM.",
            responseAr = "تأخر طلبك بسبب تقلبات جوية. موعد التسليم الجديد المتوقع هو 24 أكتوبر الساعة 8:00 مساءً.",
            city = "Dubai",
            totalAmount = 520.00
        ),
        Order(
            id = "SF-5177",
            status = "Canceled",
            eta = "Not applicable",
            lastScan = "Cancellation confirmed · 11:20",
            response = "This order was canceled successfully. No further delivery action is required.",
            responseAr = "تم إلغاء هذا الطلب بنجاح. لا يلزم أي إجراء توصيل إضافي.",
            city = "Riyadh",
            totalAmount = 110.00
        ),
        Order(
            id = "SF-6089",
            status = "Out for delivery",
            eta = "Today · before 9:00 PM",
            lastScan = "Riyadh delivery vehicle · 13:12",
            response = "Your order is out for delivery and should arrive today before 9:00 PM.",
            responseAr = "طلبك خرج للتوصيل ويُتوقع وصوله اليوم قبل الساعة 9:00 مساءً.",
            city = "Riyadh",
            totalAmount = 430.75
        ),
        Order(
            id = "SF-7194",
            status = "Ready for pickup",
            eta = "Available today · 10:00 AM",
            lastScan = "Khobar pickup point · 09:31",
            response = "Your order is ready for pickup at the Khobar location. Please bring a valid ID.",
            responseAr = "طلبك جاهز للاستلام في فرع الخبر. يرجى إحضار إثبات هوية سارٍ.",
            city = "Khobar",
            totalAmount = 95.00
        ),
        Order(
            id = "SF-8237",
            status = "Address issue",
            eta = "On hold",
            lastScan = "Makkah delivery station · Address could not be verified · 14:06",
            response = "Delivery is on hold because the address could not be verified. Please confirm your delivery details.",
            responseAr = "التوصيل معلق مؤقتاً لتعذر التحقق من العنوان. يرجى تأكيد بيانات العنوان.",
            city = "Makkah",
            totalAmount = 210.00
        ),
        Order(
            id = "SF-9361",
            status = "Returned",
            eta = "Return completed · 22 Oct",
            lastScan = "Riyadh returns center · 17:42",
            response = "The order was returned to the fulfillment center and the return process was completed on 22 October.",
            responseAr = "تم إرجاع الطلب إلى مركز الاسترجاع واكتملت عملية الإرجاع في 22 أكتوبر.",
            city = "Riyadh",
            totalAmount = 315.00
        ),
        Order(
            id = "SF-1472",
            status = "Customs clearance",
            eta = "Estimated · 28 Oct · 5:00 PM",
            lastScan = "Abu Dhabi customs facility · Documents under review · 10:15",
            response = "Your international order is undergoing customs clearance. Delivery is estimated for 28 October by 5:00 PM.",
            responseAr = "طلبك الدولي قيد التخليص الجمركي حالياً. التسليم المقدر في 28 أكتوبر 5:00 مساءً.",
            city = "Abu Dhabi",
            totalAmount = 880.00
        ),
        Order(
            id = "SF-2583",
            status = "Refund processing",
            eta = "Refund · 3–5 business days",
            lastScan = "Payments team · Refund initiated · 12:24",
            response = "Your order refund has been initiated and should appear in your account within 3–5 business days.",
            responseAr = "تم بدء معالجة استرجاع المبلغ وستصل إلى حسابك خلال 3 إلى 5 أيام عمل.",
            city = "Riyadh",
            totalAmount = 145.00
        )
    )

    private val _orders = MutableStateFlow(initialOrders)
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(
        listOf(
            AuditLogEntry(
                id = UUID.randomUUID().toString().take(8),
                eventType = "system.startup",
                orderId = null,
                actor = "system",
                details = "SawtFlow Arabic Voice Intelligence initialized with 11 operational records",
                timestampFormatted = formatCurrentTime()
            )
        )
    )
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    fun findOrder(orderId: String): Order? {
        val normalized = orderId.trim().uppercase()
        return _orders.value.find { it.id.equals(normalized, ignoreCase = true) }
    }

    fun updateOrdersStatus(orderIds: Set<String>, newStatus: String) {
        if (orderIds.isEmpty() || newStatus.isBlank()) return
        val normalizedIds = orderIds.map { it.trim().uppercase() }.toSet()
        _orders.value = _orders.value.map { order ->
            if (order.id.uppercase() in normalizedIds) {
                order.copy(status = newStatus)
            } else {
                order
            }
        }
        logEvent(
            eventType = "orders.batch_status_updated",
            orderId = normalizedIds.joinToString(", "),
            actor = "human.operator",
            details = "Batch updated ${normalizedIds.size} order(s) to status: $newStatus"
        )
    }

    fun logEvent(eventType: String, orderId: String?, actor: String, details: String) {
        val entry = AuditLogEntry(
            id = UUID.randomUUID().toString().take(8),
            eventType = eventType,
            orderId = orderId,
            actor = actor,
            details = details,
            timestampFormatted = formatCurrentTime()
        )
        _auditLogs.value = listOf(entry) + _auditLogs.value
    }

    private fun formatCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
