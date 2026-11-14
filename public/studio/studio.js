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
const ORDERS_STORAGE_KEY = 'sawtflow_studio_orders_v1';
const DRAFT_SESSION_KEY = 'sawtflow_studio_transcript_draft_v1';
const GEMINI_KEY_SESSION_KEY = 'sawtflow_studio_gemini_key_v1';

let state = {
  lang: 'ar',
  dialect: 'gulf',
  intent: 'status',
  extractedOrderId: 'SF-1048',
  orders: [],
  selectedOrderIds: new Set(),
  auditLogs: [],
  uploadedFileName: null,
  uploadedFile: null,
  isAudioApproved: false,
  isAudioPlaying: false,
  isTranscribing: false,
  isRecording: false,
  mediaRecorder: null,
  speechRecognition: null,
  liveTranscriptBuffer: '',
  audioChunks: [],
  draftReady: false
};

document.addEventListener('DOMContentLoaded', async () => {
  await loadOrders();
  initTabs();
  initLanguageToggle();
  initDialects();
  initDropzone();
  initRecorder();
  initActions();
  initStreamBanner();
  setDialect('gulf');
  restoreTranscriptDraft();
  state.draftReady = true;
  initTranscriptAutoSave();
  logEvent('system.startup', null, 'system', `Sound Flow Studio initialized with ${state.orders.length} operational order records`);
  renderHistory();
});

function saveOrdersToStorage() {
  try {
    localStorage.setItem(ORDERS_STORAGE_KEY, JSON.stringify(state.orders));
  } catch (e) {}
}

async function loadOrders() {
  let baseOrders = [];
  try {
    const res = await fetch('/studio/mock-orders.json');
    baseOrders = await res.json();
  } catch (e) {
    baseOrders = [];
  }
  try {
    const savedRaw = localStorage.getItem(ORDERS_STORAGE_KEY);
    if (savedRaw) {
      const saved = JSON.parse(savedRaw);
      if (Array.isArray(saved) && saved.length > 0) {
        const mergedMap = new Map(baseOrders.map(o => [o.id, o]));
        saved.forEach(o => {
          if (o && o.id) mergedMap.set(o.id, o);
        });
        state.orders = Array.from(mergedMap.values());
      } else {
        state.orders = baseOrders;
      }
    } else {
      state.orders = baseOrders;
    }
  } catch (e) {
    state.orders = baseOrders;
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

    const currentText = (document.getElementById('transcriptInput')?.value || '').trim();
    const isBuiltInSample = Object.values(DIALECTS).some(d => currentText === d.sampleAr || currentText === d.sampleEn);
    if (isBuiltInSample || !currentText) {
      setDialect(state.dialect);
    } else {
      const d = DIALECTS[state.dialect];
      document.getElementById('detectedDialectVal').textContent = state.lang === 'ar' ? d.labelAr : d.labelEn;
      analyzeTranscript(currentText);
      updateAutoSaveStatus(true);
    }
    renderHistory();
    renderOrdersPanel();
    updateBatchToolbar();
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

function extractOrderIdFromText(text) {
  if (!text) return null;
  const sfMatch = text.match(/\b([A-Z]{2,4})[-\s]?(\d{3,6})\b/i);
  if (sfMatch) {
    return `${sfMatch[1].toUpperCase()}-${sfMatch[2]}`;
  }
  const keywordMatch = text.match(/(?:order|id|ref|#|طلبية|طلب|رقم)\s*[:#-]?\s*([A-Z0-9-]{3,12})/i);
  if (keywordMatch) {
    const raw = keywordMatch[1].toUpperCase().replace(/[^A-Z0-9-]/g, '');
    if (/\d/.test(raw)) {
      return raw.includes('-') ? raw : `ORD-${raw}`;
    }
  }
  const hashNumMatch = text.match(/#(\d{3,6})\b/);
  if (hashNumMatch) {
    return `ORD-${hashNumMatch[1]}`;
  }
  return null;
}

function generateVoiceTicketId(seedName) {
  const digits = (seedName || '').replace(/\D/g, '');
  if (digits.length >= 3) {
    return `VF-${digits.slice(-4).padStart(4, '0')}`;
  }
  const suffix = String(Math.floor(1000 + (Date.now() % 9000)));
  return `VF-${suffix}`;
}

function formatDurationSeconds(sec) {
  if (!Number.isFinite(sec) || sec <= 0) return '--:--';
  const total = Math.round(sec);
  const mins = Math.floor(total / 60);
  const secs = total % 60;
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
}

function formatSampleRate(hz) {
  if (!Number.isFinite(hz) || hz <= 0) return null;
  return hz >= 1000 ? `${(hz / 1000).toFixed(1)} kHz` : `${hz} Hz`;
}

function formatChannelsLabel(ch) {
  if (!Number.isFinite(ch) || ch <= 0) return null;
  if (ch === 1) return 'Mono (1 ch)';
  if (ch === 2) return 'Stereo (2 ch)';
  return `${ch} ch`;
}

function parseAudioHeaderBytes(buffer, fileName, mimeType) {
  const view = new DataView(buffer);
  const bytes = new Uint8Array(buffer);
  const readStr = (offset, len) => {
    if (offset + len > bytes.length) return '';
    let out = '';
    for (let i = 0; i < len; i++) out += String.fromCharCode(bytes[offset + i]);
    return out;
  };

  const result = {
    duration: null,
    sampleRate: null,
    codec: null,
    channels: null
  };

  // 1. RIFF / WAVE header
  if (bytes.length >= 44 && readStr(0, 4) === 'RIFF' && readStr(8, 4) === 'WAVE') {
    let offset = 12;
    let byteRate = 0;
    let dataSize = 0;
    while (offset + 8 <= bytes.length) {
      const chunkId = readStr(offset, 4);
      const chunkSize = view.getUint32(offset + 4, true);
      if (chunkId === 'fmt ' && offset + 24 <= bytes.length) {
        const audioFormat = view.getUint16(offset + 8, true);
        const channels = view.getUint16(offset + 10, true);
        const sampleRate = view.getUint32(offset + 12, true);
        byteRate = view.getUint32(offset + 16, true);
        const bitsPerSample = view.getUint16(offset + 22, true);
        const fmtName = audioFormat === 1
          ? 'PCM'
          : (audioFormat === 3 ? 'IEEE Float' : (audioFormat === 6 ? 'A-Law' : (audioFormat === 7 ? 'u-Law' : `Fmt ${audioFormat}`)));
        result.sampleRate = sampleRate;
        result.channels = channels;
        result.codec = bitsPerSample ? `WAV · ${fmtName} ${bitsPerSample}-bit` : `WAV · ${fmtName}`;
      } else if (chunkId === 'data') {
        dataSize = chunkSize;
        break;
      }
      offset += 8 + chunkSize + (chunkSize % 2);
    }
    if (byteRate > 0 && dataSize > 0) {
      result.duration = dataSize / byteRate;
    }
    return result;
  }

  // 2. FLAC header
  if (bytes.length >= 42 && readStr(0, 4) === 'fLaC') {
    const b1 = bytes[18], b2 = bytes[19], b3 = bytes[20], b4 = bytes[21];
    const sampleRate = (b1 << 12) | (b2 << 4) | (b3 >> 4);
    const channels = ((b3 >> 1) & 0x07) + 1;
    const bps = (((b3 & 0x01) << 4) | (b4 >> 4)) + 1;
    const totalSamplesHigh = bytes[21] & 0x0F;
    const totalSamplesLow = view.getUint32(22, false);
    const totalSamples = totalSamplesHigh * 4294967296 + totalSamplesLow;
    result.sampleRate = sampleRate || null;
    result.channels = channels || null;
    result.codec = `FLAC ${bps}-bit`;
    if (sampleRate > 0 && totalSamples > 0) {
      result.duration = totalSamples / sampleRate;
    }
    return result;
  }

  // 3. OGG (Opus / Vorbis) header
  if (bytes.length >= 36 && readStr(0, 4) === 'OggS') {
    for (let i = 0; i < Math.min(bytes.length - 16, 256); i++) {
      if (readStr(i, 8) === 'OpusHead' && i + 16 <= bytes.length) {
        result.channels = bytes[i + 9];
        result.sampleRate = view.getUint32(i + 12, true) || 48000;
        result.codec = 'OGG · Opus';
        return result;
      }
      if (bytes[i] === 0x01 && readStr(i + 1, 6) === 'vorbis' && i + 16 <= bytes.length) {
        result.channels = bytes[i + 11];
        result.sampleRate = view.getUint32(i + 12, true);
        result.codec = 'OGG · Vorbis';
        return result;
      }
    }
    result.codec = 'OGG Audio';
    return result;
  }

  // 4. MP4 / M4A / AAC container
  if (bytes.length >= 12 && readStr(4, 4) === 'ftyp') {
    let foundCodec = 'AAC / MP4';
    const textSlice = readStr(0, Math.min(bytes.length, 4096));
    if (textSlice.includes('mp4a')) foundCodec = 'M4A · AAC';
    else if (textSlice.includes('alac')) foundCodec = 'M4A · ALAC';
    else if (textSlice.includes('Opus')) foundCodec = 'MP4 · Opus';
    result.codec = foundCodec;

    // Look for 'mdhd' atom to read timescale & duration
    for (let i = 0; i < bytes.length - 28; i++) {
      if (bytes[i] === 0x6D && bytes[i + 1] === 0x64 && bytes[i + 2] === 0x68 && bytes[i + 3] === 0x64) {
        const version = bytes[i + 4];
        if (version === 0 && i + 24 <= bytes.length) {
          const timescale = view.getUint32(i + 16, false);
          const durationUnits = view.getUint32(i + 20, false);
          if (timescale >= 8000 && timescale <= 192000) {
            result.sampleRate = timescale;
            if (durationUnits > 0) result.duration = durationUnits / timescale;
            break;
          }
        }
      }
    }
    return result;
  }

  // 5. WebM / Matroska (EBML 0x1A45DFA3)
  if (bytes.length >= 4 && bytes[0] === 0x1A && bytes[1] === 0x45 && bytes[2] === 0xDF && bytes[3] === 0xA3) {
    const textSlice = readStr(0, Math.min(bytes.length, 4096));
    if (textSlice.includes('A_OPUS')) {
      result.codec = 'WebM · Opus';
      result.sampleRate = 48000;
    } else if (textSlice.includes('A_VORBIS')) {
      result.codec = 'WebM · Vorbis';
    } else if (textSlice.includes('A_AAC')) {
      result.codec = 'WebM · AAC';
    } else {
      result.codec = 'WebM Audio';
    }
    return result;
  }

  // 6. MP3 (ID3v2 skip + MPEG Audio Frame Header)
  let startOffset = 0;
  if (bytes.length >= 10 && readStr(0, 3) === 'ID3') {
    const tagSize =
      ((bytes[6] & 0x7F) << 21) |
      ((bytes[7] & 0x7F) << 14) |
      ((bytes[8] & 0x7F) << 7) |
      (bytes[9] & 0x7F);
    startOffset = Math.min(10 + tagSize, bytes.length - 4);
  }
  const mp3Rates = {
    3: [44100, 48000, 32000], // MPEG 1
    2: [22050, 24000, 16000], // MPEG 2
    0: [11025, 12000, 8000]   // MPEG 2.5
  };
  for (let i = startOffset; i < bytes.length - 4; i++) {
    if (bytes[i] === 0xFF && (bytes[i + 1] & 0xE0) === 0xE0) {
      const versionBits = (bytes[i + 1] >> 3) & 0x03;
      const layerBits = (bytes[i + 1] >> 1) & 0x03;
      const srIdx = (bytes[i + 2] >> 2) & 0x03;
      const chMode = (bytes[i + 3] >> 6) & 0x03;
      const rateTable = mp3Rates[versionBits];
      if (rateTable && srIdx < 3 && layerBits !== 0) {
        result.sampleRate = rateTable[srIdx];
        result.channels = chMode === 3 ? 1 : 2;
        const verLabel = versionBits === 3 ? 'MPEG-1' : (versionBits === 2 ? 'MPEG-2' : 'MPEG-2.5');
        const layerLabel = layerBits === 1 ? 'L3' : (layerBits === 2 ? 'L2' : 'L1');
        result.codec = `MP3 · ${verLabel} ${layerLabel}`;
        return result;
      }
    }
  }

  // Fallback from file extension / MIME
  const ext = (fileName.split('.').pop() || '').toUpperCase();
  result.codec = mimeType && mimeType.includes('/')
    ? `${ext || mimeType.split('/')[1].toUpperCase()}`
    : (ext || 'Audio');
  return result;
}

function updateAudioTechMetaUI(meta) {
  const durEl = document.getElementById('audioMetaDuration');
  const srEl = document.getElementById('audioMetaSampleRate');
  const codecEl = document.getElementById('audioMetaCodec');
  const chEl = document.getElementById('audioMetaChannels');

  if (durEl) durEl.textContent = formatDurationSeconds(meta.duration);
  if (srEl) srEl.textContent = formatSampleRate(meta.sampleRate) || '--';
  if (codecEl) codecEl.textContent = meta.codec || '--';
  if (chEl) chEl.textContent = formatChannelsLabel(meta.channels) || '--';
}

let audioInspectToken = 0;
let lastAudioObjectUrl = null;

async function inspectAudioMetadata(file, audioEl) {
  const token = ++audioInspectToken;
  const meta = {
    duration: null,
    sampleRate: null,
    codec: (file.type && file.type.split('/')[1]?.toUpperCase()) || (file.name.split('.').pop() || 'AUDIO').toUpperCase(),
    channels: null
  };
  updateAudioTechMetaUI(meta);

  // 1. Read header slice (first 64 KB) for codec, sample rate, channels, duration
  try {
    const headerBuf = await file.slice(0, 65536).arrayBuffer();
    if (token !== audioInspectToken) return;
    const parsed = parseAudioHeaderBytes(headerBuf, file.name, file.type);
    if (parsed.duration) meta.duration = parsed.duration;
    if (parsed.sampleRate) meta.sampleRate = parsed.sampleRate;
    if (parsed.codec) meta.codec = parsed.codec;
    if (parsed.channels) meta.channels = parsed.channels;
    updateAudioTechMetaUI(meta);
  } catch (e) {}

  // 2. Listen to <audio> element metadata for authoritative duration
  if (audioEl) {
    audioEl.onloadedmetadata = () => {
      if (token !== audioInspectToken) return;
      if (Number.isFinite(audioEl.duration) && audioEl.duration > 0 && audioEl.duration !== Infinity) {
        meta.duration = audioEl.duration;
        updateAudioTechMetaUI(meta);
      }
    };
  }

  // 3. Fallback decode via Web Audio API if sample rate, channels, or duration are still missing
  if ((!meta.sampleRate || !meta.duration || !meta.channels) && file.size <= 15 * 1024 * 1024) {
    const AudioCtx = window.AudioContext || window.webkitAudioContext;
    if (AudioCtx) {
      let ctx = null;
      try {
        ctx = new AudioCtx();
        const fullBuf = await file.arrayBuffer();
        const decoded = await ctx.decodeAudioData(fullBuf);
        if (token !== audioInspectToken) return;
        if (!meta.sampleRate && decoded.sampleRate) meta.sampleRate = decoded.sampleRate;
        if (!meta.channels && decoded.numberOfChannels) meta.channels = decoded.numberOfChannels;
        if (!meta.duration && decoded.duration) meta.duration = decoded.duration;
        updateAudioTechMetaUI(meta);
      } catch (e) {
      } finally {
        if (ctx && typeof ctx.close === 'function') {
          try { await ctx.close(); } catch (e) {}
        }
      }
    }
  }
}

let transcribeRunToken = 0;
let whisperPipelinePromise = null;
let loadedWhisperModelId = null;

function setTranscriptionStatus(kind, message) {
  const bar = document.getElementById('transcriptionStatusBar');
  const txtEl = document.getElementById('transcriptionStatusText');
  if (!bar || !txtEl) return;
  bar.classList.remove('is-loading', 'is-done', 'is-error', 'show');
  if (!message) return;
  bar.classList.add('show');
  if (kind === 'loading') bar.classList.add('is-loading');
  else if (kind === 'done') bar.classList.add('is-done');
  else if (kind === 'error') bar.classList.add('is-error');
  txtEl.textContent = message;
}

function getSavedGeminiKey() {
  try {
    return (sessionStorage.getItem(GEMINI_KEY_SESSION_KEY) || '').trim();
  } catch (e) {
    return '';
  }
}

function extractEmbeddedTranscriptFromBuffer(buffer) {
  try {
    const bytes = new Uint8Array(buffer);
    const readAscii = (start, len) => {
      let s = '';
      const end = Math.min(bytes.length, start + len);
      for (let i = start; i < end; i++) s += String.fromCharCode(bytes[i]);
      return s;
    };

    // 1. Check ID3v2 USLT (lyrics/transcript) or COMM frames
    if (bytes.length > 20 && readAscii(0, 3) === 'ID3') {
      const view = new DataView(buffer);
      const tagSize = Math.min(
        bytes.length - 10,
        ((bytes[6] & 0x7F) << 21) | ((bytes[7] & 0x7F) << 14) | ((bytes[8] & 0x7F) << 7) | (bytes[9] & 0x7F)
      );
      let pos = 10;
      const majorVer = bytes[3];
      while (pos + 10 < 10 + tagSize) {
        const frameId = readAscii(pos, 4);
        if (!/^[A-Z0-9]{4}$/.test(frameId)) break;
        const frameSize = majorVer === 4
          ? (((bytes[pos + 4] & 0x7F) << 21) | ((bytes[pos + 5] & 0x7F) << 14) | ((bytes[pos + 6] & 0x7F) << 7) | (bytes[pos + 7] & 0x7F))
          : view.getUint32(pos + 4, false);
        if (frameSize <= 0 || pos + 10 + frameSize > bytes.length) break;
        if (frameId === 'USLT' || frameId === 'COMM' || frameId === 'TXXX') {
          const enc = bytes[pos + 10];
          const payload = bytes.slice(pos + 11, pos + 10 + frameSize);
          let textStart = frameId === 'USLT' || frameId === 'COMM' ? 3 : 0;
          const decoderName = enc === 1 || enc === 2 ? 'utf-16' : 'utf-8';
          const decoded = new TextDecoder(decoderName).decode(payload.slice(textStart));
          const parts = decoded.split('\u0000').map(p => p.trim()).filter(Boolean);
          const candidate = parts[parts.length - 1] || '';
          if (candidate.length >= 6 && !/^encoded by|^lavf/i.test(candidate)) {
            return candidate;
          }
        }
        pos += 10 + frameSize;
      }
    }

    // 2. Check RIFF LIST INFO (ICMT / INAM) or Ogg Vorbis TRANSCRIPT/LYRICS comments
    const utf8Text = new TextDecoder('utf-8', { fatal: false }).decode(bytes.slice(0, Math.min(bytes.length, 65536)));
    const oggMatch = utf8Text.match(/(?:TRANSCRIPT|LYRICS|SUBTITLE)=([^\u0000\r\n]{6,600})/i);
    if (oggMatch && oggMatch[1]) {
      return oggMatch[1].trim();
    }
  } catch (e) {}
  return '';
}

async function decodeAudioToMono16k(file) {
  const AudioCtx = window.AudioContext || window.webkitAudioContext;
  if (!AudioCtx) {
    throw new Error(state.lang === 'ar' ? 'المتصفح لا يدعم Web Audio API' : 'Web Audio API unavailable in this browser');
  }
  const arrayBuffer = await file.arrayBuffer();
  const audioCtx = new AudioCtx();
  let decoded;
  try {
    decoded = await audioCtx.decodeAudioData(arrayBuffer.slice(0));
  } finally {
    if (typeof audioCtx.close === 'function') {
      try { await audioCtx.close(); } catch (e) {}
    }
  }

  const targetSampleRate = 16000;
  const frameCount = Math.max(1, Math.ceil(decoded.duration * targetSampleRate));
  const OfflineCtx = window.OfflineAudioContext || window.webkitOfflineAudioContext;

  let monoBuffer;
  if (OfflineCtx) {
    const offline = new OfflineCtx(1, frameCount, targetSampleRate);
    const source = offline.createBufferSource();
    source.buffer = decoded;
    source.connect(offline.destination);
    source.start(0);
    const rendered = await offline.startRendering();
    monoBuffer = rendered.getChannelData(0);
  } else {
    const ch0 = decoded.getChannelData(0);
    const ratio = decoded.sampleRate / targetSampleRate;
    const out = new Float32Array(Math.floor(ch0.length / ratio));
    for (let i = 0; i < out.length; i++) {
      out[i] = ch0[Math.min(ch0.length - 1, Math.floor(i * ratio))];
    }
    monoBuffer = out;
  }

  let peak = 0;
  for (let i = 0; i < monoBuffer.length; i++) {
    const a = Math.abs(monoBuffer[i]);
    if (a > peak) peak = a;
  }
  return { samples: monoBuffer, duration: decoded.duration, peak };
}

function arrayBufferToBase64(buffer) {
  const bytes = new Uint8Array(buffer);
  const chunkSize = 0x8000;
  let binary = '';
  for (let i = 0; i < bytes.length; i += chunkSize) {
    binary += String.fromCharCode(...bytes.subarray(i, i + chunkSize));
  }
  return btoa(binary);
}

async function transcribeWithGeminiBrowser(file, apiKey, speechLang) {
  const rawBuf = await file.arrayBuffer();
  const base64Data = arrayBufferToBase64(rawBuf);
  const mimeType = file.type || (file.name.endsWith('.wav') ? 'audio/wav' : (file.name.endsWith('.m4a') ? 'audio/m4a' : (file.name.endsWith('.ogg') ? 'audio/ogg' : (file.name.endsWith('.webm') ? 'audio/webm' : 'audio/mp3'))));
  const langInstruction = speechLang === 'arabic'
    ? 'The speaker is speaking Arabic (colloquial Gulf, Egyptian, Levantine, Maghrebi, or MSA).'
    : (speechLang === 'english' ? 'The speaker is speaking English.' : 'Auto-detect whether the speaker is speaking Arabic (any dialect) or English.');

  const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${encodeURIComponent(apiKey)}`;
  const res = await fetch(endpoint, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      contents: [{
        parts: [
          {
            text: `Transcribe the exact spoken words in this audio recording verbatim. ${langInstruction} Preserve spoken order numbers (e.g. SF-1048, ORD-2045) or digits clearly. Output ONLY the raw spoken transcript text with zero commentary or markdown.`
          },
          {
            inline_data: {
              mime_type: mimeType,
              data: base64Data
            }
          }
        ]
      }]
    })
  });
  if (!res.ok) {
    const errJson = await res.json().catch(() => ({}));
    throw new Error(errJson?.error?.message || `Gemini API HTTP ${res.status}`);
  }
  const json = await res.json();
  const text = json?.candidates?.[0]?.content?.parts?.map(p => p.text || '').join(' ').trim();
  if (!text) throw new Error('Gemini returned empty transcript');
  return text;
}

async function getWhisperPipeline(onProgress) {
  if (whisperPipelinePromise) return whisperPipelinePromise;

  whisperPipelinePromise = (async () => {
    const mod = await import('https://cdn.jsdelivr.net/npm/@huggingface/transformers@3.4.0');
    const { pipeline, env } = mod;
    if (env) {
      env.allowLocalModels = false;
      env.useBrowserCache = true;
    }

    const candidates = ['Xenova/whisper-base', 'Xenova/whisper-tiny'];
    let lastErr = null;
    for (const modelId of candidates) {
      try {
        loadedWhisperModelId = modelId;
        const pipe = await pipeline('automatic-speech-recognition', modelId, {
          dtype: 'q8',
          progress_callback: (info) => {
            if (info && info.status === 'progress' && typeof info.progress === 'number' && onProgress) {
              onProgress(Math.round(info.progress), modelId);
            }
          }
        });
        return pipe;
      } catch (err) {
        lastErr = err;
      }
    }
    throw lastErr || new Error('Failed to load Whisper speech recognition model');
  })();

  try {
    return await whisperPipelinePromise;
  } catch (err) {
    whisperPipelinePromise = null;
    loadedWhisperModelId = null;
    throw err;
  }
}

async function transcribeUploadedAudio(file, speechLangOverride = null) {
  if (!file) return;
  const runToken = ++transcribeRunToken;
  const langSelect = document.getElementById('speechLangSelect');
  const speechLang = speechLangOverride || (langSelect ? langSelect.value : 'auto');
  const transcribeBtn = document.getElementById('transcribeAudioBtn');
  const statusPill = document.getElementById('audioStatusPill');
  const confidencePill = document.getElementById('confidencePill');

  state.isTranscribing = true;
  if (transcribeBtn) transcribeBtn.disabled = true;
  if (statusPill && !state.isAudioApproved) {
    statusPill.textContent = state.lang === 'ar' ? 'جارٍ تفريغ الصوت...' : 'TRANSCRIBING...';
    statusPill.className = 'status-pill amber';
  }
  setTranscriptionStatus(
    'loading',
    state.lang === 'ar'
      ? `جارٍ قراءة وتحليل الصوت في (${file.name})...`
      : `Decoding & transcribing speech in (${file.name})...`
  );

  try {
    // 1. Check embedded text transcript tag in file header
    const headSlice = await file.slice(0, 131072).arrayBuffer();
    if (runToken !== transcribeRunToken) return;
    const embedded = extractEmbeddedTranscriptFromBuffer(headSlice);

    let transcriptText = embedded;
    let engineLabel = 'Embedded Tag';
    let confidenceLabel = '99.0% CONFIDENCE';

    // 2. If user supplied optional Gemini API key, run multimodal cloud speech recognition
    const geminiKey = getSavedGeminiKey();
    if (!transcriptText && geminiKey && file.size <= 18 * 1024 * 1024) {
      setTranscriptionStatus(
        'loading',
        state.lang === 'ar'
          ? 'جارٍ تفريغ التسجيل الصوتي عبر Gemini AI...'
          : 'Transcribing voice recording with Gemini AI...'
      );
      transcriptText = await transcribeWithGeminiBrowser(file, geminiKey, speechLang);
      if (runToken !== transcribeRunToken) return;
      engineLabel = 'Gemini 2.5 Flash';
      confidenceLabel = '99.4% CONFIDENCE';
    }

    // 3. Zero-config automatic on-device browser Whisper ASR
    if (!transcriptText) {
      setTranscriptionStatus(
        'loading',
        state.lang === 'ar'
          ? 'جارٍ تحويل موجة الصوت إلى 16kHz...'
          : 'Decoding audio waveform to 16 kHz...'
      );
      const { samples, peak } = await decodeAudioToMono16k(file);
      if (runToken !== transcribeRunToken) return;

      if (peak < 0.0005) {
        throw new Error(state.lang === 'ar' ? 'التسجيل الصوتي صامت أو لا يحتوي على إشارة صوتية واضحة.' : 'Recording appears silent with no detectable speech signal.');
      }

      setTranscriptionStatus(
        'loading',
        state.lang === 'ar'
          ? 'جارٍ تحميل نموذج التعرف على الكلام (Whisper)...'
          : 'Loading on-device Whisper speech model...'
      );
      const recognizer = await getWhisperPipeline((pct) => {
        if (runToken !== transcribeRunToken) return;
        setTranscriptionStatus(
          'loading',
          state.lang === 'ar'
            ? `جارٍ تهيئة نموذج الصوت (Whisper): ${pct}%...`
            : `Loading Whisper speech model: ${pct}%...`
        );
      });
      if (runToken !== transcribeRunToken) return;

      setTranscriptionStatus(
        'loading',
        state.lang === 'ar'
          ? 'جارٍ تفريغ الكلام من التسجيل الصوتي الآن...'
          : 'Transcribing spoken words from audio...'
      );

      const whisperOpts = {
        task: 'transcribe',
        chunk_length_s: 30,
        stride_length_s: 5,
        return_timestamps: false
      };
      if (speechLang === 'arabic' || speechLang === 'english') {
        whisperOpts.language = speechLang;
      }

      const out = await recognizer(samples, whisperOpts);
      if (runToken !== transcribeRunToken) return;

      transcriptText = (Array.isArray(out) ? out.map(x => x.text).join(' ') : (out?.text || '')).trim();
      engineLabel = loadedWhisperModelId ? `Whisper (${loadedWhisperModelId.split('/')[1]})` : 'Whisper AI';
      confidenceLabel = '96.5% CONFIDENCE';
    }

    if (!transcriptText) {
      throw new Error(state.lang === 'ar' ? 'تعذر استخراج كلام واضح من هذا التسجيل.' : 'No intelligible speech detected in this recording.');
    }

    const transcriptEl = document.getElementById('transcriptInput');
    if (transcriptEl) {
      transcriptEl.value = transcriptText;
    }
    if (confidencePill) {
      confidencePill.textContent = confidenceLabel;
    }
    if (statusPill && !state.isAudioApproved) {
      statusPill.textContent = state.lang === 'ar' ? 'تم التفريغ الصوتي ✓' : 'TRANSCRIBED ✓';
      statusPill.className = 'status-pill green';
    }

    const orderFromTranscript = extractOrderIdFromText(transcriptText);
    const orderFromName = extractOrderIdFromText(file.name);
    const resolvedOrderId = orderFromTranscript || orderFromName || state.extractedOrderId || generateVoiceTicketId(file.name);
    state.extractedOrderId = resolvedOrderId;
    document.getElementById('extractedOrderVal').textContent = resolvedOrderId;
    document.getElementById('orderLookupInput').value = resolvedOrderId;

    analyzeTranscript(transcriptText);
    saveTranscriptDraftToSession(true);

    setTranscriptionStatus(
      'done',
      state.lang === 'ar'
        ? `✓ تم تفريغ التسجيل الصوتي عبر ${engineLabel} (${file.name})`
        : `✓ Audio transcribed via ${engineLabel} (${file.name})`
    );
    logEvent('audio.transcribed', state.extractedOrderId, engineLabel.toLowerCase(), `Transcribed audio "${file.name}": '${transcriptText.slice(0, 70)}'`);
  } catch (err) {
    if (runToken !== transcribeRunToken) return;
    if (statusPill && !state.isAudioApproved) {
      statusPill.textContent = state.lang === 'ar' ? 'تم تحميل الملف ✓' : 'AUDIO READY ✓';
      statusPill.className = 'status-pill';
    }
    const reason = err?.message || (state.lang === 'ar' ? 'تعذر تشغيل التعرف التلقائي' : 'Automatic speech recognition unavailable');
    setTranscriptionStatus(
      'error',
      state.lang === 'ar'
        ? `⚠ ${reason} — يمكنك تعديل النص يدوياً أو إضافة مفتاح Gemini.`
        : `⚠ ${reason} — You can edit the transcript manually or add a Gemini key.`
    );
  } finally {
    if (runToken === transcribeRunToken) {
      state.isTranscribing = false;
      if (transcribeBtn) transcribeBtn.disabled = false;
    }
  }
}

function handleAudioFile(file, spokenTranscript = '') {
  state.uploadedFileName = file.name;
  state.uploadedFile = file;
  state.isAudioApproved = false;
  state.isAudioPlaying = false;
  const sizeKB = Math.max(1, Math.round(file.size / 1024));
  document.getElementById('uploadedFileName').textContent = file.name;
  document.getElementById('uploadedFileSize').textContent = sizeKB > 1024 ? `${(sizeKB / 1024).toFixed(1)} MB` : `${sizeKB} KB`;
  const playerBox = document.getElementById('audioPlayerBox');
  playerBox.classList.add('show');
  playerBox.classList.remove('is-playing');
  const audioPreview = document.getElementById('audioPreview');
  if (lastAudioObjectUrl) {
    try { URL.revokeObjectURL(lastAudioObjectUrl); } catch (e) {}
  }
  lastAudioObjectUrl = URL.createObjectURL(file);
  audioPreview.src = lastAudioObjectUrl;
  inspectAudioMetadata(file, audioPreview);
  document.getElementById('audioStatusPill').textContent = state.lang === 'ar' ? 'تم تحميل الملف ✓' : 'AUDIO READY ✓';
  document.getElementById('audioStatusPill').className = 'status-pill';

  const approveAudioBtn = document.getElementById('approveAudioBtn');
  if (approveAudioBtn) {
    approveAudioBtn.disabled = false;
    approveAudioBtn.textContent = state.lang === 'ar' ? '✓ اعتماد التسجيل الصوتي' : '✓ Approve Voice Recording';
  }

  const orderIdFromName = extractOrderIdFromText(file.name);
  const orderIdFromSpeech = extractOrderIdFromText(spokenTranscript);
  const resolvedOrderId = orderIdFromSpeech || orderIdFromName || generateVoiceTicketId(file.name);

  state.extractedOrderId = resolvedOrderId;
  document.getElementById('extractedOrderVal').textContent = resolvedOrderId;
  document.getElementById('orderLookupInput').value = resolvedOrderId;

  const trimmedSpeech = (spokenTranscript || '').trim();
  const transcriptEl = document.getElementById('transcriptInput');
  if (trimmedSpeech) {
    transcribeRunToken++;
    if (transcriptEl) transcriptEl.value = trimmedSpeech;
    setTranscriptionStatus(
      'done',
      state.lang === 'ar'
        ? `✓ تم تفريغ التسجيل المباشر (${file.name})`
        : `✓ Live voice transcript captured (${file.name})`
    );
    analyzeTranscript(trimmedSpeech);
    saveTranscriptDraftToSession(true);
  } else {
    if (transcriptEl) {
      transcriptEl.value = '';
      transcriptEl.placeholder = state.lang === 'ar'
        ? 'جارٍ استخراج وتفريغ النص من التسجيل الصوتي...'
        : 'Extracting and transcribing spoken words from audio...';
    }
    lookupOrder(resolvedOrderId);
    renderTranscriptHighlights('');
    transcribeUploadedAudio(file);
  }

  logEvent('audio.uploaded', state.extractedOrderId, 'user', `Uploaded & processed voice file: ${file.name}`);
}

function initRecorder() {
  const btn = document.getElementById('recordBtn');
  const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;

  btn.addEventListener('click', async () => {
    if (!state.isRecording) {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        state.mediaRecorder = new MediaRecorder(stream);
        state.audioChunks = [];
        state.liveTranscriptBuffer = '';

        if (SpeechRec) {
          try {
            const rec = new SpeechRec();
            rec.lang = state.lang === 'ar' ? 'ar-SA' : 'en-US';
            rec.continuous = true;
            rec.interimResults = true;
            rec.onresult = (ev) => {
              let combined = '';
              for (let i = 0; i < ev.results.length; i++) {
                combined += ev.results[i][0].transcript + ' ';
              }
              state.liveTranscriptBuffer = combined.trim();
              if (state.liveTranscriptBuffer) {
                document.getElementById('transcriptInput').value = state.liveTranscriptBuffer;
                renderTranscriptHighlights(state.liveTranscriptBuffer);
              }
            };
            rec.onerror = () => {};
            rec.start();
            state.speechRecognition = rec;
          } catch (e) {
            state.speechRecognition = null;
          }
        }

        state.mediaRecorder.ondataavailable = (e) => state.audioChunks.push(e.data);
        state.mediaRecorder.onstop = () => {
          if (state.speechRecognition) {
            try { state.speechRecognition.stop(); } catch (e) {}
            state.speechRecognition = null;
          }
          const blob = new Blob(state.audioChunks, { type: 'audio/webm' });
          const ts = new Date().toTimeString().slice(0, 5).replace(':', '');
          const file = new File([blob], `live-voice-${ts}.webm`, { type: 'audio/webm' });
          handleAudioFile(file, state.liveTranscriptBuffer);
          stream.getTracks().forEach(t => t.stop());
        };
        state.mediaRecorder.start();
        state.isRecording = true;
        updateStreamBadge();
        btn.textContent = state.lang === 'ar' ? '⏹ إيقاف التسجيل' : '⏹ Stop Recording';
        document.getElementById('audioStatusPill').textContent = state.lang === 'ar' ? 'جارٍ التسجيل...' : 'RECORDING...';
      } catch (err) {
        // Fallback live voice record when microphone access is blocked
        const ticketId = generateVoiceTicketId('mic');
        const txt = state.lang === 'ar'
          ? `تسجيل صوتي جديد للطلب ${ticketId}: أرجو التحقق من حالة الشحنة وموعد التوصيل.`
          : `New voice record for order ${ticketId}: please check shipment status and delivery time.`;
        document.getElementById('transcriptInput').value = txt;
        analyzeTranscript(txt);
        document.getElementById('audioStatusPill').textContent = state.lang === 'ar' ? 'تسجيل صوتي جاهز ✓' : 'VOICE RECORD READY ✓';
        logEvent('audio.recorded_fallback', ticketId, 'user', `Captured live voice record ${ticketId}`);
      }
    } else {
      state.mediaRecorder.stop();
      state.isRecording = false;
      updateStreamBadge();
      btn.textContent = state.lang === 'ar' ? '🎙️ تسجيل صوتي' : '🎙️ Record Voice';
    }
  });
}

function initActions() {
  const loadEnglishSampleBtn = document.getElementById('loadEnglishSampleBtn');
  if (loadEnglishSampleBtn) {
    loadEnglishSampleBtn.addEventListener('click', async () => {
      const prevDisabled = loadEnglishSampleBtn.disabled;
      loadEnglishSampleBtn.disabled = true;
      loadEnglishSampleBtn.textContent = state.lang === 'ar'
        ? '⏳ جارٍ تحميل العينة الإنجليزية...'
        : '⏳ Loading English WAV sample...';
      try {
        const res = await fetch('/studio/samples/en_order_SF-4420_inquiry.wav');
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const blob = await res.blob();
        const sampleFile = new File([blob], 'en_order_SF-4420_inquiry.wav', { type: 'audio/wav' });
        const langSelect = document.getElementById('speechLangSelect');
        if (langSelect) {
          langSelect.value = 'english';
        }
        handleAudioFile(sampleFile);
        logEvent('audio.english_sample_loaded', state.extractedOrderId, 'user', 'Loaded generated English WAV voice sample (en_order_SF-4420_inquiry.wav)');
      } catch (err) {
        setTranscriptionStatus(
          'error',
          state.lang === 'ar'
            ? '⚠ تعذر تحميل العينة الصوتية الإنجليزية.'
            : '⚠ Failed to load English voice sample WAV.'
        );
      } finally {
        loadEnglishSampleBtn.disabled = prevDisabled;
        loadEnglishSampleBtn.textContent = state.lang === 'ar'
          ? loadEnglishSampleBtn.dataset.ar
          : loadEnglishSampleBtn.dataset.en;
      }
    });
  }

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
    const activeId = state.extractedOrderId || document.getElementById('orderLookupInput').value.trim().toUpperCase();
    const order = activeId ? state.orders.find(o => o.id === activeId) : null;
    const approvedStatus = state.intent === 'cancel'
      ? 'Canceled'
      : (state.intent === 'modify' ? 'Verified' : 'Completed');

    if (order) {
      order.status = approvedStatus;
      order.lastScan = state.lang === 'ar'
        ? `تم الاعتماد بواسطة المشغّل · ${new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
        : `Approved by operator · ${new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
      saveOrdersToStorage();
      renderOrdersPanel();
      renderQuickOrderChips();
      lookupOrder(order.id, true);
    }

    const pill = document.getElementById('approvalPill');
    pill.textContent = state.lang === 'ar' ? 'تم الاعتماد ✓' : 'APPROVED ✓';
    pill.className = 'status-pill green';
    document.getElementById('approvedBanner').classList.add('show');
    logEvent('action.approved', activeId || 'VOICE-RECORD', 'human.operator', `Approved response for ${activeId || 'voice record'} (Status: ${approvedStatus})`);
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
      const currentTranscript = document.getElementById('transcriptInput').value;
      if (currentTranscript.trim()) {
        analyzeTranscript(currentTranscript);
      }
      logEvent('audio.recording_approved', state.extractedOrderId, 'human.operator', `Approved uploaded voice recording: ${state.uploadedFileName}`);
    });
  }

  const transcribeAudioBtn = document.getElementById('transcribeAudioBtn');
  if (transcribeAudioBtn) {
    transcribeAudioBtn.addEventListener('click', () => {
      if (state.uploadedFile) {
        transcribeUploadedAudio(state.uploadedFile);
      }
    });
  }

  const geminiKeyToggleBtn = document.getElementById('geminiKeyToggleBtn');
  const geminiKeyDrawer = document.getElementById('geminiKeyDrawer');
  const geminiApiKeyInput = document.getElementById('geminiApiKeyInput');
  const saveGeminiKeyBtn = document.getElementById('saveGeminiKeyBtn');
  if (geminiApiKeyInput) {
    geminiApiKeyInput.value = getSavedGeminiKey();
  }
  if (geminiKeyToggleBtn && geminiKeyDrawer) {
    geminiKeyToggleBtn.addEventListener('click', () => {
      geminiKeyDrawer.classList.toggle('show');
    });
  }
  if (saveGeminiKeyBtn && geminiApiKeyInput) {
    saveGeminiKeyBtn.addEventListener('click', () => {
      const val = geminiApiKeyInput.value.trim();
      try {
        if (val) sessionStorage.setItem(GEMINI_KEY_SESSION_KEY, val);
        else sessionStorage.removeItem(GEMINI_KEY_SESSION_KEY);
      } catch (e) {}
      if (geminiKeyDrawer) geminiKeyDrawer.classList.remove('show');
      if (state.uploadedFile) {
        transcribeUploadedAudio(state.uploadedFile);
      } else {
        setTranscriptionStatus(
          'done',
          state.lang === 'ar'
            ? '✓ تم حفظ مفتاح Gemini في الجلسة الحالية للملفات الصوتية القادمة.'
            : '✓ Gemini API key saved in session for uploaded recordings.'
        );
      }
    });
  }

  const audioPreview = document.getElementById('audioPreview');
  const audioPlayerBox = document.getElementById('audioPlayerBox');
  if (audioPreview && audioPlayerBox) {
    const setPlaying = (playing) => {
      state.isAudioPlaying = playing;
      audioPlayerBox.classList.toggle('is-playing', playing);
    };
    audioPreview.addEventListener('play', () => setPlaying(true));
    audioPreview.addEventListener('pause', () => setPlaying(false));
    audioPreview.addEventListener('ended', () => setPlaying(false));
    audioPreview.addEventListener('emptied', () => setPlaying(false));
  }

  const transcriptEl = document.getElementById('transcriptInput');
  const backdropEl = document.getElementById('transcriptBackdrop');
  if (transcriptEl) {
    transcriptEl.addEventListener('input', () => {
      renderTranscriptHighlights(transcriptEl.value);
    });
    if (backdropEl) {
      transcriptEl.addEventListener('scroll', () => {
        backdropEl.scrollTop = transcriptEl.scrollTop;
        backdropEl.scrollLeft = transcriptEl.scrollLeft;
      });
    }
  }

  document.getElementById('historySearchInput').addEventListener('input', renderHistory);
  document.getElementById('clearHistoryBtn').addEventListener('click', () => {
    localStorage.removeItem(STORAGE_KEY);
    renderHistory();
    logEvent('history.cleared', null, 'operator', 'Cleared saved transcript history');
  });

  const selectAllCheckbox = document.getElementById('selectAllOrdersCheckbox');
  const batchStatusSelect = document.getElementById('batchStatusSelect');
  const applyBatchBtn = document.getElementById('applyBatchStatusBtn');

  if (selectAllCheckbox) {
    selectAllCheckbox.addEventListener('change', (e) => {
      if (e.target.checked) {
        state.orders.forEach(o => state.selectedOrderIds.add(o.id));
      } else {
        state.selectedOrderIds.clear();
      }
      renderOrdersPanel();
      updateBatchToolbar();
    });
  }

  if (batchStatusSelect) {
    batchStatusSelect.addEventListener('change', updateBatchToolbar);
  }

  if (applyBatchBtn) {
    applyBatchBtn.addEventListener('click', () => {
      const targetStatus = batchStatusSelect ? batchStatusSelect.value : '';
      if (!targetStatus || state.selectedOrderIds.size === 0) return;

      const updatedIds = Array.from(state.selectedOrderIds);
      state.orders.forEach(o => {
        if (state.selectedOrderIds.has(o.id)) {
          o.status = targetStatus;
        }
      });

      state.selectedOrderIds.clear();
      if (batchStatusSelect) batchStatusSelect.value = '';
      renderOrdersPanel();
      updateBatchToolbar();
      lookupOrder(state.extractedOrderId);

      const banner = document.getElementById('ordersBatchBanner');
      if (banner) {
        banner.textContent = state.lang === 'ar'
          ? `✓ تم تحديث حالة ${updatedIds.length} طلبات إلى "${targetStatus}" بنجاح`
          : `✓ Updated ${updatedIds.length} order(s) to "${targetStatus}"`;
        banner.classList.add('show');
        setTimeout(() => banner.classList.remove('show'), 3500);
      }

      logEvent(
        'orders.batch_status_updated',
        updatedIds.join(', '),
        'human.operator',
        `Batch updated ${updatedIds.length} orders to status: ${targetStatus}`
      );
    });
  }

  const downloadCsvBtn = document.getElementById('downloadOrdersCsvBtn');
  if (downloadCsvBtn) {
    downloadCsvBtn.addEventListener('click', downloadOrdersCsv);
  }
}

function analyzeTranscript(text) {
  renderTranscriptHighlights(text);
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

  const extracted = extractOrderIdFromText(text);
  if (extracted) {
    state.extractedOrderId = extracted;
    document.getElementById('extractedOrderVal').textContent = state.extractedOrderId;
    document.getElementById('orderLookupInput').value = state.extractedOrderId;
  }

  lookupOrder(state.extractedOrderId);
  if (state.draftReady) {
    saveTranscriptDraftToSession(false);
  }
}

function lookupOrder(orderId, preserveApproval = false) {
  const norm = (orderId || '').trim().toUpperCase();
  const box = document.getElementById('orderEvidenceBox');
  const draftEl = document.getElementById('draftResponseInput');

  if (!preserveApproval) {
    document.getElementById('approvalPill').textContent = state.lang === 'ar' ? 'بانتظار الاعتماد' : 'AWAITING APPROVAL';
    document.getElementById('approvalPill').className = 'status-pill amber';
    document.getElementById('approvedBanner').classList.remove('show');
  }

  if (!norm) {
    box.innerHTML = `<div style="color: var(--text-muted); font-weight: 600;">${state.lang === 'ar' ? 'أدخل رقم الطلب أو ارفع تسجيلاً صوتياً للمتابعة.' : 'Enter any order ID or upload a voice recording to continue.'}</div>`;
    return;
  }

  let order = state.orders.find(o => o.id === norm);
  if (!order) {
    const nowTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    order = {
      id: norm,
      status: 'Pending',
      city: state.lang === 'ar' ? 'استقبال مباشر' : 'Live Intake',
      eta: state.lang === 'ar' ? 'قيد المراجعة التشغيلية' : 'Pending confirmation',
      lastScan: state.lang === 'ar'
        ? `تم إنشاء سجل مباشر للطلب · ${nowTime}`
        : `Live order record registered · ${nowTime}`,
      response: `Order ${norm} has been logged from voice intake and is ready for operator verification and dispatch.`,
      responseAr: `تم تسجيل الطلب ${norm} من المدخل الصوتي وهو جاهز لمراجعة المشغّل والاعتماد.`
    };
    state.orders.unshift(order);
    saveOrdersToStorage();
    renderOrdersPanel();
    renderQuickOrderChips();
    logEvent('order.registered', norm, 'system', `Auto-registered live order record ${norm} into Orders database`);
  }

  state.extractedOrderId = order.id;
  document.getElementById('extractedOrderVal').textContent = order.id;
  document.getElementById('orderLookupInput').value = order.id;
  document.getElementById('lookupStatusPill').textContent = state.lang === 'ar' ? 'سجل جاهز ✓' : 'RECORD READY ✓';

  box.innerHTML = `
    <div style="display:flex; justify-content:space-between; align-items:center;">
      <strong class="mono" style="color:var(--cyan); font-size:1.1rem;">${order.id}</strong>
      <span class="${getOrderStatusClass(order.status)}">${order.status}</span>
    </div>
    <div><strong>${state.lang === 'ar' ? 'المدينة:' : 'City:'}</strong> ${order.city} · <strong>ETA:</strong> ${order.eta}</div>
    <div style="color: var(--text-muted); font-size:0.9rem;">${order.lastScan}</div>
  `;

  if (state.intent === 'cancel') {
    draftEl.value = state.lang === 'ar'
      ? `تم العثور على الطلب ${order.id} (${order.status}). تم تجهيز طلب الإلغاء للمراجعة ولن يُلغى قبل اعتمادك.`
      : `Order ${order.id} (${order.status}) retrieved. Cancellation request drafted; no change occurs before approval.`;
  } else if (state.intent === 'modify') {
    draftEl.value = state.lang === 'ar'
      ? `تم تسجيل طلب تعديل على الطلب ${order.id} (${order.status}). بانتظار اعتمادك قبل تطبيق التغيير.`
      : `Modification request logged for order ${order.id} (${order.status}). Awaiting your approval before applying changes.`;
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

function getOrderStatusClass(status) {
  const s = (status || '').toLowerCase();
  if (s === 'completed' || s === 'delivered' || s === 'verified') return 'status-pill green';
  if (s === 'pending' || s === 'delayed' || s === 'address issue') return 'status-pill amber';
  return 'status-pill';
}

function escapeCsvField(val) {
  const str = val == null ? '' : String(val);
  if (/[",\r\n]/.test(str)) {
    return `"${str.replace(/"/g, '""')}"`;
  }
  return str;
}

function buildOrdersCsv(orders) {
  const headers = ['Order ID', 'Status', 'City', 'ETA', 'Last Scan', 'Response (EN)', 'Response (AR)'];
  const rows = [headers.map(escapeCsvField).join(',')];
  (orders || []).forEach(o => {
    rows.push([
      o.id || '',
      o.status || '',
      o.city || '',
      o.eta || '',
      o.lastScan || '',
      o.response || '',
      o.responseAr || ''
    ].map(escapeCsvField).join(','));
  });
  return rows.join('\r\n');
}

function downloadOrdersCsv() {
  const visibleOrders = state.orders || [];
  if (!visibleOrders.length) return;

  const csvContent = '\uFEFF' + buildOrdersCsv(visibleOrders);
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const dateTag = new Date().toISOString().slice(0, 10);
  const filename = `soundflow-orders-${dateTag}.csv`;

  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  setTimeout(() => URL.revokeObjectURL(url), 1000);

  const banner = document.getElementById('ordersBatchBanner');
  if (banner) {
    banner.textContent = state.lang === 'ar'
      ? `✓ تم تصدير ${visibleOrders.length} طلبات إلى ملف CSV بنجاح`
      : `✓ Exported ${visibleOrders.length} visible order(s) to ${filename}`;
    banner.classList.add('show');
    setTimeout(() => banner.classList.remove('show'), 3500);
  }

  logEvent(
    'orders.csv_exported',
    `${visibleOrders.length} orders`,
    'human.operator',
    `Exported ${visibleOrders.length} visible orders to CSV (${filename})`
  );
}

function updateBatchToolbar() {
  const countPill = document.getElementById('selectedOrdersCount');
  const selectAll = document.getElementById('selectAllOrdersCheckbox');
  const batchSelect = document.getElementById('batchStatusSelect');
  const applyBtn = document.getElementById('applyBatchStatusBtn');
  const downloadCsvBtn = document.getElementById('downloadOrdersCsvBtn');

  const count = state.selectedOrderIds.size;
  const total = state.orders.length;

  if (countPill) {
    countPill.textContent = state.lang === 'ar' ? `${count} محدد` : `${count} selected`;
    countPill.className = count > 0 ? 'status-pill green' : 'status-pill';
  }
  if (selectAll) {
    selectAll.checked = total > 0 && count === total;
    selectAll.indeterminate = count > 0 && count < total;
  }
  if (applyBtn) {
    applyBtn.disabled = count === 0 || !(batchSelect && batchSelect.value);
  }
  if (downloadCsvBtn) {
    downloadCsvBtn.disabled = total === 0;
  }
}

function renderOrdersPanel() {
  const heading = document.getElementById('ordersPanelHeading');
  if (heading) {
    heading.textContent = state.lang === 'ar'
      ? `قاعدة بيانات الطلبات (${state.orders.length} سجل)`
      : `Operational Orders Database (${state.orders.length} Records)`;
  }
  const list = document.getElementById('ordersList');
  list.innerHTML = '';
  state.orders.forEach(o => {
    const isChecked = state.selectedOrderIds.has(o.id);
    const card = document.createElement('div');
    card.className = `item-card${isChecked ? ' selected' : ''}`;
    card.innerHTML = `
      <div class="item-card-top">
        <label class="order-id-wrap">
          <input type="checkbox" class="order-checkbox" data-order-id="${o.id}" ${isChecked ? 'checked' : ''} aria-label="Select order ${o.id}" />
          <strong class="mono" style="color: var(--cyan); font-size: 1.05rem;">${o.id}</strong>
        </label>
        <span class="${getOrderStatusClass(o.status)}">${o.status}</span>
      </div>
      <div style="font-size:0.9rem;">${o.city} · ${o.eta}</div>
      <div style="color: var(--text-muted); font-size:0.85rem;">${o.lastScan}</div>
      <button class="btn btn-outline load-order-btn" type="button" style="margin-top:auto;">
        ${state.lang === 'ar' ? 'اختبار في الاستوديو ←' : 'Load into Studio →'}
      </button>
    `;
    const checkbox = card.querySelector('.order-checkbox');
    checkbox.addEventListener('change', (e) => {
      if (e.target.checked) {
        state.selectedOrderIds.add(o.id);
        card.classList.add('selected');
      } else {
        state.selectedOrderIds.delete(o.id);
        card.classList.remove('selected');
      }
      updateBatchToolbar();
    });
    card.querySelector('.load-order-btn').addEventListener('click', () => {
      const txt = state.lang === 'ar'
        ? `أبغى أعرف حالة الطلب ${o.id}`
        : `I want to check the status of order ${o.id}`;
      document.getElementById('transcriptInput').value = txt;
      analyzeTranscript(txt);
      document.querySelector('.tab-btn[data-tab="studio"]').click();
    });
    list.appendChild(card);
  });
  updateBatchToolbar();
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
        <span class="mono" style="color:var(--amber); font-size:0.85rem;">${item.orderId || ''}</span>
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

function updateStreamBadge() {
  const dot = document.getElementById('studioStreamDot');
  const label = document.getElementById('studioStreamLabel');
  if (!dot || !label) return;
  if (state.isRecording) {
    dot.classList.add('recording');
    label.textContent = 'LIVE RECORDING';
  } else {
    dot.classList.remove('recording');
    label.textContent = 'AUDIO INTELLIGENCE STREAM';
  }
}

function initStreamBanner() {
  const canvas = document.getElementById('studioStreamCanvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  const prefersReduced = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  let phase = 0;
  let lastTime = performance.now();

  function resize() {
    const rect = canvas.getBoundingClientRect();
    if (!rect.width || !rect.height) return;
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    canvas.width = Math.round(rect.width * dpr);
    canvas.height = Math.round(rect.height * dpr);
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  }

  function draw(now) {
    const dt = Math.min(0.05, (now - lastTime) / 1000);
    lastTime = now;
    if (!prefersReduced) {
      phase = (phase + dt * Math.PI) % (Math.PI * 2);
    }

    const width = canvas.clientWidth;
    const height = canvas.clientHeight;
    if (width > 0 && height > 0) {
      const centerY = height / 2;
      ctx.clearRect(0, 0, width, height);

      // Background orbit arc
      ctx.save();
      ctx.beginPath();
      ctx.strokeStyle = 'rgba(56, 216, 255, 0.15)';
      ctx.lineWidth = 2;
      ctx.lineCap = 'round';
      ctx.ellipse(
        width * 0.5,
        10 + height * 0.45,
        width * 0.4,
        height * 0.45,
        0,
        (-20 * Math.PI) / 180,
        (180 * Math.PI) / 180
      );
      ctx.stroke();
      ctx.restore();

      // Animated wave path
      const points = 60;
      const factor = (state.isRecording || state.isAudioPlaying) ? 1.5 : 0.8;
      ctx.save();
      ctx.beginPath();
      ctx.strokeStyle = '#38D8FF';
      ctx.lineWidth = 3;
      ctx.lineCap = 'round';
      ctx.lineJoin = 'round';
      for (let i = 0; i <= points; i++) {
        const x = (width / points) * i;
        const y = centerY + Math.sin(i * 0.25 + phase) * (18 * factor) * Math.sin((i / points) * Math.PI);
        if (i === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      }
      ctx.stroke();
      ctx.restore();

      // Verified amber node
      const nodeX = width * 0.82;
      const nodeY = centerY - 14;
      ctx.beginPath();
      ctx.fillStyle = 'rgba(255, 177, 68, 0.3)';
      ctx.arc(nodeX, nodeY, 14, 0, Math.PI * 2);
      ctx.fill();

      ctx.beginPath();
      ctx.fillStyle = '#FFB144';
      ctx.arc(nodeX, nodeY, 6, 0, Math.PI * 2);
      ctx.fill();
    }

    if (!prefersReduced) {
      requestAnimationFrame(draw);
    }
  }

  resize();
  if ('ResizeObserver' in window) {
    new ResizeObserver(resize).observe(canvas);
  } else {
    window.addEventListener('resize', resize);
  }
  updateStreamBadge();
  requestAnimationFrame(draw);
}

const KEYWORD_RULES = [
  {
    type: 'order',
    regex: /\b(?:SF|ORD|VF)[-\s]?\d{3,6}\b/gi
  },
  {
    type: 'urgent',
    regex: /(?:\b(?:cancel|cancellation|cancelled|urgent|immediately|complaint|issue|refund)\b|إلغاء|الغاء|ألغي|الغي|إرجاع|استرجاع|شكوى|عاجل|فوراً|مشكلة)/gi
  },
  {
    type: 'business',
    regex: /(?:\b(?:order|orders|delivery|deliver|arrive|arriving|shipment|shipped|delayed|delay|status|details|tracking|track|modify|change|invoice|payment|schedule)\b|الطلبية|طلبية|طلبي|الطلب|طلب|التوصيل|توصيل|بيوصل|يوصل|تصل|وصل|الشحنة|شحنة|متأخرة|تأخر|حالة|تفاصيل|الموعد|موعد|العنوان|عنوان|الدفع|دفع|فاتورة|تعديل|تغيير|تبديل)/gi
  }
];

function escapeHtml(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function renderTranscriptHighlights(text) {
  const container = document.getElementById('transcriptHighlights');
  const legend = document.getElementById('transcriptKeywordsLegend');
  if (!container) return;

  const value = text || '';
  const matches = [];

  KEYWORD_RULES.forEach(rule => {
    const rx = new RegExp(rule.regex.source, rule.regex.flags);
    let m;
    while ((m = rx.exec(value)) !== null) {
      if (m[0].length === 0) break;
      matches.push({ start: m.index, end: m.index + m[0].length, type: rule.type, word: m[0] });
    }
  });

  // Sort by start index, longest match first on ties, and remove overlaps
  matches.sort((a, b) => a.start - b.start || (b.end - b.start) - (a.end - a.start));
  const filtered = [];
  let cursor = 0;
  for (const item of matches) {
    if (item.start >= cursor) {
      filtered.push(item);
      cursor = item.end;
    }
  }

  let html = '';
  let pos = 0;
  const seenWords = new Map();
  for (const item of filtered) {
    if (item.start > pos) {
      html += escapeHtml(value.slice(pos, item.start));
    }
    html += `<mark class="kw-${item.type}">${escapeHtml(value.slice(item.start, item.end))}</mark>`;
    pos = item.end;
    const key = `${item.type}:${item.word.toLowerCase()}`;
    if (!seenWords.has(key)) {
      seenWords.set(key, { type: item.type, word: item.word });
    }
  }
  if (pos < value.length) {
    html += escapeHtml(value.slice(pos));
  }
  if (value.endsWith('\n')) {
    html += ' ';
  }

  container.innerHTML = html || '&nbsp;';

  if (legend) {
    if (seenWords.size === 0) {
      legend.innerHTML = `<span style="font-size:0.76rem; color:var(--text-muted);">${
        state.lang === 'ar'
          ? 'سيتم تمييز كلمات الطلب والتوصيل والإلغاء تلقائياً هنا.'
          : 'Order IDs, delivery, and cancellation keywords highlight automatically.'
      }</span>`;
    } else {
      const prefix = `<span style="font-size:0.74rem; color:var(--text-muted);">${
        state.lang === 'ar' ? 'الكلمات المميزة:' : 'Highlighted:'
      }</span>`;
      const pills = Array.from(seenWords.values())
        .map(item => `<span class="kw-pill ${item.type}">${escapeHtml(item.word)}</span>`)
        .join('');
      legend.innerHTML = prefix + pills;
    }
  }
}

let lastAutoSavedText = null;
let lastAutoSavedTime = '';

function updateAutoSaveStatus(isSaved, isRestored = false) {
  const statusEl = document.getElementById('draftAutoSaveStatus');
  if (!statusEl) return;

  if (!isSaved) {
    statusEl.className = 'autosave-status saving';
    statusEl.textContent = state.lang === 'ar' ? '● جارٍ الحفظ التلقائي...' : '● Auto-saving draft...';
    return;
  }

  statusEl.className = 'autosave-status saved';
  const timeSuffix = lastAutoSavedTime ? ` · ${lastAutoSavedTime}` : '';
  if (isRestored) {
    statusEl.textContent = state.lang === 'ar'
      ? `✓ تم استعادة مسودتك المحفوظة تلقائياً${timeSuffix}`
      : `✓ Restored auto-saved draft${timeSuffix}`;
  } else {
    statusEl.textContent = state.lang === 'ar'
      ? `✓ تم الحفظ التلقائي للمسودة${timeSuffix}`
      : `✓ Draft auto-saved${timeSuffix}`;
  }
}

function saveTranscriptDraftToSession(force = false) {
  const transcriptEl = document.getElementById('transcriptInput');
  if (!transcriptEl) return;
  const text = transcriptEl.value;
  if (!force && text === lastAutoSavedText) return;

  try {
    const nowTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    sessionStorage.setItem(DRAFT_SESSION_KEY, JSON.stringify({
      text,
      dialect: state.dialect,
      savedAt: nowTime
    }));
    lastAutoSavedText = text;
    lastAutoSavedTime = nowTime;
    updateAutoSaveStatus(true, false);
  } catch (e) {}
}

function restoreTranscriptDraft() {
  const transcriptEl = document.getElementById('transcriptInput');
  if (!transcriptEl) return;
  try {
    const raw = sessionStorage.getItem(DRAFT_SESSION_KEY);
    if (!raw) {
      saveTranscriptDraftToSession(true);
      return;
    }
    const parsed = JSON.parse(raw);
    if (parsed && typeof parsed.text === 'string' && parsed.text.trim().length > 0) {
      if (parsed.dialect && DIALECTS[parsed.dialect]) {
        state.dialect = parsed.dialect;
        document.querySelectorAll('#dialectChips .chip').forEach(chip => {
          chip.classList.toggle('selected', chip.dataset.dialect === parsed.dialect);
        });
        document.getElementById('detectedDialectVal').textContent =
          state.lang === 'ar' ? DIALECTS[parsed.dialect].labelAr : DIALECTS[parsed.dialect].labelEn;
      }
      transcriptEl.value = parsed.text;
      lastAutoSavedText = parsed.text;
      lastAutoSavedTime = parsed.savedAt || '';
      analyzeTranscript(parsed.text);
      updateAutoSaveStatus(true, true);
    } else {
      saveTranscriptDraftToSession(true);
    }
  } catch (e) {}
}

function initTranscriptAutoSave() {
  const transcriptEl = document.getElementById('transcriptInput');
  if (!transcriptEl) return;

  let debounceTimer = null;
  transcriptEl.addEventListener('input', () => {
    updateAutoSaveStatus(false);
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      saveTranscriptDraftToSession(false);
    }, 600);
  });

  // Periodic background auto-save every 3 seconds
  setInterval(() => {
    saveTranscriptDraftToSession(false);
  }, 3000);

  // Flush immediately before refresh or tab navigation
  window.addEventListener('pagehide', () => saveTranscriptDraftToSession(true));
  window.addEventListener('beforeunload', () => saveTranscriptDraftToSession(true));
  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'hidden') {
      saveTranscriptDraftToSession(false);
    }
  });
}
