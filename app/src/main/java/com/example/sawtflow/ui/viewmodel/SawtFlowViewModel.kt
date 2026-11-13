package com.example.sawtflow.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sawtflow.data.local.AppDatabase
import com.example.sawtflow.data.local.SavedTranscript
import com.example.sawtflow.data.local.TranscriptHistoryRepository
import com.example.sawtflow.data.model.AppLanguage
import com.example.sawtflow.data.model.AuditLogEntry
import com.example.sawtflow.data.model.DemoInquiry
import com.example.sawtflow.data.model.Dialect
import com.example.sawtflow.data.model.IntentType
import com.example.sawtflow.data.model.Order
import com.example.sawtflow.data.repository.GeminiTranscriptionService
import com.example.sawtflow.data.repository.SawtFlowRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.regex.Pattern

enum class AppTab {
    STUDIO, HISTORY, ORDERS, AUDIT, OVERVIEW
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
    
    // Contact Us Email Form
    val contactEmail: String = "",
    val contactSubject: String = "",
    val contactMessage: String = "",
    val isContactSubmitted: Boolean = false,

    // Uploaded audio file state
    val uploadedAudioName: String? = null,
    val uploadedAudioSize: String? = null,
    val uploadedAudioUri: String? = null,
    val isPlayingAudio: Boolean = false,
    val playbackProgress: Float = 0f,
    val playbackPosition: String = "00:00",
    val playbackDuration: String = "00:18",

    // Gemini API Speech-to-Text State
    val isTranscribingWithGemini: Boolean = false,
    val geminiError: String? = null,
    val geminiSuccessMessage: String? = null,

    // Local History (Room Database) State
    val historySaveMessage: String? = null
)

class SawtFlowViewModel(
    application: Application,
    private val repository: SawtFlowRepository,
    private val historyRepository: TranscriptHistoryRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application = application,
        repository = SawtFlowRepository(),
        historyRepository = TranscriptHistoryRepository(
            AppDatabase.getInstance(application).savedTranscriptDao()
        )
    )

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val orders: StateFlow<List<Order>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val auditLogs: StateFlow<List<AuditLogEntry>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedTranscripts: StateFlow<List<SavedTranscript>> = historyRepository.allTranscripts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun updateContactForm(email: String, subject: String, message: String) {
        _uiState.value = _uiState.value.copy(
            contactEmail = email,
            contactSubject = subject,
            contactMessage = message,
            isContactSubmitted = false
        )
    }

    fun submitContactMessage() {
        val state = _uiState.value
        _uiState.value = state.copy(isContactSubmitted = true)
        repository.logEvent(
            eventType = "contact.email_submitted",
            orderId = null,
            actor = "guest",
            details = "Contact message sent from ${state.contactEmail} — Subject: ${state.contactSubject.ifBlank { "General Inquiry" }}"
        )
    }

    private var playbackJob: Job? = null

    fun onAudioFileUploaded(name: String, sizeStr: String, uriString: String? = null) {
        val isAr = _uiState.value.language == AppLanguage.AR
        _uiState.value = _uiState.value.copy(
            uploadedAudioName = name,
            uploadedAudioSize = sizeStr,
            uploadedAudioUri = uriString,
            isPlayingAudio = false,
            playbackProgress = 0f,
            playbackPosition = "00:00",
            audioStatusMessage = if (isAr) "تم تحميل الصوت: $name · جاهز للمعاينة والتوليد" else "Audio loaded: $name · preview before sending",
            confidence = "97.8%",
            isApproved = false,
            isRetrieved = false
        )
        // Check if file name mentions an order ID
        val pattern = Pattern.compile("\\bSF[-\\s]?\\d{4}\\b", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(name)
        if (matcher.find()) {
            val orderId = matcher.group().uppercase().replace("\\s+".toRegex(), "-")
            val sampleText = if (isAr) {
                "أحتاج أعرف تفاصيل وموقع طلبي $orderId وهل سيصل اليوم؟"
            } else {
                "I need to know the details and location of my order $orderId and if it arrives today?"
            }
            _uiState.value = _uiState.value.copy(transcriptText = sampleText)
            analyzeCurrentTranscript(sampleText)
        }

        repository.logEvent(
            eventType = "audio.uploaded",
            orderId = _uiState.value.extractedOrderId,
            actor = "user",
            details = "Uploaded voice recording: $name ($sizeStr)"
        )
    }

    fun toggleAudioPlayback() {
        if (_uiState.value.isPlayingAudio) {
            pauseAudioPlayback()
        } else {
            startAudioPlayback()
        }
    }

    private fun startAudioPlayback() {
        _uiState.value = _uiState.value.copy(isPlayingAudio = true)
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val totalSeconds = 18
            var currentSec = (_uiState.value.playbackProgress * totalSeconds).toInt()
            while (currentSec < totalSeconds && _uiState.value.isPlayingAudio) {
                delay(500)
                currentSec++
                val prog = (currentSec.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
                val posStr = String.format("%02d:%02d", currentSec / 60, currentSec % 60)
                _uiState.value = _uiState.value.copy(
                    playbackProgress = prog,
                    playbackPosition = posStr
                )
            }
            if (currentSec >= totalSeconds) {
                _uiState.value = _uiState.value.copy(
                    isPlayingAudio = false,
                    playbackProgress = 0f,
                    playbackPosition = "00:00"
                )
            }
        }
    }

    private fun pauseAudioPlayback() {
        playbackJob?.cancel()
        _uiState.value = _uiState.value.copy(isPlayingAudio = false)
    }

    fun clearUploadedAudio() {
        playbackJob?.cancel()
        val isAr = _uiState.value.language == AppLanguage.AR
        _uiState.value = _uiState.value.copy(
            uploadedAudioName = null,
            uploadedAudioSize = null,
            uploadedAudioUri = null,
            isPlayingAudio = false,
            playbackProgress = 0f,
            playbackPosition = "00:00",
            audioStatusMessage = if (isAr) "تمت إزالة الصوت المرفوع" else "Uploaded audio cleared"
        )
    }

    fun loadPresetAudio(fileName: String, sizeStr: String, textEn: String, textAr: String) {
        val isAr = _uiState.value.language == AppLanguage.AR
        val transcript = if (isAr) textAr else textEn
        _uiState.value = _uiState.value.copy(
            uploadedAudioName = fileName,
            uploadedAudioSize = sizeStr,
            uploadedAudioUri = null,
            isPlayingAudio = false,
            playbackProgress = 0f,
            playbackPosition = "00:00",
            transcriptText = transcript,
            audioStatusMessage = if (isAr) "تم تحميل العينة: $fileName" else "Preset audio sample loaded: $fileName",
            confidence = "98.2%",
            isApproved = false,
            isRetrieved = false
        )
        analyzeCurrentTranscript(transcript)
        repository.logEvent(
            eventType = "audio.preset_loaded",
            orderId = _uiState.value.extractedOrderId,
            actor = "user",
            details = "Loaded preset audio recording: $fileName"
        )
    }

    private val geminiService = GeminiTranscriptionService()

    fun transcribeAudioWithGemini(context: Context) {
        val currentState = _uiState.value
        val isAr = currentState.language == AppLanguage.AR
        val audioUri = currentState.uploadedAudioUri?.let { Uri.parse(it) }

        _uiState.value = currentState.copy(
            isTranscribingWithGemini = true,
            geminiError = null,
            geminiSuccessMessage = null,
            audioStatusMessage = if (isAr) "جارٍ إرسال التسجيل إلى Gemini للتعرف على الصوت العربي..." else "Sending audio to Gemini API for Arabic transcription..."
        )

        viewModelScope.launch {
            try {
                val result = geminiService.transcribeArabicAudio(
                    context = context,
                    audioUri = audioUri,
                    audioFileName = currentState.uploadedAudioName,
                    dialectHint = currentState.selectedDialect.labelEn
                )

                _uiState.value = _uiState.value.copy(
                    transcriptText = result.transcript,
                    confidence = result.confidenceScore,
                    isTranscribingWithGemini = false,
                    geminiSuccessMessage = if (isAr) "تم التفريغ الصوتي بنجاح عبر Gemini 3.8 Flash" else "Successfully transcribed with Gemini 3.8 Flash",
                    audioStatusMessage = if (isAr) "اكتمل التفريغ عبر Gemini API" else "Gemini transcription completed ✓",
                    isApproved = false,
                    isRetrieved = false
                )

                // Run intent analysis on the newly transcribed text
                analyzeCurrentTranscript(result.transcript)

                // Auto retrieve if an order ID was extracted
                result.extractedOrderHint?.let { orderId ->
                    retrieveOrder(orderId)
                }

                // Automatically save successful transcript to local Room database
                viewModelScope.launch {
                    try {
                        historyRepository.saveTranscript(
                            transcriptText = result.transcript,
                            audioFileName = currentState.uploadedAudioName ?: "Live Voice Recording",
                            dialect = result.detectedDialect,
                            detectedIntent = _uiState.value.detectedIntent.id,
                            extractedOrderId = _uiState.value.extractedOrderId,
                            confidence = result.confidenceScore,
                            source = "Gemini 3.8 Flash"
                        )
                        repository.logEvent(
                            eventType = "transcript.saved_to_room",
                            orderId = _uiState.value.extractedOrderId,
                            actor = "room.database",
                            details = "Persisted transcript to Room database: '${result.transcript.take(40)}...'"
                        )
                    } catch (e: Exception) {
                        // Non-blocking log
                    }
                }

                repository.logEvent(
                    eventType = "gemini.transcription_completed",
                    orderId = _uiState.value.extractedOrderId,
                    actor = "gemini-3.8-flash",
                    details = "Transcribed Arabic speech (${result.detectedDialect}): '${result.transcript.take(60)}...'"
                )

            } catch (e: TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isTranscribingWithGemini = false,
                    geminiError = if (isAr) "انتهت مهلة الاتصال بـ Gemini API بعد 60 ثانية. يرجى المحاولة مرة أخرى." else "Gemini transcription timed out after 60 seconds. Please try again."
                )
                repository.logEvent(
                    eventType = "gemini.transcription_timeout",
                    orderId = _uiState.value.extractedOrderId,
                    actor = "gemini-3.8-flash",
                    details = "Request timed out after 60 seconds."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Gemini API request failed."
                _uiState.value = _uiState.value.copy(
                    isTranscribingWithGemini = false,
                    geminiError = if (isAr) "تعذر إكمال التفريغ عبر Gemini: $errorMsg" else "Gemini transcription failed: $errorMsg"
                )
                repository.logEvent(
                    eventType = "gemini.transcription_failed",
                    orderId = _uiState.value.extractedOrderId,
                    actor = "gemini-3.8-flash",
                    details = "Error: $errorMsg"
                )
            }
        }
    }

    fun saveCurrentTranscriptToHistory(source: String = "Studio Saved") {
        val state = _uiState.value
        viewModelScope.launch {
            try {
                historyRepository.saveTranscript(
                    transcriptText = state.transcriptText,
                    audioFileName = state.uploadedAudioName ?: "Studio Transcript",
                    dialect = state.selectedDialect.labelEn,
                    detectedIntent = state.detectedIntent.id,
                    extractedOrderId = state.extractedOrderId,
                    confidence = state.confidence,
                    source = source
                )
                val msg = if (state.language == AppLanguage.AR) "تم حفظ التفريغ في قاعدة البيانات المحلية (Room) ✓" else "Saved to Room Database history ✓"
                _uiState.value = _uiState.value.copy(historySaveMessage = msg)
                repository.logEvent(
                    eventType = "transcript.saved_to_room",
                    orderId = state.extractedOrderId,
                    actor = "operator",
                    details = "Manual transcript saved to Room DB: '${state.transcriptText.take(40)}...'"
                )
                delay(3000)
                _uiState.value = _uiState.value.copy(historySaveMessage = null)
            } catch (e: Exception) {
                val msg = if (state.language == AppLanguage.AR) "تعذر الحفظ: ${e.message}" else "Save failed: ${e.message}"
                _uiState.value = _uiState.value.copy(historySaveMessage = msg)
            }
        }
    }

    fun deleteSavedTranscript(id: Long) {
        viewModelScope.launch {
            historyRepository.deleteTranscript(id)
            repository.logEvent(
                eventType = "transcript.deleted_from_room",
                orderId = null,
                actor = "operator",
                details = "Deleted transcript record #$id from Room DB"
            )
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
            repository.logEvent(
                eventType = "history.cleared",
                orderId = null,
                actor = "operator",
                details = "Cleared all local transcripts from Room database"
            )
        }
    }

    fun loadTranscriptIntoStudio(saved: SavedTranscript) {
        val isAr = _uiState.value.language == AppLanguage.AR
        _uiState.value = _uiState.value.copy(
            transcriptText = saved.transcript,
            confidence = saved.confidence,
            currentTab = AppTab.STUDIO,
            audioStatusMessage = if (isAr) "تم تحميل التفريغ من السجل: ${saved.formattedTime}" else "Loaded from Room History: ${saved.formattedTime}",
            isApproved = false,
            isRetrieved = false
        )
        analyzeCurrentTranscript(saved.transcript)
        saved.extractedOrderId?.let { retrieveOrder(it) }
        repository.logEvent(
            eventType = "transcript.loaded_from_history",
            orderId = saved.extractedOrderId,
            actor = "operator",
            details = "Loaded saved transcript #${saved.id} into Studio"
        )
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SawtFlowViewModel(application) as T
                }
            }
    }
}
