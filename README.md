# VCMC Welcome Plugin

VCMCサーバー向けのカスタムウェルカムメッセージプラグインです。プレイヤーがサーバーに参加した際に、装飾されたメッセージ、クリック可能なリンク、サウンドなどを提供します。

## 概要 (Overview)

このプラグインは、プレイヤーがサーバーにログインした数秒後（設定可能）に、カスタマイズ可能なウェルカムメッセージをチャットに送信します。[MiniMessage](https://docs.advntr.dev/minimessage/format.html)形式を採用しており、リッチなテキスト表現（色、太字、ホバーイベント、クリックアクションなど）が可能です。また、ゲーム内コマンドから直接メッセージや設定を変更・確認することができます。

## 機能 (Features)

- **ウェルカムメッセージの自動送信**: サーバー参加時に自動的にメッセージを表示します。
- **遅延表示**: ログイン直後のチャットログ流出を防ぐため、指定した時間（Tick単位）遅延させてメッセージを表示できます。
- **サウンド再生**: メッセージ表示時に指定したサウンドを再生します。
- **リッチテキスト対応**: MiniMessageを使用した柔軟なテキスト装飾（公式HPへのクリッカブルリンクやホバーテキストを含む）。
- **プレースホルダー**: `<player>`（または `{player}`）タグでプレイヤー名を動的に置き換えます。
- **インゲーム設定管理**: コマンドを使用して、ゲーム内から直接メッセージ内容の変更、確認、リロードが可能です。

## コマンド (Commands)

| コマンド | 説明 | 権限 |
|----------|------|------|
| `/welcome` | VCMCWelcomeのコマンドヘルプを表示します。 | `vcmcwelcome.admin` |
| `/welcome reload` | `config.yml` の設定を再読み込みします。 | `vcmcwelcome.admin` |
| `/welcome set <キー> <値>` | 指定したキーの設定値を変更し、保存します。 | `vcmcwelcome.admin` |
| `/welcome get <キー>` | 指定したキーの現在の設定値を確認します。 | `vcmcwelcome.admin` |

**設定可能なキー一覧 (`set` / `get` コマンド用):**
`welcome`, `separator`, `url-label`, `url-hover`, `discord-warning`, `discord-note`, `url`, `delay`

## パーミッション (Permissions)

| パーミッション | デフォルト | 説明 |
|----------------|------------|------|
| `vcmcwelcome.admin` | OP | `/welcome` コマンドの全機能を利用できる管理者権限です。 |

## 設定 (Configuration)

プラグイン初回起動時に生成される `config.yml` で詳細な設定が可能です。ゲーム内の `/welcome set` コマンドを使用した場合も、このファイルに自動的に保存されます。

```yaml
# 参加後にメッセージを表示するまでの遅延 (20 ticks = 1秒)
delay-ticks: 20

# 公式HP の URL (クリック可能なリンクとして表示されます)
url: "https://www.vcmc.jp/"

# サウンド設定
sound:
  # サウンド名 (Minecraftのサウンドキー)
  name: "entity.player.levelup"
  volume: 1.0
  pitch: 1.0

# メッセージ設定 (MiniMessage 形式)
messages:
  separator: "<dark_gray><st>--------------------------------------------</st>"
  welcome: "<yellow><bold>ようこそ！ <aqua><player></aqua><white> さん"
  url-label: "<green><bold>[公式HP] <white>クリックしてアクセス:"
  url-hover: "クリックしてHPを開く"
  discord-warning: "<red><bold>【重要】 <gold>Discordサーバーへの参加は <dark_red><u>必須</u></dark_red><gold> です！"
  discord-note: "<gray>※HP内に招待リンクがあります"
```

## コード構成 (Code Structure/Architecture)

このプラグインは主に以下の2つのクラスで構成されており、Paper API (Kyori Adventure API) を活用しています。

- **`WelcomePlugin.java`** (`jp.vcmc.welcome.WelcomePlugin`)
  - プラグインのメインクラス (`JavaPlugin` を継承)。
  - プラグインの有効化/無効化時の処理、イベントリスナーの登録。
  - `/welcome` コマンドの処理 (`CommandExecutor`) とタブ補完 (`TabCompleter`) を担当。
  - configファイルへの設定の保存・読み込みのロジックを管理。

- **`JoinListener.java`** (`jp.vcmc.welcome.JoinListener`)
  - プレイヤー参加イベント (`PlayerJoinEvent`) を監視するリスナークラス。
  - プレイヤーログイン時に遅延タスク (Scheduler) をスケジュール。
  - Kyori Adventure API (MiniMessage) を使用して、プレースホルダーの置換、サウンドの再生、クリッカブルなURLコンポーネントの構築、メッセージの送信を実行します。
