# ES Helper AI - API仕様書

## 概要

ES Helper AIのバックエンドが提供するREST APIの完全な仕様です。

## ベースURL

```
http://localhost:8080
```

---

## エンドポイント一覧

### 1. ヘルスチェック

**エンドポイント**: `GET /api/health`

**説明**: アプリケーションが正常に動作しているか確認するためのエンドポイント

**レスポンス**:
```json
"ES Helper AI is running ✅"
```

**ステータスコード**: `200 OK`

---

### 2. テキスト文字数調整

**エンドポイント**: `POST /api/adjust`

**説明**: ES文章をAIで調整し、指定文字数に合わせた修正案を生成します

#### リクエスト

**ヘッダー**:
```
Content-Type: application/json
```

**ボディ**:
```json
{
  "text": "string (必須)",
  "targetWordCount": "integer (必須)",
  "action": "string (必須: 'EXPAND' または 'SHRINK')",
  "diff": "integer (必須: 正の整数)"
}
```

**パラメータ詳細**:

| パラメータ | 型 | 必須 | 説明 | 例 |
|-----------|-----|------|------|-----|
| text | string | ✅ | 調整対象のES本文 | "私は〜という経験から..." |
| targetWordCount | integer | ✅ | ES本文の上限文字数 | 400 |
| action | string | ✅ | 調整の方向性（拡大/縮小） | "EXPAND" / "SHRINK" |
| diff | integer | ✅ | 調整する文字数 | 30, 20 |

#### レスポンス（成功時）

**ステータスコード**: `200 OK`

```json
{
  "suggestedText": "string",
  "adjustedCount": "integer",
  "success": true,
  "errorMessage": null
}
```

**レスポンスパラメータ**:

| パラメータ | 型 | 説明 |
|-----------|-----|------|
| suggestedText | string | AIが生成した修正案のテキスト |
| adjustedCount | integer | 修正後の実際の文字数 |
| success | boolean | 処理成功フラグ（常にtrue） |
| errorMessage | null | エラーメッセージ（成功時はnull） |

#### レスポンス（エラー時）

**ステータスコード**: `200 OK` (処理は成功するがsuccess=false)

```json
{
  "suggestedText": null,
  "adjustedCount": null,
  "success": false,
  "errorMessage": "エラーの詳細説明"
}
```

#### 使用例

**リクエスト例**:
```bash
curl -X POST http://localhost:8080/api/adjust \
  -H "Content-Type: application/json" \
  -d '{
    "text": "私はこれまでの人生で多くの挑戦をしてきました。その中で最も印象的な経験は、大学でのプロジェクトです。",
    "targetWordCount": 400,
    "action": "EXPAND",
    "diff": 30
  }'
```

**レスポンス例**:
```json
{
  "suggestedText": "私はこれまでの人生で多くの挑戦をしてきました。その中で最も印象的な経験は、大学でのプロジェクトです。このプロジェクトを通じて、チームワークの大切さと問題解決能力を培いました。",
  "adjustedCount": 431,
  "success": true,
  "errorMessage": null
}
```

---

## エラーハンドリング

### エラーパターン

1. **テキストが空の場合**
```json
{
  "success": false,
  "errorMessage": "文字数調整処理に失敗しました: テキストが空です"
}
```

2. **目標文字数が無効な場合**
```json
{
  "success": false,
  "errorMessage": "文字数調整処理に失敗しました: 目標文字数が無効です"
}
```

3. **変更文字数が無効な場合**
```json
{
  "success": false,
  "errorMessage": "文字数調整処理に失敗しました: 変更文字数が無効です"
}
```

4. **Gemini API呼び出しエラー**
```json
{
  "success": false,
  "errorMessage": "文字数調整処理に失敗しました: APIキーが無効です"
}
```

---

## リクエスト/レスポンスの制約

### リクエスト

- `text`: 最小1文字、最大100,000文字
- `targetWordCount`: 100 ～ 10,000
- `action`: 厳密に `"EXPAND"` または `"SHRINK"` (大文字)
- `diff`: 1 ～ 1,000

### レスポンス

- `adjustedCount`: 実際の修正後の文字数（`diff`と異なる場合もあります）
- `suggestedText`: 最大100,000文字

---

## フロントエンド実装例

### JavaScriptでの呼び出し

```javascript
async function adjustText() {
  const response = await fetch('/api/adjust', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      text: document.getElementById('esText').value,
      targetWordCount: 400,
      action: 'EXPAND',
      diff: 30
    })
  });

  const data = await response.json();

  if (data.success) {
    console.log('修正案:', data.suggestedText);
    console.log('文字数:', data.adjustedCount);
  } else {
    console.error('エラー:', data.errorMessage);
  }
}
```

---

## レート制限

現在、レート制限は実装されていません。
本番環境へのデプロイ時に追加予定です。

---

## CORS設定

フロントエンドとバックエンドが同じオリジンにホストされているため、CORS設定は不要です。

---

## セキュリティに関する注意

⚠️ **重要**: Gemini APIキーは以下の方針に従い管理してください

- **環境変数で管理**: `GEMINI_API_KEY`
- **コードには直接記述しない**
- **フロントエンドに露出させない**
- **本番環境で定期的にキーをローテーション**

---

## バージョン履歴

| バージョン | リリース日 | 変更内容 |
|-----------|----------|--------|
| 1.0.0 | 2026-09-01 | 初版リリース |

---

## 参考資料

- [Spring Boot公式ドキュメント](https://spring.io/projects/spring-boot)
- [Google Generative AI SDK](https://ai.google.dev/tutorials/python_quickstart)
- [REST API設計ベストプラクティス](https://restfulapi.net/)

---

**最終更新**: 2026年9月1日
