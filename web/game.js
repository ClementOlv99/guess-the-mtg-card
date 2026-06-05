'use strict';

// ── Constants ─────────────────────────────────────────────────────────────────

const HINTS_MAX = 11;

// Proportional text coordinates, mirrored from CardPanel.java
const C = {
  creature:    { nameY: 0.085, typeY: 0.600, oraY: 0.640, oraBotY: 0.942, ptX: 0.875, ptY: 0.920 },
  nonCreature: { nameY: 0.085, typeY: 0.595, oraY: 0.638, oraBotY: 0.938 },
  textLeft:  0.092,
  textRight: 0.908,
};

// ── DOM refs ──────────────────────────────────────────────────────────────────

const canvas       = document.getElementById('cardCanvas');
const ctx          = canvas.getContext('2d');
const hintsEl      = document.getElementById('hints');
const scoreEl      = document.getElementById('score');
const seedEl       = document.getElementById('seed');
const letterInput  = document.getElementById('letterInput');
const guessBtn     = document.getElementById('guessBtn');
const newCardBtn   = document.getElementById('newCardBtn');
const setSeedBtn   = document.getElementById('setSeedBtn');
const showAnsBtn   = document.getElementById('showAnsBtn');
const scryfallLink = document.getElementById('scryfallLink');
const guessModal      = document.getElementById('guessModal');
const guessInput      = document.getElementById('guessInput');
const confirmBtn      = document.getElementById('confirmGuess');
const closeModal      = document.getElementById('closeModal');
const resultMsg       = document.getElementById('resultMsg');
const loadingOverlay  = document.getElementById('loadingOverlay');

// ── Images ────────────────────────────────────────────────────────────────────

const creatureImg = new Image();
const ncImg       = new Image();

// Track readiness of images and card data separately
let imagesReady = 0;
let cardReady   = false;
let pendingCard = null;

function onImageSettled() {
  imagesReady++;
  // If card data already loaded while images were fetching, start now
  if (imagesReady === 2 && cardReady) {
    startCard(pendingCard);
    loadingOverlay.classList.add('hidden');
  }
}

creatureImg.onload = creatureImg.onerror = onImageSettled;
ncImg.onload       = ncImg.onerror       = onImageSettled;
creatureImg.src = '../assets/frame.png';
ncImg.src       = '../assets/ncframe.jpg';

// ── State ─────────────────────────────────────────────────────────────────────

let allLines   = null; // cached JSONL lines from cardlist.json
let card       = null;
let revealed   = {};   // { name, manacost, type, power, toughness, ruletext } — char arrays
let hintsLeft  = HINTS_MAX;
let score      = 0;
let over       = false;
let won        = false;
let alreadyUsed = new Set();

// ── Card data ─────────────────────────────────────────────────────────────────

async function getLines() {
  if (!allLines) {
    const res = await fetch('../cardlist.json');
    if (!res.ok) throw new Error(`Failed to fetch card data: ${res.status}`);
    const text = await res.text();
    allLines = text.split('\n').filter(l => l.trim());
  }
  return allLines;
}

async function loadRandom() {
  const lines = await getLines();
  const idx   = Math.floor(Math.random() * lines.length);
  return parseCard(lines[idx], idx);
}

async function loadById(id) {
  const lines = await getLines();
  if (id < 0 || id >= lines.length) return null;
  return parseCard(lines[id], id);
}

function parseCard(line, id) {
  const j = JSON.parse(line);
  return {
    id,
    name:       j.name,
    manacost:   j.mana_cost,
    type:       j.type_line,
    ruletext:   j.oracle_text,
    power:      j.power      ?? '',
    toughness:  j.toughness  ?? '',
    rarity:     j.rarity,
    url:        'https://scryfall.com/search?q=' + encodeURIComponent(j.name),
    isCreature: j.type_line.includes('Creature'),
  };
}

// ── Game logic ────────────────────────────────────────────────────────────────

function to8Dash(text) {
  return Array.from(text).map(c => /[a-zA-Z0-9]/.test(c) ? '_' : c);
}

function revealAll() {
  revealed.name      = Array.from(card.name);
  revealed.manacost  = Array.from(card.manacost);
  revealed.type      = Array.from(card.type);
  revealed.power     = Array.from(card.power);
  revealed.toughness = Array.from(card.toughness);
  revealed.ruletext  = Array.from(card.ruletext);
}

function startCard(c) {
  card = c;
  hintsLeft   = HINTS_MAX;
  over        = false;
  alreadyUsed = new Set();
  revealed = {
    name:      to8Dash(card.name),
    manacost:  to8Dash(card.manacost),
    type:      to8Dash(card.type),
    power:     to8Dash(card.power),
    toughness: to8Dash(card.toughness),
    ruletext:  to8Dash(card.ruletext),
  };
  scryfallLink.style.display = 'none';
  scryfallLink.href          = card.url;
  letterInput.disabled       = false;
  letterInput.value          = '';
  letterInput.focus();
  updateUI();
  drawCard();
}

function endGame(correct) {
  if (over) return;
  over = true;
  won  = correct;
  if (correct) score += 100 + 10 * hintsLeft;
  hintsLeft = 0;
  revealAll();
  letterInput.disabled       = true;
  scryfallLink.style.display = 'inline';
  updateUI();
  drawCard();
}

function revealLetter(ch) {
  if (over || alreadyUsed.has(ch) || hintsLeft <= 0) return;
  alreadyUsed.add(ch);
  hintsLeft--;

  const origMap = {
    name: card.name, manacost: card.manacost, type: card.type,
    power: card.power, toughness: card.toughness, ruletext: card.ruletext,
  };

  for (const field of Object.keys(origMap)) {
    const orig = origMap[field];
    revealed[field] = revealed[field].map((disp, i) =>
      (disp === '_' && orig[i].toLowerCase() === ch) ? orig[i] : disp
    );
  }

  if (hintsLeft === 0) {
    endGame(false);
  } else {
    updateUI();
    drawCard();
  }
}

function updateUI() {
  hintsEl.textContent = 'Hints left: ' + hintsLeft;
  scoreEl.textContent = 'Score: ' + score;
  seedEl.textContent  = 'Seed: ' + card.id;
}

// ── Canvas rendering ──────────────────────────────────────────────────────────

// Insert thin space between characters so letter count is visible;
// preserve real spaces and newlines as word/line break points.
function withSpacing(chars) {
  let out = '';
  for (let i = 0; i < chars.length; i++) {
    out += chars[i];
    if (chars[i] !== ' ' && chars[i] !== '\n' && i < chars.length - 1) {
      out += ' ';
    }
  }
  return out;
}

function parsePx(fontStr) {
  const m = fontStr.match(/(\d+(?:\.\d+)?)px/);
  return m ? parseFloat(m[1]) : 12;
}

function drawCard() {
  const img = card.isCreature ? creatureImg : ncImg;
  const pw = canvas.width, ph = canvas.height;
  ctx.clearRect(0, 0, pw, ph);

  // Scale image to fit panel, preserving aspect ratio
  const aspect = img.naturalWidth / img.naturalHeight;
  let iw, ih;
  if (pw / ph > aspect) { ih = ph; iw = ih * aspect; }
  else                   { iw = pw; ih = iw / aspect; }
  const ix = (pw - iw) / 2;
  const iy = (ph - ih) / 2;
  ctx.drawImage(img, ix, iy, iw, ih);

  const co = card.isCreature ? C.creature : C.nonCreature;
  const tl = ix + iw * C.textLeft;
  const tr = ix + iw * C.textRight;
  const tw = tr - tl;
  const sz = Math.max(10, iw / 30);

  const nameF = `bold ${sz}px Arial`;
  const bodyF = `${Math.max(8, sz - 1)}px Arial`;
  const oraF  = `${Math.max(8, sz - 4)}px Arial`;

  ctx.fillStyle = '#000';

  // Name — left-aligned on name bar
  ctx.font = nameF;
  ctx.fillText(withSpacing(revealed.name), tl, iy + ih * co.nameY);

  // Mana cost — right-aligned on name bar
  ctx.font = bodyF;
  const mana = withSpacing(revealed.manacost);
  ctx.fillText(mana, tr - ctx.measureText(mana).width, iy + ih * co.nameY);

  // Type line
  ctx.fillText(withSpacing(revealed.type), tl, iy + ih * co.typeY);

  // Oracle text (word-wrapped)
  ctx.font = oraF;
  drawWrapped(
    withSpacing(revealed.ruletext),
    tl, iy + ih * co.oraY, tw, iy + ih * co.oraBotY, parsePx(oraF)
  );

  // Power / Toughness (creature only)
  if (card.isCreature) {
    ctx.font = nameF;
    const pt   = withSpacing(revealed.power) + '/' + withSpacing(revealed.toughness);
    const ptX  = ix + iw * C.creature.ptX - ctx.measureText(pt).width / 2;
    ctx.fillText(pt, ptX, iy + ih * C.creature.ptY);
  }
}

function drawWrapped(text, x, y, maxW, botY, fontSize) {
  const lh = fontSize * 1.35;
  let cy = y + fontSize;
  for (const para of text.split('\n')) {
    if (cy > botY) return;
    const words = para.split(' ');
    let line = '';
    for (const word of words) {
      const test = line ? line + ' ' + word : word;
      if (ctx.measureText(test).width > maxW && line) {
        ctx.fillText(line, x, cy);
        cy += lh;
        if (cy > botY) return;
        line = word;
      } else {
        line = test;
      }
    }
    if (line) { ctx.fillText(line, x, cy); cy += lh; }
  }
}

// ── Event listeners ───────────────────────────────────────────────────────────

letterInput.addEventListener('input', e => {
  const ch = (e.data ?? letterInput.value).slice(-1).toLowerCase();
  if (/^[a-z0-9]$/.test(ch)) revealLetter(ch);
});

guessBtn.addEventListener('click', () => {
  if (over) return;
  guessInput.value    = '';
  resultMsg.textContent = '';
  resultMsg.className   = '';
  guessModal.style.display = 'flex';
  guessInput.focus();
});

confirmBtn.addEventListener('click', submitGuess);
guessInput.addEventListener('keydown', e => { if (e.key === 'Enter') submitGuess(); });
closeModal.addEventListener('click',   () => { guessModal.style.display = 'none'; });

// Close modal on backdrop click
guessModal.addEventListener('click', e => {
  if (e.target === guessModal) guessModal.style.display = 'none';
});

function submitGuess() {
  const answer = guessInput.value.trim();
  if (!answer) return;
  const correct = answer.toLowerCase() === card.name.toLowerCase();
  endGame(correct);
  resultMsg.textContent = correct ? '✓ Correct!' : '✗ Wrong!';
  resultMsg.className   = correct ? 'correct' : 'wrong';
  setTimeout(() => { guessModal.style.display = 'none'; }, 1500);
}

newCardBtn.addEventListener('click', async () => {
  if (!won) score = 0;
  won = false;
  const c = await loadRandom();
  startCard(c);
});

setSeedBtn.addEventListener('click', async () => {
  const lines = await getLines();
  const input = prompt(`Enter seed (0 – ${lines.length - 1}):`);
  if (input === null) return;
  const id = parseInt(input.trim(), 10);
  if (isNaN(id)) { alert('Please enter a valid number.'); return; }
  const c = await loadById(id);
  if (!c) { alert('Invalid seed.'); return; }
  score = 0;
  won   = false;
  startCard(c);
});

showAnsBtn.addEventListener('click', () => {
  won = false;
  endGame(false);
});

// ── Init ──────────────────────────────────────────────────────────────────────

async function init() {
  canvas.width  = 500;
  canvas.height = 620;
  try {
    const c = await loadRandom();
    pendingCard = c;
    cardReady   = true;
    if (imagesReady === 2) {
      startCard(c);
      loadingOverlay.classList.add('hidden');
    }
  } catch (err) {
    document.querySelector('#loadingBox p').textContent = 'Failed to load card data. ' + err.message;
    console.error(err);
  }
}

init();
