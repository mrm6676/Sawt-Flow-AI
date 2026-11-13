const DIALECTS = {
  gulf: {
    id: 'gulf',
    labelAr: 'خليجي / سعودي',
    labelEn: 'Gulf / Saudi',
    sampleAr: 'أبغى أعرف وين وصل طلبي SF-1048، قالوا بيوصل اليوم.',
    sampleEn: 'I want to know where my order SF-1048 is; they said it would arrive today.'
  },
  egyptian: {
    id: 'egyptian',
    labelAr: 'مصري',
    labelEn: 'Egyptian',
    sampleAr: 'ممكن أعرف الطلب SF-2086 وصل لفين؟ قالولي هييجي النهارده.',
    sampleEn: 'Can I check where order SF-2086 has reached? They told me it would arrive today.'
  },
  levantine: {
    id: 'levantine',
    labelAr: 'شامي',
    labelEn: 'Levantine',
    sampleAr: 'بدي أعرف وين صار طلبي SF-3312، حكولي بيوصل اليوم.',
    sampleEn: 'I want to know where my order SF-3312 is; they told me it would arrive today.'
  },
  maghrebi: {
    id: 'maghrebi',
    labelAr: 'مغاربي',
    labelEn: 'Maghrebi',
    sampleAr: 'بغيت نعرف فين وصل الطلب ديالي SF-4420، قالو غادي يوصل اليوم.',
    sampleEn: 'I would like to know where my order SF-4420 is; they said it would arrive today.'
  }
};

const STORAGE_KEY = 'sawtflow_studio_history_v1';

let state = {
  lang: 'ar',
  dialect: 'gulf',
  intent: 'status',
  extractedOrderId: 'SF-1048',
  orders: [],
  auditLogs: [],
  uploadedFileName: null,
  isAudioApproved: false,
  isRecording: false,
  mediaRecorder: null,
  audioChunks: []
};

document.addEventListener('DOMContentLoaded', async () => {
  await loadOrders();
  initTabs();
  initLanguageToggle();
  initDialects();
  initDropzone();
  initRecorder();
  initActions();
  setDialect('gulf');
  logEvent('system.startup', null, 'system', 'SawtFlow Studio initialized with 11 operational order records');
  renderHistory();
});

async function loadOrders() {
  try {
    const res = await fetch('/studio/mock-orders.json');
    state.orders = await res.json();
  } catch (e) {
    state.orders = [];
  }
  renderQuickOrderChips();
  renderOrdersPanel();
  lookupOrder('SF-1048');
}

function initTabs() {
  const tabBtns = document.querySelectorAll('.tab-btn');
  tabBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const target = btn.dataset.tab;
      tabBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
      document.getElementById(`panel-${target}`).classList.add('active');
    });
  });
}

function initLanguageToggle() {
  const btn = document.getElementById('langToggleBtn');
  btn.addEventListener('click', () => {
    state.lang = state.lang === 'ar' ? 'en' : 'ar';
    document.documentElement.lang = state.lang;
    document.documentElement.dir = state.lang === 'ar' ? 'rtl' : 'ltr';
    btn.textContent = state.lang === 'ar' ? 'EN' : 'عربي';

    document.querySelectorAll('[data-ar][data-en]').forEach(el => {
      el.textContent = state.lang === 'ar' ? el.dataset.ar : el.dataset.en;
    });

    const approveAudioBtn = document.getElementById('approveAudioBtn');
    const audioStatusPill = document.getElementById('audioStatusPill');
    if (approveAudioBtn && state.isAudioApproved) {
      approveAudioBtn.textContent = state.lang === 'ar' ? 'تم اعتماد التسجيل الصوتي ✓' : 'Voice Recording Approved ✓';
      if (audioStatusPill) {
        audioStatusPill.textContent = state.lang === 'ar' ? 'تم اعتماد التسجيل ✓' : 'RECORDING APPROVED ✓';
      }
    } else if (audioStatusPill) {
      audioStatusPill.textContent = state.uploadedFileName
        ? (state.lang === 'ar' ? 'تم تحميل الملف ✓' : 'AUDIO READY ✓')
        : (state.lang === 'ar' ? 'جاهز' : 'READY');
    }

    setDialect(state.dialect);
    renderHistory();
    renderOrdersPanel();
    lookupOrder(state.extractedOrderId);
  });
}

function initDialects() {
  const chips = document.querySelectorAll('#dialectChips .chip');
  chips.forEach(chip => {
    chip.addEventListener('click', () => {
      chips.forEach(c => c.classList.remove('selected'));
      chip.classList.add('selected');
      setDialect(chip.dataset.dialect);
    });
  });
}

function setDialect(key) {
  state.dialect = key;
  const d = DIALECTS[key];
  const sample = state.lang === 'ar' ? d.sampleAr : d.sampleEn;
  document.getElementById('transcriptInput').value = sample;
  document.getElementById('detectedDialectVal').textContent = state.lang === 'ar' ? d.labelAr : d.labelEn;
  analyzeTranscript(sample);
}

function initDropzone() {
  const dropzone = document.getElementById('dropzone');
  const fileInput = document.getElementById('audioFileInput');

  dropzone.addEventListener('click', () => fileInput.click());
  dropzone.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropzone.classList.add('dragover');
  });
  dropzone.addEventListener('dragleave', () => dropzone.classList.remove('dragover'));
  dropzone.addEventListener('drop', (e) => {
    e.preventDefault();
    dropzone.classList.remove('dragover');
    if (e.dataTransfer.files.length) {
      handleAudioFile(e.dataTransfer.files[0]);
    }
  });
  fileInput.addEventListener('change', (e) => {
    if (e.target.files.length) {
      handleAudioFile(e.target.files[0]);
    }
  });
}

function handleAudioFile(file) {
  state.uploadedFileName = file.name;
  state.isAudioApproved = false;
  const sizeKB = Math.max(1, Math.round(file.size / 1024));
  document.getElementById('uploadedFileName').textContent = file.name;
  document.getElementById('uploadedFileSize').textContent = sizeKB > 1024 ? `${(sizeKB / 1024).toFixed(1)} MB` : `${sizeKB} KB`;
  document.getElementById('audioPlayerBox').classList.add('show');
  document.getElementById('audioPreview').src = URL.createObjectURL(file);
  document.getElementById('audioStatusPill').textContent = state.lang === 'ar' ? 'تم تحميل الملف ✓' : 'AUDIO READY ✓';
  document.getElementById('audioStatusPill').className = 'status-pill';

  const approveAudioBtn = document.getElementById('approveAudioBtn');
  if (approveAudioBtn) {
    approveAudioBtn.disabled = false;
    approveAudioBtn.textContent = state.lang === 'ar' ? '✓ اعتماد التسجيل الصوتي' : '✓ Approve Voice Recording';
  }

  const match = file.name.match(/\bSF[-\s]?\d{4}\b/i);
  if (match) {
    const orderId = match[0].toUpperCase().replace(/\s+/, '-');
    const txt = state.lang === 'ar'
      ? `أبغى أعرف تفاصيل طلبي ${orderId} وهل بيوصل اليوم؟`
      : `I need to check the details for order ${orderId} and when it arrives.`;
    document.getElementById('transcriptInput').value = txt;
    analyzeTranscript(txt);
  }
  logEvent('audio.uploaded', state.extractedOrderId, 'user', `Uploaded voice file: ${file.name}`);
}

function initRecorder() {
  const btn = document.getElementById('recordBtn');
  btn.addEventListener('click', async () => {
    if (!state.isRecording) {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        state.mediaRecorder = new MediaRecorder(stream);
        state.audioChunks = [];
        state.mediaRecorder.ondataavailable = (e) => state.audioChunks.push(e.data);
        state.mediaRecorder.onstop = () => {
          const blob = new Blob(state.audioChunks, { type: 'audio/webm' });
          const file = new File([blob], 'live-voice-recording.webm', { type: 'audio/webm' });
          handleAudioFile(file);
          stream.getTracks().forEach(t => t.stop());
        };
        state.mediaRecorder.start();
        state.isRecording = true;
        btn.textContent = state.lang === 'ar' ? '⏹ إيقاف التسجيل' : '⏹ Stop Recording';
        document.getElementById('audioStatusPill').textContent = state.lang === 'ar' ? 'جارٍ التسجيل...' : 'RECORDING...';
      } catch (err) {
        // Fallback simulation if microphone permission is unavailable
        const txt = state.lang === 'ar'
          ? 'لو سمحت أبغى ألغي طلبي SF-5177 لأن الموعد ما يناسبني.'
          : 'Please cancel my order SF-5177 because the delivery time does not work for me.';
        document.getElementById('transcriptInput').value = txt;
        analyzeTranscript(txt);
        document.getElementById('audioStatusPill').textContent = state.lang === 'ar' ? 'عينة صوتية جاهزة' : 'SAMPLE CAPTURED';
        logEvent('audio.simulated', 'SF-5177', 'user', 'Simulated voice recording loaded');
      }
    } else {
      state.mediaRecorder.stop();
      state.isRecording = false;
      btn.textContent = state.lang === 'ar' ? '🎙️ تسجيل صوتي' : '🎙️ Record Voice';
    }
  });
}

function initActions() {
  document.getElementById('loadSampleBtn').addEventListener('click', () => {
    const txt = state.lang === 'ar'
      ? 'السلام عليكم، الطلب SF-4420 تأخر علي، متى بيوصل؟'
      : 'Hello, my order SF-4420 is delayed, when will it arrive?';
    document.getElementById('transcriptInput').value = txt;
    analyzeTranscript(txt);
    logEvent('audio.sample_loaded', 'SF-4420', 'user', 'Loaded SF-4420 delayed order sample');
  });

  document.getElementById('analyzeBtn').addEventListener('click', () => {
    const text = document.getElementById('transcriptInput').value;
    analyzeTranscript(text);
    logEvent('intent.analyzed', state.extractedOrderId, 'ai.engine', `Detected intent: ${state.intent}`);
  });

  document.getElementById('saveHistoryBtn').addEventListener('click', saveCurrentToHistory);

  document.getElementById('lookupBtn').addEventListener('click', () => {
    lookupOrder(document.getElementById('orderLookupInput').value);
  });

  document.getElementById('approveBtn').addEventListener('click', () => {
    const pill = document.getElementById('approvalPill');
    pill.textContent = state.lang === 'ar' ? 'تم الاعتماد ✓' : 'APPROVED ✓';
    pill.className = 'status-pill green';
    document.getElementById('approvedBanner').classList.add('show');
    logEvent('action.approved', state.extractedOrderId, 'human.operator', `Approved response for ${state.extractedOrderId}`);
  });

  const approveAudioBtn = document.getElementById('approveAudioBtn');
  if (approveAudioBtn) {
    approveAudioBtn.addEventListener('click', () => {
      state.isAudioApproved = true;
      approveAudioBtn.disabled = true;
      approveAudioBtn.textContent = state.lang === 'ar' ? 'تم اعتماد التسجيل الصوتي ✓' : 'Voice Recording Approved ✓';
      const statusPill = document.getElementById('audioStatusPill');
      statusPill.textContent = state.lang === 'ar' ? 'تم اعتماد التسجيل ✓' : 'RECORDING APPROVED ✓';
      statusPill.className = 'status-pill green';
      logEvent('audio.recording_approved', state.extractedOrderId, 'human.operator', `Approved uploaded voice recording: ${state.uploadedFileName}`);
    });
  }

  document.getElementById('historySearchInput').addEventListener('input', renderHistory);
  document.getElementById('clearHistoryBtn').addEventListener('click', () => {
    localStorage.removeItem(STORAGE_KEY);
    renderHistory();
    logEvent('history.cleared', null, 'operator', 'Cleared saved transcript history');
  });
}

function analyzeTranscript(text) {
  const lower = text.toLowerCase();
  if (lower.includes('cancel') || text.includes('ألغي') || text.includes('إلغاء') || text.includes('الغاء')) {
    state.intent = 'cancel';
  } else if (lower.includes('modify') || lower.includes('change') || text.includes('تعديل') || text.includes('غيّر')) {
    state.intent = 'modify';
  } else {
    state.intent = 'status';
  }

  const intentLabels = {
    status: state.lang === 'ar' ? 'حالة الطلب' : 'Order status',
    cancel: state.lang === 'ar' ? 'إلغاء الطلب' : 'Cancel order',
    modify: state.lang === 'ar' ? 'تعديل الطلب' : 'Modify order'
  };
  document.getElementById('detectedIntentVal').textContent = intentLabels[state.intent];
  document.getElementById('urgencyVal').textContent = state.intent === 'cancel'
    ? (state.lang === 'ar' ? 'عالية (High)' : 'High')
    : (state.lang === 'ar' ? 'عادية (Normal)' : 'Normal');

  const match = text.match(/\bSF[-\s]?\d{4}\b/i);
  if (match) {
    state.extractedOrderId = match[0].toUpperCase().replace(/\s+/, '-');
    document.getElementById('extractedOrderVal').textContent = state.extractedOrderId;
    document.getElementById('orderLookupInput').value = state.extractedOrderId;
  }

  lookupOrder(state.extractedOrderId);
}

function lookupOrder(orderId) {
  const norm = (orderId || '').trim().toUpperCase();
  const order = state.orders.find(o => o.id === norm);
  const box = document.getElementById('orderEvidenceBox');
  const draftEl = document.getElementById('draftResponseInput');

  document.getElementById('approvalPill').textContent = state.lang === 'ar' ? 'بانتظار الاعتماد' : 'AWAITING APPROVAL';
  document.getElementById('approvalPill').className = 'status-pill amber';
  document.getElementById('approvedBanner').classList.remove('show');

  if (!order) {
    box.innerHTML = `<div style="color: var(--coral); font-weight: 700;">${state.lang === 'ar' ? 'لم يتم العثور على الطلب: ' : 'Order not found: '}${norm}</div>`;
    draftEl.value = state.lang === 'ar' ? 'يرجى التحقق من رقم الطلب قبل إرسال الرد.' : 'Please verify a valid order ID before dispatching.';
    return;
  }

  state.extractedOrderId = order.id;
  box.innerHTML = `
    <div style="display:flex; justify-content:space-between; align-items:center;">
      <strong class="mono" style="color:var(--cyan); font-size:1.1rem;">${order.id}</strong>
      <span class="status-pill">${order.status}</span>
    </div>
    <div><strong>${state.lang === 'ar' ? 'المدينة:' : 'City:'}</strong> ${order.city} · <strong>ETA:</strong> ${order.eta}</div>
    <div style="color: var(--text-muted); font-size:0.9rem;">${order.lastScan}</div>
  `;

  if (state.intent === 'cancel') {
    draftEl.value = state.lang === 'ar'
      ? `تم العثور على الطلب ${order.id} (${order.status}). تم تجهيز طلب الإلغاء للمراجعة ولن يُلغى قبل اعتمادك.`
      : `Order ${order.id} (${order.status}) retrieved. Cancellation request drafted; no change occurs before approval.`;
  } else {
    draftEl.value = state.lang === 'ar' ? (order.responseAr || order.response) : order.response;
  }
}

function renderQuickOrderChips() {
  const container = document.getElementById('quickOrderChips');
  container.innerHTML = '';
  state.orders.slice(0, 6).forEach(o => {
    const btn = document.createElement('button');
    btn.className = 'chip mono';
    btn.textContent = o.id;
    btn.addEventListener('click', () => {
      document.getElementById('orderLookupInput').value = o.id;
      lookupOrder(o.id);
    });
    container.appendChild(btn);
  });
}

function renderOrdersPanel() {
  const list = document.getElementById('ordersList');
  list.innerHTML = '';
  state.orders.forEach(o => {
    const card = document.createElement('div');
    card.className = 'item-card';
    card.innerHTML = `
      <div class="item-card-top">
        <strong class="mono" style="color: var(--cyan); font-size: 1.05rem;">${o.id}</strong>
        <span class="status-pill">${o.status}</span>
      </div>
      <div style="font-size:0.9rem;">${o.city} · ${o.eta}</div>
      <div style="color: var(--text-muted); font-size:0.85rem;">${o.lastScan}</div>
      <button class="btn btn-outline" type="button" style="margin-top:auto;">
        ${state.lang === 'ar' ? 'اختبار في الاستوديو ←' : 'Load into Studio →'}
      </button>
    `;
    card.querySelector('button').addEventListener('click', () => {
      const txt = state.lang === 'ar'
        ? `أبغى أعرف حالة الطلب ${o.id}`
        : `I want to check the status of order ${o.id}`;
      document.getElementById('transcriptInput').value = txt;
      analyzeTranscript(txt);
      document.querySelector('.tab-btn[data-tab="studio"]').click();
    });
    list.appendChild(card);
  });
}

function saveCurrentToHistory() {
  const text = document.getElementById('transcriptInput').value.trim();
  if (!text) return;
  const items = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
  items.unshift({
    id: Date.now(),
    transcript: text,
    dialect: DIALECTS[state.dialect][state.lang === 'ar' ? 'labelAr' : 'labelEn'],
    orderId: state.extractedOrderId,
    intent: document.getElementById('detectedIntentVal').textContent,
    fileName: state.uploadedFileName || (state.lang === 'ar' ? 'عينة استوديو' : 'Studio Input'),
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  });
  localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
  const banner = document.getElementById('historySavedBanner');
  banner.classList.add('show');
  setTimeout(() => banner.classList.remove('show'), 2500);
  renderHistory();
  logEvent('transcript.saved', state.extractedOrderId, 'operator', 'Saved transcript to local browser storage');
}

function renderHistory() {
  const list = document.getElementById('historyList');
  const q = (document.getElementById('historySearchInput').value || '').toLowerCase();
  const items = JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]').filter(item =>
    item.transcript.toLowerCase().includes(q) ||
    (item.orderId || '').toLowerCase().includes(q) ||
    (item.dialect || '').toLowerCase().includes(q)
  );

  if (!items.length) {
    list.innerHTML = `<div class="item-card" style="grid-column: 1 / -1; text-align:center; color: var(--text-muted);">
      ${state.lang === 'ar' ? 'لا توجد تفريغات محفوظة بعد.' : 'No saved transcripts yet.'}
    </div>`;
    return;
  }

  list.innerHTML = '';
  items.forEach(item => {
    const card = document.createElement('div');
    card.className = 'item-card';
    card.innerHTML = `
      <div class="item-card-top">
        <span class="status-pill">${item.dialect}</span>
        <span class="mono" style="font-size:0.8rem; color: var(--text-muted);">${item.time}</span>
      </div>
      <p style="font-size:0.95rem;">${item.transcript}</p>
      <div class="item-card-top" style="margin-top:auto;">
        <span class="mono" style="color:var(--cyan); font-size:0.85rem;">${item.orderId || ''}</span>
        <button class="btn btn-cyan" type="button" style="padding:0.45rem 0.85rem; font-size:0.82rem;">
          ${state.lang === 'ar' ? 'فتح بالاستوديو' : 'Load'}
        </button>
      </div>
    `;
    card.querySelector('button').addEventListener('click', () => {
      document.getElementById('transcriptInput').value = item.transcript;
      analyzeTranscript(item.transcript);
      document.querySelector('.tab-btn[data-tab="studio"]').click();
    });
    list.appendChild(card);
  });
}

function logEvent(type, orderId, actor, details) {
  state.auditLogs.unshift({
    type,
    orderId: orderId || 'N/A',
    actor,
    details,
    time: new Date().toLocaleTimeString()
  });
  const container = document.getElementById('auditList');
  container.innerHTML = '';
  state.auditLogs.forEach(entry => {
    const div = document.createElement('div');
    div.className = 'item-card';
    div.innerHTML = `
      <div class="item-card-top">
        <strong class="mono" style="color:var(--cyan);">${entry.type}</strong>
        <span class="mono" style="color:var(--text-muted); font-size:0.8rem;">${entry.time} · ${entry.actor}</span>
      </div>
      <div style="font-size:0.92rem;">${entry.details} <span class="mono" style="color:var(--amber);">[${entry.orderId}]</span></div>
    `;
    container.appendChild(div);
  });
}
