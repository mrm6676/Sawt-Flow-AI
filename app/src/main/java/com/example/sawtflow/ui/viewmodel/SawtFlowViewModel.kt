package com.example.sawtflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawtflow.data.model.AppLanguage
import com.example.sawtflow.data.model.AuditLogEntry
import com.example.sawtflow.data.model.DemoInquiry
import com.example.sawtflow.data.model.Dialect
import com.example.sawtflow.data.model.IntentType
import com.example.sawtflow.data.model.Order
import com.example.sawtflow.data.repository.SawtFlowRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.regex.Pattern

enum class AppTab {
    STUDIO, ORDERS, AUDIT, OVERVIEW
}

data class StudioUiState(
    val currentTab: AppTab = AppTab.STUDIO,
    val language: AppLanguage = AppLanguage.EN,
    val selectedDialect: Dialect = Dialect.GULF,
    val transcriptText: String = Dialect.GULF.sampleEn,
    val isEditingTranscript: Boolean = false,
    val confidence: String = "96.8%",
    val isRecording: Boolean = false,
    val recordSeconds: Int = 0,
    val audioStatusMessage: String = "Dialect sample loaded",
    val hasAudioSource: Boolean = true,
    
    // Intent detection
    val detectedIntent: IntentType = IntentType.STATUS,
    val extractedOrderId: String? = "SF-1048",
    val intentConfidence: String = "98.4%",
    val isAnalyzed: Boolean = true,
    val urgency: String = "Normal",
    
    // Action / Evidence
    val orderInputId: String = "SF-1048",
    val isSearchingOrder: Boolean = false,
    val activeOrder: Order? = null,
    val lookupMessage: String = "Order ready for lookup",
    
    // Human Approval
    val draftResponse: String = "Your order is in transit and is expected to arrive today by 6:00 PM.",
    val isApproved: Boolean = false,
    val isRetrieved: Boolean = false,
    
    // Demo Inquiry
    val demoName: String = "",
    val demoEmail: String = "",
    val demoNotes: String = "",
    val isDemoSubmitted: Boolean = false
)

class SawtFlowViewModel(
    private val repository: SawtFlowRepository = SawtFlowRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val orders: StateFlow<List<Order>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val auditLogs: StateFlow<List<AuditLogEntry>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var recordingJob: Job? = null

    init {
        // Initial lookup
        retrieveOrder("SF-1048")
    }

    fun selectTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun toggleLanguage() {
        val nextLang = if (_uiState.value.language == AppLanguage.EN) AppLanguage.AR else AppLanguage.EN
        _uiState.value = _uiState.value.copy(
            language = nextLang,
            transcriptText = if (nextLang == AppLanguage.AR) _uiState.value.selectedDialect.sampleAr else _uiState.value.selectedDialect.sampleEn
        )
        updateDraftResponse()
    }

    fun selectDialect(dialect: Dialect) {
        val text = if (_uiState.value.language == AppLanguage.AR) dialect.sampleAr else dialect.sampleEn
        _uiState.value = _uiState.value.copy(
            selectedDialect = dialect,
            transcriptText = text,
            audioStatusMessage = if (_uiState.value.language == AppLanguage.AR) "تم تحميل عينة اللهجة" else "Colloquial sample loaded",
            isApproved = false,
            isRetrieved = false
        )
        analyzeCurrentTranscript(text)
    }

    fun toggleRecording() {
        if (_uiState.value.isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        _uiState.value = _uiState.value.copy(
            isRecording = true,
            recordSeconds = 0,
            audioStatusMessage = if (_uiState.value.language == AppLanguage.AR) "جارٍ التسجيل · 00:00" else "Recording · 00:00"
        )
        recordingJob?.cancel()
        recordingJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val newSec = _uiState.value.recordSeconds + 1
                val timeStr = String.format("%02d:%02d", newSec / 60, newSec % 60)
                _uiState.value = _uiState.value.copy(
                    recordSeconds = newSec,
                    audioStatusMessage = if (_uiState.value.language == AppLanguage.AR) "جارٍ التسجيل · $timeStr" else "Recording · $timeStr"
                )
            }
        }
    }

    private fun stopRecording() {
        recordingJob?.cancel()
        val timeStr = String.format("%02d:%02d", _uiState.value.recordSeconds / 60, _uiState.value.recordSeconds % 60)
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            hasAudioSource = true,
            audioStatusMessage = if (_uiState.value.language == AppLanguage.AR) "تم التسجيل · $timeStr" else "Recorded voice ready · $timeStr",
            confidence = "97.4%"
        )
        repository.logEvent(
            eventType = "audio.recorded",
            orderId = _uiState.value.extractedOrderId,
            actor = "user",
            details = "Captured voice input duration: $timeStr"
        )
    }

    fun loadSample() {
        val text = if (_uiState.value.language == AppLanguage.AR) {
            "أين طلبي SF-1048؟ قيل لي إنه سيصل اليوم."
        } else {
            "Where is my order SF-1048? I was told it would arrive today."
        }
        _uiState.value = _uiState.value.copy(
            transcriptText = text,
            audioStatusMessage = if (_uiState.value.language == AppLanguage.AR) "تم تحميل الرسالة التجريبية" else "Sample message loaded",
            isApproved = false,
            isRetrieved = false
        )
        analyzeCurrentTranscript(text)
    }

    fun updateTranscript(text: String) {
        _uiState.value = _uiState.value.copy(transcriptText = text)
    }

    fun toggleEditTranscript() {
        val newEditing = !_uiState.value.isEditingTranscript
        _uiState.value = _uiState.value.copy(isEditingTranscript = newEditing)
        if (!newEditing) {
            // Saved correction
            analyzeCurrentTranscript(_uiState.value.transcriptText)
            repository.logEvent(
                eventType = "transcript.corrected",
                orderId = _uiState.value.extractedOrderId,
                actor = "operator",
                details = "User corrected transcript wording: '${_uiState.value.transcriptText.take(40)}...'"
            )
        }
    }

    fun analyzeIntent() {
        analyzeCurrentTranscript(_uiState.value.transcriptText)
        _uiState.value = _uiState.value.copy(isAnalyzed = true)
        repository.logEvent(
            eventType = "intent.analyzed",
            orderId = _uiState.value.extractedOrderId,
            actor = "ai.engine",
            details = "Detected intent: ${_uiState.value.detectedIntent.id}, Order: ${_uiState.value.extractedOrderId ?: "N/A"}"
        )
    }

    private fun analyzeCurrentTranscript(text: String) {
        val lower = text.lowercase()
        val detected = when {
            lower.contains("cancel") || lower.contains("cancellation") ||
                    text.contains("إلغاء") || text.contains("الغاء") || text.contains("ألغ") || text.contains("الغي") || text.contains("الغى") -> IntentType.CANCEL
            lower.contains("change") || lower.contains("modify") || lower.contains("update") || lower.contains("edit") ||
                    text.contains("تعديل") || text.contains("عدّل") || text.contains("عدل") || text.contains("غيّر") || text.contains("تغيير") -> IntentType.MODIFY
            else -> IntentType.STATUS
        }

        val pattern = Pattern.compile("\\bSF[-\\s]?\\d{4}\\b", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        val extracted = if (matcher.find()) {
            matcher.group().uppercase().replace("\\s+".toRegex(), "-")
        } else {
            _uiState.value.extractedOrderId ?: "SF-1048"
        }

        _uiState.value = _uiState.value.copy(
            detectedIntent = detected,
            extractedOrderId = extracted,
            orderInputId = extracted,
            intentConfidence = "98.4%",
            urgency = if (detected == IntentType.CANCEL) "High" else "Normal"
        )
        updateDraftResponse()
    }

    fun updateOrderInput(id: String) {
        _uiState.value = _uiState.value.copy(orderInputId = id.uppercase())
    }

    fun retrieveOrder(targetId: String? = null) {
        val queryId = targetId ?: _uiState.value.orderInputId
        _uiState.value = _uiState.value.copy(isSearchingOrder = true)

        viewModelScope.launch {
            delay(300) // smooth responsive feel
            val found = repository.findOrder(queryId)
            if (found != null) {
                _uiState.value = _uiState.value.copy(
                    activeOrder = found,
                    isSearchingOrder = false,
                    isRetrieved = true,
                    lookupMessage = if (_uiState.value.language == AppLanguage.AR) "تم العثور على الطلب" else "Order found in database",
                    extractedOrderId = found.id,
                    orderInputId = found.id
                )
                repository.logEvent(
                    eventType = "order.lookup_succeeded",
                    orderId = found.id,
                    actor = "agent",
                    details = "Retrieved status: ${found.status}, Hub: ${found.lastScan}"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    activeOrder = null,
                    isSearchingOrder = false,
                    isRetrieved = false,
                    lookupMessage = if (_uiState.value.language == AppLanguage.AR) "لم يتم العثور على الطلب" else "Order not found"
                )
                repository.logEvent(
                    eventType = "order.lookup_failed",
                    orderId = queryId,
                    actor = "agent",
                    details = "No record matched ID $queryId"
                )
            }
            updateDraftResponse()
        }
    }

    private fun updateDraftResponse() {
        val order = _uiState.value.activeOrder
        val isAr = _uiState.value.language == AppLanguage.AR
        val intent = _uiState.value.detectedIntent

        val drafted = if (order == null) {
            if (isAr) "لا يوجد رد حتى يتم العثور على طلب صالح." else "No response drafted until a valid order is found."
        } else {
            when (intent) {
                IntentType.CANCEL -> {
                    if (isAr) {
                        "تم العثور على الطلب ${order.id} وحالته «${order.status}». تمت صياغة طلب إلغاء للمراجعة، ولن يُلغى الطلب قبل اعتمادك."
                    } else {
                        "Order ${order.id} was found with status \"${order.status}\". A cancellation request is ready for review; the order will not be canceled before approval."
                    }
                }
                IntentType.MODIFY -> {
                    if (isAr) {
                        "تم العثور على الطلب ${order.id} وحالته «${order.status}». تمت صياغة طلب تعديل للمراجعة، ولن يتغير الطلب قبل اعتمادك."
                    } else {
                        "Order ${order.id} was found with status \"${order.status}\". A modification request is ready for review; no order details will change before approval."
                    }
                }
                IntentType.STATUS -> {
                    if (isAr) (order.responseAr ?: order.response) else order.response
                }
            }
        }
        _uiState.value = _uiState.value.copy(draftResponse = drafted)
    }

    fun approveAction() {
        _uiState.value = _uiState.value.copy(isApproved = true)
        val order = _uiState.value.activeOrder
        repository.logEvent(
            eventType = "action.approved",
            orderId = order?.id,
            actor = "human.operator",
            details = "Approved response dispatched for intent: ${_uiState.value.detectedIntent.id}"
        )
    }

    fun updateDemoForm(name: String, email: String, notes: String) {
        _uiState.value = _uiState.value.copy(
            demoName = name,
            demoEmail = email,
            demoNotes = notes
        )
    }

    fun submitDemoRequest() {
        _uiState.value = _uiState.value.copy(isDemoSubmitted = true)
        repository.logEvent(
            eventType = "demo.requested",
            orderId = null,
            actor = "guest",
            details = "Inquiry registered for: ${_uiState.value.demoName} (${_uiState.value.demoEmail})"
        )
    }
}
