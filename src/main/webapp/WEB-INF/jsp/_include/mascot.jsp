<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%-- Tabi AI Mascot — floating chat assistant, global on authenticated pages --%>
<style>
/* ── Tabi Mascot ── */
.tabi-mascot {
  position: fixed;
  z-index: 250;
  font-family: var(--font);
}

/* Collapsed toggle button */
.tabi-toggle {
  position: fixed;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #4a6cf7 0%, #6c5ce7 100%);
  box-shadow: 0 4px 18px rgba(74, 108, 247, 0.38);
  cursor: grab;
  display: flex;
  align-items: center;
  justify-content: center;
  touch-action: none;
  user-select: none;
  animation: tabi-bounce 2.4s ease-in-out infinite;
}

.tabi-toggle:active {
  cursor: grabbing;
}

.tabi-toggle.dragging {
  animation: none;
}

.tabi-toggle:hover {
  transform: scale(1.12);
  box-shadow: 0 8px 28px rgba(74, 108, 247, 0.5);
}

.tabi-toggle:hover:active {
  transform: scale(0.95);
}

.tabi-emoji {
  font-size: 1.65rem;
  line-height: 1;
  pointer-events: none;
}

@keyframes tabi-bounce {
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-6px); }
}

/* Chat panel */
.tabi-panel {
  position: fixed;
  width: 380px;
  height: 500px;
  max-height: calc(100vh - 6rem);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  box-shadow: 0 14px 52px rgba(15, 23, 42, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tabi-panel.dragging {
  user-select: none;
}

.tabi-panel[hidden] {
  display: none;
}

/* Panel header — drag handle */
.tabi-panel__header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 1rem;
  background: linear-gradient(135deg, #4a6cf7 0%, #6c5ce7 100%);
  color: #fff;
  flex-shrink: 0;
  cursor: grab;
  user-select: none;
  touch-action: none;
}

.tabi-panel__header:active {
  cursor: grabbing;
}

.tabi-panel__avatar {
  font-size: 1.3rem;
  line-height: 1;
  pointer-events: none;
}

.tabi-panel__name {
  font-weight: 700;
  font-size: 0.95rem;
  flex: 1;
  pointer-events: none;
}

.tabi-panel__status {
  font-size: 0.68rem;
  opacity: 0.85;
  padding: 0.12rem 0.45rem;
  background: rgba(255,255,255,0.2);
  border-radius: 10px;
  pointer-events: none;
}

.tabi-panel__close {
  background: rgba(255,255,255,0.18);
  border: none;
  color: #fff;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.1rem;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.tabi-panel__close:hover {
  background: rgba(255,255,255,0.32);
}

/* Messages area */
.tabi-messages {
  flex: 1;
  overflow-y: auto;
  padding: 0.85rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
  background: #f8fafc;
  scroll-behavior: smooth;
}

/* Chat bubbles */
.tabi-bubble {
  max-width: 88%;
  padding: 0.55rem 0.8rem;
  border-radius: var(--radius);
  font-size: 0.875rem;
  line-height: 1.5;
  word-wrap: break-word;
  animation: aiFadeSlide 0.22s ease both;
}

.tabi-bubble--bot {
  align-self: flex-start;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-bottom-left-radius: 4px;
  color: var(--color-text);
}

.tabi-bubble--user {
  align-self: flex-end;
  background: var(--color-primary);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.tabi-bubble--bot strong {
  color: var(--color-ai-primary, #4a6cf7);
}

/* Typing indicator */
.tabi-typing {
  align-self: flex-start;
  display: flex;
  gap: 0.28rem;
  padding: 0.65rem 0.9rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  border-bottom-left-radius: 4px;
}

.tabi-typing span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--color-text-muted);
  animation: tabi-dot 1.3s ease-in-out infinite;
}

.tabi-typing span:nth-child(2) { animation-delay: 0.18s; }
.tabi-typing span:nth-child(3) { animation-delay: 0.36s; }

@keyframes tabi-dot {
  0%, 80%, 100% { opacity: 0.3; transform: scale(0.75); }
  40%           { opacity: 1; transform: scale(1); }
}

/* Input area */
.tabi-input-area {
  display: flex;
  gap: 0.5rem;
  padding: 0.65rem 0.8rem;
  border-top: 1px solid var(--color-border);
  background: var(--color-surface);
  flex-shrink: 0;
}

.tabi-input {
  flex: 1;
  border: 1px solid var(--color-border);
  border-radius: 20px;
  padding: 0.5rem 0.9rem;
  font-size: 0.875rem;
  font-family: var(--font);
  outline: none;
  transition: border-color 0.2s ease;
  line-height: 1.4;
}

.tabi-input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-primary-muted);
}

.tabi-send-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: var(--color-primary);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.05rem;
  flex-shrink: 0;
  transition: background 0.2s ease, transform 0.15s ease;
}

.tabi-send-btn:hover {
  background: var(--color-primary-hover);
  transform: scale(1.05);
}

.tabi-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* Job card inside chat */
.tabi-job-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  padding: 0.65rem 0.8rem;
  align-self: flex-start;
  max-width: 100%;
  width: 100%;
  box-shadow: var(--shadow-sm);
}

.tabi-job-card__title {
  font-weight: 700;
  font-size: 0.88rem;
  margin-bottom: 0.25rem;
  line-height: 1.4;
  padding-block: 0.05em;
}

.tabi-job-card__meta {
  font-size: 0.78rem;
  color: var(--color-text-muted);
  margin-bottom: 0.35rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 0.55rem;
  align-items: center;
}

.tabi-job-card__score {
  font-weight: 700;
  font-size: 0.78rem;
}

.tabi-job-card__score--high { color: #15803d; }
.tabi-job-card__score--med  { color: #c2410c; }
.tabi-job-card__score--low  { color: #b91c1c; }

.tabi-job-card__skills {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem;
  margin-bottom: 0.5rem;
}

.tabi-job-card__pill {
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.15rem 0.4rem;
  border-radius: 4px;
  line-height: 1.35;
}

.tabi-job-card__pill--match {
  background: #d1fae5;
  color: #065f46;
}

.tabi-job-card__pill--miss {
  background: var(--color-danger-bg);
  color: var(--color-danger);
}

.tabi-job-card__apply {
  font-size: 0.78rem;
  padding: 0.25rem 0.7rem;
}

/* Mobile fullscreen */
@media (max-width: 540px) {
  .tabi-panel {
    position: fixed;
    inset: 0;
    width: 100%;
    height: 100%;
    max-height: 100vh;
    border-radius: 0;
    left: 0 !important;
    top: 0 !important;
    right: 0 !important;
    bottom: 0 !important;
  }

  .tabi-toggle {
    width: 50px;
    height: 50px;
    left: auto !important;
    top: auto !important;
    right: 0.75rem !important;
    bottom: 0.75rem !important;
  }
}

@media (prefers-reduced-motion: reduce) {
  .tabi-toggle { animation: none; }
  .tabi-typing span { animation: none; }
}
</style>

<div id="tabi-mascot" class="tabi-mascot">
  <%-- Collapsed button --%>
  <button id="tabi-toggle" class="tabi-toggle" aria-label="Chat with Tabi" title="Drag to move, click to chat">
    <span class="tabi-emoji">&#x1F989;</span>
  </button>

  <%-- Chat panel --%>
  <div id="tabi-panel" class="tabi-panel" hidden>
    <div class="tabi-panel__header" id="tabi-panel-header">
      <span class="tabi-panel__avatar">&#x1F989;</span>
      <span class="tabi-panel__name">Tabi</span>
      <span class="tabi-panel__status">Online</span>
      <button id="tabi-close" class="tabi-panel__close" aria-label="Minimize">&minus;</button>
    </div>
    <div id="tabi-messages" class="tabi-messages" role="log" aria-live="polite"></div>
    <div class="tabi-input-area">
      <input id="tabi-input" type="text" class="tabi-input" placeholder="Ask Tabi..." maxlength="500" autocomplete="off">
      <button id="tabi-send" class="tabi-send-btn" aria-label="Send">&#x27A4;</button>
    </div>
  </div>
</div>

<script>
(function () {
  'use strict';
  var TABI_KEY = 'tabi_chat_history';
  var POS_KEY = 'tabi_position';
  var ctx = '${pageContext.request.contextPath}';

  // State
  var history = [];
  try {
    var raw = sessionStorage.getItem(TABI_KEY);
    history = raw ? JSON.parse(raw) : [];
  } catch (e) { history = []; }

  // DOM
  var toggle = document.getElementById('tabi-toggle');
  var panel = document.getElementById('tabi-panel');
  var closeBtn = document.getElementById('tabi-close');
  var messages = document.getElementById('tabi-messages');
  var input = document.getElementById('tabi-input');
  var sendBtn = document.getElementById('tabi-send');
  var mascotRoot = document.getElementById('tabi-mascot');

  if (!toggle || !panel) return;

  // ═══════════════════════════════════
  //  DRAG  —  mousedown/mousemove/mouseup  +  touch
  // ═══════════════════════════════════════════

  var _dragEl = null;
  var _dragSX, _dragSY;
  var _elSL, _elST;
  var _dragMoved = false;

  var savedPos = null;
  try { var p = localStorage.getItem(POS_KEY); if (p) savedPos = JSON.parse(p); } catch (e) {}

  function savePos(l, t) { try { localStorage.setItem(POS_KEY, JSON.stringify({left:l,top:t})); } catch (e) {} }

  function setPos(el, l, t) { el.style.left=l+'px'; el.style.top=t+'px'; el.style.right='auto'; el.style.bottom='auto'; }

  function clamp(l, t, w, h) {
    var vw=window.innerWidth, vh=window.innerHeight;
    return { left: Math.max(-w+32, Math.min(vw-32,l)), top: Math.max(0, Math.min(vh-32,t)) };
  }

  function placeToggle() {
    var vw=window.innerWidth, vh=window.innerHeight;
    var l=savedPos&&typeof savedPos.left=='number'?savedPos.left:vw-56-24;
    var t=savedPos&&typeof savedPos.top=='number'?savedPos.top:Math.round(vh*0.35);
    var c=clamp(l,t,56,56);
    setPos(toggle,c.left,c.top);
  }

  function placePanel() {
    var tL=parseFloat(toggle.style.left)||0, tT=parseFloat(toggle.style.top)||0;
    var pW=380, pH=500;
    var l=tL-(pW-56)/2, t=tT-pH+56;
    var c=clamp(l,t,pW,pH);
    setPos(panel,c.left,c.top);
  }

  placeToggle();

  function dragStart(el, cx, cy) {
    if (window.innerWidth <= 540) return;
    _dragEl = el;
    _dragMoved = false;
    _dragSX = cx; _dragSY = cy;
    _elSL = parseFloat(el.style.left) || 0;
    _elST = parseFloat(el.style.top)  || 0;
    el.classList.add('dragging');
  }

  function dragMove(cx, cy) {
    if (!_dragEl) return;
    var dx=cx-_dragSX, dy=cy-_dragSY;
    if (Math.abs(dx)<2 && Math.abs(dy)<2) return;
    _dragMoved = true;
    var c=clamp(_elSL+dx, _elST+dy, _dragEl.offsetWidth, _dragEl.offsetHeight);
    setPos(_dragEl, c.left, c.top);
  }

  function dragEnd() {
    if (!_dragEl) return;
    _dragEl.classList.remove('dragging');
    var wasToggle = (_dragEl === toggle);
    _dragEl = null;
    if (_dragMoved && wasToggle) {
      var tl=parseFloat(toggle.style.left)||0, tt=parseFloat(toggle.style.top)||0;
      savedPos = {left:tl, top:tt};
      savePos(tl, tt);
      placePanel();
    }
  }

  // Toggle: mousedown/touchstart to start drag
  toggle.addEventListener('mousedown', function(e){ dragStart(toggle, e.clientX, e.clientY); });
  toggle.addEventListener('touchstart', function(e){
    var t=e.touches[0]; dragStart(toggle, t.clientX, t.clientY);
  }, {passive:true});

  // Panel header: drag, but skip if clicking close button
  var panelHdr = document.getElementById('tabi-panel-header');
  panelHdr.addEventListener('mousedown', function(e){
    if (e.target === closeBtn) return;
    dragStart(panel, e.clientX, e.clientY);
  });
  panelHdr.addEventListener('touchstart', function(e){
    if (e.target === closeBtn) return;
    var t=e.touches[0]; dragStart(panel, t.clientX, t.clientY);
  }, {passive:true});

  // Global move/end (works because events bubble to document)
  document.addEventListener('mousemove', function(e){ dragMove(e.clientX, e.clientY); });
  document.addEventListener('touchmove', function(e){
    if (!_dragEl) return;
    var t=e.touches[0]; dragMove(t.clientX, t.clientY);
  }, {passive:true});
  document.addEventListener('mouseup', dragEnd);
  document.addEventListener('touchend', dragEnd);

  window.addEventListener('resize', function(){
    var tl=parseFloat(toggle.style.left)||0, tt=parseFloat(toggle.style.top)||0;
    var c=clamp(tl,tt,56,56);
    setPos(toggle,c.left,c.top);
    placePanel();
  });

  // ═══════════════════════════════════════════════
  //  OPEN / CLOSE
  // ═══════════════════════════════════════════════

  toggle.addEventListener('click', function(e){
    if (_dragMoved) { _dragMoved=false; return; }
    openPanel();
  });

  closeBtn.addEventListener('click', function(e){
    e.stopPropagation();
    closePanel();
  });

  document.addEventListener('click', function(e){
    if (_dragMoved) { _dragMoved=false; return; }
    if (mascotRoot && !mascotRoot.contains(e.target) && !panel.hidden) closePanel();
  });

  function openPanel() {
    placePanel();
    panel.hidden = false;
    toggle.style.display = 'none';
    scrollDown();
    setTimeout(function(){ input.focus(); }, 120);
  }

  function closePanel() {
    panel.hidden = true;
    toggle.style.display = 'flex';
    toggle.focus();
  }

  // ═══════════════════════════════════════════════
  //  CHAT
  // ═══════════════════════════════════════════════

  sendBtn.addEventListener('click', sendMessage);
  input.addEventListener('keydown', function (e) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  });

  function sendMessage() {
    var text = input.value.trim();
    if (!text || sendBtn.disabled) return;
    input.value = '';

    addUserBubble(text);
    saveHistory({ role: 'user', text: text });

    showTyping();
    sendBtn.disabled = true;

    fetch(ctx + '/api/mascot/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ message: text })
    })
    .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.json(); })
    .then(function (data) {
      hideTyping();
      var reply = (data && data.reply) ? data.reply : 'Hmm, I had a little brain freeze. Try again?';
      addBotBubble(reply);
      saveHistory({ role: 'bot', text: reply, cards: data.cards || [] });
      if (data.cards && data.cards.length) {
        data.cards.forEach(function (c) { renderCard(c); });
      }
    })
    .catch(function () {
      hideTyping();
      addBotBubble('Hi! Tabi is recharging her wisdom \u2728 Please try again in a moment.');
    })
    .finally(function () {
      sendBtn.disabled = false;
      input.focus();
      scrollDown();
    });
  }

  // ── Render ──
  function addUserBubble(text) {
    var div = document.createElement('div');
    div.className = 'tabi-bubble tabi-bubble--user';
    div.textContent = text;
    messages.appendChild(div);
    scrollDown();
  }

  function addBotBubble(text) {
    var div = document.createElement('div');
    div.className = 'tabi-bubble tabi-bubble--bot';
    div.innerHTML = formatText(text);
    messages.appendChild(div);
    scrollDown();
  }

  function renderCard(card) {
    switch (card.cardType) {
      case 'status_card':    renderStatusCard(card); break;
      case 'profile_card':   renderProfileCard(card); break;
      case 'applicant_card': renderApplicantCard(card); break;
      case 'alert_card':     renderAlertCard(card); break;
      default:               renderJobCard(card); break;
    }
  }

  function renderStatusCard(card) {
    var div = document.createElement('div');
    div.className = 'tabi-job-card';
    var st = (card.appStatus||'').toUpperCase();
    var badgeCls = st==='SUBMITTED'?'badge submitted':st==='UNDER_REVIEW'?'badge under-review':st==='SELECTED'?'badge selected':st==='REJECTED'?'badge rejected':'badge';
    div.innerHTML =
      '<div class="tabi-job-card__title">' + escHtml(card.title) + '</div>' +
      '<div class="tabi-job-card__meta">' +
        '<span class="' + badgeCls + '">' + escHtml(st||'N/A') + '</span>' +
        (card.matchScore ? '<span class="tabi-job-card__score">' + card.matchScore + '% match</span>' : '') +
      '</div>' +
      '<a class="btn btn-ghost btn-sm tabi-job-card__apply" href="' + ctx + '/ta/status">View Status &rarr;</a>';
    messages.appendChild(div);
    scrollDown();
  }

  function renderProfileCard(card) {
    var div = document.createElement('div');
    div.className = 'tabi-job-card';
    var items = [];
    if (card.missingName)  items.push('Name');
    if (card.missingEmail) items.push('Email');
    if (card.missingMajor) items.push('Major');
    if (card.missingSkills) items.push('Skills');
    if (card.missingAvail) items.push('Availability');
    if (card.missingCV)    items.push('CV');
    var missingHtml = items.length ? '<div style="color:var(--color-danger);font-size:0.78rem;margin-bottom:0.35rem;">Missing: ' + items.join(', ') + '</div>' : '';
    div.innerHTML =
      '<div class="tabi-job-card__title">Profile: ' + card.pct + '% complete</div>' +
      missingHtml +
      '<div style="display:flex;gap:0.4rem;flex-wrap:wrap;">' +
        '<a class="btn btn-primary btn-sm tabi-job-card__apply" href="' + ctx + '/ta/profile">Edit Profile</a>' +
        (card.missingCV ? '<a class="btn btn-ghost btn-sm tabi-job-card__apply" href="' + ctx + '/ta/cv">Upload CV</a>' : '') +
      '</div>';
    messages.appendChild(div);
    scrollDown();
  }

  function renderApplicantCard(card) {
    var div = document.createElement('div');
    div.className = 'tabi-job-card';
    var detail = card.totalApplicants > 0
      ? '<div style="font-size:0.78rem;color:var(--color-text-muted);margin-bottom:0.35rem;">' + card.totalApplicants + ' applicant(s) — ' + card.submittedCount + ' submitted, ' + card.underReviewCount + ' under review</div>'
      : '<div style="font-size:0.78rem;color:var(--color-text-muted);margin-bottom:0.35rem;">No applicants yet</div>';
    div.innerHTML =
      '<div class="tabi-job-card__title">' + escHtml(card.title) + '</div>' +
      '<div class="tabi-job-card__meta"><span class="badge">' + escHtml(card.status) + '</span><span>Cap: ' + card.capacity + '</span></div>' +
      detail +
      '<a class="btn btn-primary btn-sm tabi-job-card__apply" href="' + ctx + '/mo/jobs/select">Review Applicants &rarr;</a>';
    messages.appendChild(div);
    scrollDown();
  }

  function renderAlertCard(card) {
    var div = document.createElement('div');
    div.className = 'tabi-job-card';
    var isRed = card.alertType === 'over_capacity';
    div.style.borderLeft = '3px solid ' + (isRed ? 'var(--color-danger)' : 'var(--color-warning)');
    var msg = isRed
      ? '<span style="color:var(--color-danger);font-weight:700;">Over Capacity:</span> ' + card.selected + ' selected / cap ' + card.capacity
      : '<span style="color:var(--color-warning);font-weight:700;">No Selection:</span> 0 selected / cap ' + card.capacity;
    div.innerHTML =
      '<div class="tabi-job-card__title">' + escHtml(card.title) + '</div>' +
      '<div style="font-size:0.78rem;margin-bottom:0.4rem;">' + msg + '</div>' +
      '<a class="btn btn-ghost btn-sm tabi-job-card__apply" href="' + ctx + '/admin/workload">View Workload &rarr;</a>';
    messages.appendChild(div);
    scrollDown();
  }

  function renderJobCard(card) {
    var div = document.createElement('div');
    div.className = 'tabi-job-card';
    var sc = card.matchScore >= 80 ? 'high' : card.matchScore >= 50 ? 'med' : 'low';
    var sk = '';
    (card.matchedSkills || []).forEach(function (s) { sk += '<span class="tabi-job-card__pill tabi-job-card__pill--match">&#x2713; ' + escHtml(s) + '</span>'; });
    (card.missingSkills || []).forEach(function (s) { sk += '<span class="tabi-job-card__pill tabi-job-card__pill--miss">&#x2717; ' + escHtml(s) + '</span>'; });
    var btn;
    if (card.applied) {
      btn = '<button class="btn btn-secondary btn-sm tabi-job-card__apply" disabled>Applied</button>';
    } else if (card.status === 'OPEN') {
      btn = '<button class="btn btn-primary btn-sm tabi-job-card__apply" onclick="window._tabiApply(\'' + escHtml(card.jobId) + '\',this)">Apply &#x27A4;</button>';
    } else {
      btn = '<button class="btn btn-secondary btn-sm tabi-job-card__apply" disabled>Closed</button>';
    }
    div.innerHTML =
      '<div class="tabi-job-card__title">' + escHtml(card.title) + '</div>' +
      '<div class="tabi-job-card__meta">' +
        '<span class="badge badge-' + (card.type||'').toLowerCase() + '">' + escHtml(card.type) + '</span>' +
        '<span>' + escHtml(card.schedule) + '</span>' +
        '<span class="tabi-job-card__score tabi-job-card__score--' + sc + '">' + card.matchScore + '% match</span>' +
      '</div>' +
      '<div class="tabi-job-card__skills">' + sk + '</div>' + btn;
    messages.appendChild(div);
    scrollDown();
  }

  window._tabiApply = function (jobId, btn) {
    btn.disabled = true;
    btn.textContent = 'Applying\u2026';
    var f = document.createElement('form');
    f.method = 'POST';
    f.action = ctx + '/ta/apply';
    var i = document.createElement('input');
    i.type = 'hidden'; i.name = 'jobId'; i.value = jobId;
    f.appendChild(i);
    document.body.appendChild(f);
    f.submit();
  };

  // ── Typing ──
  function showTyping() {
    if (document.getElementById('tabi-typing-indicator')) return;
    var d = document.createElement('div');
    d.id = 'tabi-typing-indicator';
    d.className = 'tabi-typing';
    d.innerHTML = '<span></span><span></span><span></span>';
    messages.appendChild(d);
    scrollDown();
  }
  function hideTyping() {
    var el = document.getElementById('tabi-typing-indicator');
    if (el) el.remove();
  }

  // ── History ──
  function saveHistory(entry) {
    history.push(entry);
    if (history.length > 60) history = history.slice(-60);
    try { sessionStorage.setItem(TABI_KEY, JSON.stringify(history)); } catch (e) {}
  }

  function renderHistory() {
    messages.innerHTML = '';
    history.forEach(function (e) {
      if (e.role === 'user') addUserBubble(e.text);
      else if (e.role === 'bot') {
        if (e.text) addBotBubble(e.text);
        if (e.cards) e.cards.forEach(function (c) { renderCard(c); });
      }
    });
  }

  // ── Utils ──
  function scrollDown() { messages.scrollTop = messages.scrollHeight; }

  function escHtml(s) {
    if (!s) return '';
    var d = document.createElement('div');
    d.textContent = s;
    return d.innerHTML;
  }

  function formatText(t) {
    if (!t) return '';
    return escHtml(t).replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
  }

  // ── Init ──
  if (history.length > 0) {
    renderHistory();
  } else {
    addBotBubble('Hi! I\u2019m **Tabi** the owl \uD83E\uDD89 Ask me about TA jobs, your applications, or just say hi!');
    saveHistory({ role: 'bot', text: 'Hi! I\u2019m **Tabi** the owl \uD83E\uDD89 Ask me about TA jobs, your applications, or just say hi!', cards: [] });
  }
})();
</script>
