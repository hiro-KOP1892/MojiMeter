/**
 * 文章カウントアシスト - フロントエンドJavaScript
 * 文字数カウント、プログレスバー、保存・コピーを担当
 */

// DOM要素の取得
const targetCountInput = document.getElementById('targetCount');
const countModeInput = document.getElementById('countMode');
const ninetyPercentInput = document.getElementById('ninetyPercent');
const esTextarea = document.getElementById('esText');
const currentCountSpan = document.getElementById('currentCount');
const currentPercentSpan = document.getElementById('currentPercent');
const remainingValueSpan = document.getElementById('remainingValue');
const progressBar = document.getElementById('progressBar');
const progressValue = document.getElementById('progressValue');
const progressStatus = document.getElementById('progressStatus');
const goalMarker = document.getElementById('goalMarker');
const saveBtn = document.getElementById('saveBtn');
const copyTextBtn = document.getElementById('copyTextBtn');
const draftSelect = document.getElementById('draftSelect');
const loadDraftBtn = document.getElementById('loadDraftBtn');
const draftCount = document.getElementById('draftCount');

const DRAFT_STORAGE_KEY = 'es-helper-ai-drafts';

/**
 * 文字数カウントと進捗バーを更新
 */
function updateProgress() {
    const targetCount = parseInt(targetCountInput.value, 10) || 0;
    const ninetyPercent = Math.floor(targetCount * 0.9);
    const currentCount = countText(esTextarea.value);
    const percent = targetCount > 0 ? Math.round((currentCount / targetCount) * 100) : 0;
    const remaining = Math.max(0, ninetyPercent - currentCount);

    // 9割ラインの値を更新
    ninetyPercentInput.value = ninetyPercent;

    // 文字数表示を更新
    currentCountSpan.textContent = currentCount;
    currentPercentSpan.textContent = percent;
    remainingValueSpan.textContent = remaining;

    // プログレスバーを更新
    const barPercent = Math.min(percent, 120); // 上限超過時も状態を確認できる幅に制限
    progressBar.style.width = barPercent + '%';
    progressValue.textContent = barPercent + '%';

    // 9割から上限までを理想範囲として表示
    if (currentCount >= targetCount) {
        progressBar.className = 'progress-bar-fill red';
        progressStatus.className = 'progress-status over';
        progressStatus.textContent = `文字数オーバー！（${currentCount - targetCount}文字超過）`;
    } else if (currentCount >= ninetyPercent) {
        progressBar.className = 'progress-bar-fill green';
        progressStatus.className = 'progress-status achieved';
        progressStatus.textContent = '9割達成！';
    } else {
        progressBar.className = 'progress-bar-fill yellow';
        progressStatus.className = 'progress-status';
        progressStatus.textContent = `9割ラインまであと${remaining}文字`;
    }

    // ゴールマーカーの位置を更新
    goalMarker.style.left = '90%';

}

function countText(text) {
    switch (countModeInput.value) {
        case 'no-newlines':
            return text.replace(/[\r\n]/g, '').length;
        case 'no-whitespace':
            return text.replace(/\s/g, '').length;
        default:
            return text.length;
    }
}

function saveDraft() {
    const drafts = getDrafts();
    const draft = {
        text: esTextarea.value,
        targetCount: targetCountInput.value,
        countMode: countModeInput.value,
        savedAt: new Date().toISOString()
    };
    drafts.unshift(draft);
    localStorage.setItem(DRAFT_STORAGE_KEY, JSON.stringify(drafts));
    renderDraftOptions(drafts);
    showStatusMessage('一時保存しました', 'success');
}

function getDrafts() {
    try {
        const savedDrafts = JSON.parse(localStorage.getItem(DRAFT_STORAGE_KEY) || '[]');
        return Array.isArray(savedDrafts) ? savedDrafts : [];
    } catch (error) {
        localStorage.removeItem(DRAFT_STORAGE_KEY);
        return [];
    }
}

function renderDraftOptions(drafts = getDrafts()) {
    draftSelect.replaceChildren(new Option('保存した文章を選択', ''));
    draftCount.textContent = `保存件数: ${drafts.length}件`;
    drafts.forEach((draft, index) => {
        const savedAt = new Date(draft.savedAt).toLocaleString('ja-JP');
        const preview = draft.text.replace(/\s+/g, ' ').slice(0, 30) || '(空の文章)';
        draftSelect.add(new Option(`${savedAt} - ${preview}`, String(index)));
    });
    loadDraftBtn.disabled = draftSelect.value === '';
}

function restoreDraft(index) {
    const draft = getDrafts()[index];
    if (!draft) {
        return;
    }

    esTextarea.value = typeof draft.text === 'string' ? draft.text : '';
    if (draft.targetCount) {
        targetCountInput.value = draft.targetCount;
    }
    if (['all', 'no-newlines', 'no-whitespace'].includes(draft.countMode)) {
        countModeInput.value = draft.countMode;
    }
    updateProgress();
    showStatusMessage('保存した文章を呼び出しました', 'success');
}

async function copyText(text, label) {
    if (!text) {
        showStatusMessage('コピーする文章がありません', 'error');
        return;
    }

    try {
        await navigator.clipboard.writeText(text);
        showStatusMessage(`${label}をコピーしました`, 'success');
    } catch (error) {
        showStatusMessage('コピーに失敗しました。ブラウザの権限を確認してください', 'error');
    }
}

/**
 * イベントリスナー登録
 */
targetCountInput.addEventListener('change', updateProgress);
targetCountInput.addEventListener('input', updateProgress);
countModeInput.addEventListener('change', updateProgress);
esTextarea.addEventListener('input', updateProgress);
saveBtn.addEventListener('click', () => {
    if (!esTextarea.value.trim()) {
        showStatusMessage('保存する文章を入力してください', 'error');
        return;
    }
    saveDraft();
});
copyTextBtn.addEventListener('click', () => copyText(esTextarea.value, '本文'));
draftSelect.addEventListener('change', () => {
    loadDraftBtn.disabled = draftSelect.value === '';
});
loadDraftBtn.addEventListener('click', () => restoreDraft(Number(draftSelect.value)));

/**
 * ステータスメッセージを表示
 * @param {string} message - 表示するメッセージ
 * @param {string} type - メッセージタイプ（'success', 'error', 'loading'）
 */
function showStatusMessage(message, type = 'success') {
    const statusEl = document.getElementById('statusMessage');
    statusEl.textContent = message;
    statusEl.className = `status-message show ${type}`;
    
    if (type !== 'loading') {
        setTimeout(() => {
            statusEl.classList.remove('show');
        }, 4000);
    }
}

renderDraftOptions();
updateProgress();
console.log('✅ 文章カウントアシスト初期化完了');
