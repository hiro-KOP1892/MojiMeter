# 文章カウントアシスト

就活生のES（エントリーシート）作成に特化したWebツール。
指定文字数の9割（360字/400字など）を確実に達成させるUIと、AIが提案する文字数調整機能を提供します。

## 🎯 主な機能

- **9割到達プログレスバー**: 目標文字数の9割までの進捗を可視化
- **リアルタイム文字数カウント**: 入力と同時に文字数をカウント
- **AI文字数調整**: Google Gemini APIを使用した自動文字数調整提案
- **レスポンシブデザイン**: PCからモバイルまで対応

## 🛠 技術スタック

- **フロントエンド**: HTML5, CSS3, Vanilla JavaScript
- **バックエンド**: Java 17+, Spring Boot 3.3
- **外部API**: Google Generative AI (Gemini 1.5 Flash)
- **ビルドツール**: Maven
- **デプロイ**: Render (予定)

## 📦 プロジェクト構成

```
es-helper-ai/
├── pom.xml                          # Maven設定
├── README.md                        # このファイル
├── .gitignore
│
├── src/
│   ├── main/
│   │   ├── java/com/jobhunt/eshelper/
│   │   │   ├── EsHelperAiApplication.java      # Main Application
│   │   │   ├── config/
│   │   │   │   └── GeminiConfig.java           # Gemini API設定
│   │   │   ├── controller/
│   │   │   │   └── TextAdjustController.java   # REST APIコントローラー
│   │   │   ├── service/
│   │   │   │   └── GeminiService.java          # AI連携サービス
│   │   │   ├── dto/
│   │   │   │   ├── AdjustRequest.java          # リクエストDTO
│   │   │   │   └── AdjustResponse.java         # レスポンスDTO
│   │   │   └── exception/
│   │   │       └── GeminiApiException.java     # 例外クラス
│   │   │
│   │   └── resources/
│   │       ├── application.yml                 # アプリケーション設定
│   │       ├── application-dev.yml             # 開発環境設定
│   │       └── static/
│   │           ├── index.html                  # メインページ
│   │           ├── css/
│   │           │   └── style.css               # スタイルシート
│   │           └── js/
│   │               └── app.js                  # フロントエンドJS
│   │
│   └── test/java/com/jobhunt/eshelper/
│       └── EsHelperAiApplicationTests.java
│
└── docs/
    └── API_SPEC.md                  # API仕様書
```

## 🚀 セットアップ

### 前提条件
- Java 17以上
- Maven 3.8以上
- Git

### インストール手順

1. **リポジトリをクローン**
```bash
git clone <repository-url>
cd es-helper-ai
```

2. **依存関係をダウンロード**
```bash
mvn clean install
```

3. **環境変数を設定**
```bash
# Gemini APIキーを設定（必須）
export GEMINI_API_KEY="your-gemini-api-key-here"
```

> **注意**: APIキーは環境変数で管理し、コードには直接記述しないでください

4. **アプリケーションを起動**
```bash
mvn spring-boot:run
```

5. **ブラウザでアクセス**
```
http://localhost:8080
```

## 📝 使用方法

1. **上限文字数を入力** (例: 400)
2. **ES本文をテキストエリアに入力**
3. **リアルタイムで文字数と進捗を確認**
4. **文字数カウント方式を選択**（改行・空白を数える／数えない）
5. **「一時保存」で本文を履歴としてブラウザに保存**
6. **保存履歴から文章を選び、「呼び出す」で復元**
7. **「本文をコピー」で本文をクリップボードへコピー**

## 🔌 API仕様

### テキスト調整エンドポイント

**エンドポイント**: `POST /api/adjust`

**リクエストボディ**:
```json
{
  "text": "ES本文のテキスト...",
  "targetWordCount": 400,
  "action": "EXPAND",
  "diff": 30
}
```

**レスポンス**:
```json
{
  "suggestedText": "調整済みのテキスト...",
  "adjustedCount": 430,
  "success": true,
  "errorMessage": null
}
```

詳細は [docs/API_SPEC.md](docs/API_SPEC.md) を参照してください。

## ⚙️ 設定

### application.yml

```yaml
spring:
  application:
    name: es-helper-ai

gemini:
  api:
    key: ${GEMINI_API_KEY:dummy-key-for-development}
    model: gemini-1.5-flash

server:
  port: 8080
```

## 🔐 セキュリティ

- **APIキーは環境変数で管理**: `GEMINI_API_KEY`
- **フロントエンドには秘密鍵を露出させない**
- **全てのAPI呼び出しはバックエンド経由**

## 📚 今後の実装予定

- [ ] データベースへの履歴保存
- [ ] ユーザー認証・管理機能
- [ ] ES修正履歴の保管
- [ ] 複数のES形式テンプレート対応
- [ ] Renderへのデプロイ設定

## 🐛 トラブルシューティング

### ポート8080が既に使用されている場合
```bash
# 別のポートで起動
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Gemini APIエラーが表示される場合
1. APIキーが正しく設定されているか確認
2. APIキーが有効期限切れでないか確認
3. ネットワーク接続を確認

## 📄 ライセンス

MIT License

## 👨‍💻 開発者向け情報

### ログレベル設定

開発時はDEBUGレベルで詳細なログが出力されます：
```yaml
logging:
  level:
    com.jobhunt.eshelper: DEBUG
```

### テスト実行
```bash
mvn test
```

## 📞 サポート

問題が発生した場合は、GitHubのIssueセクションで報告してください。

---

**最終更新**: 2026年9月
