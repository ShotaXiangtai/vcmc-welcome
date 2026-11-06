# VCMC Welcome Plugin

プレイヤーがサーバーに参加したときに、装飾されたメッセージ、クリックできるリンク、サウンドを届ける Folia 26.3 向けプラグインです。

## 動作環境

| 項目 | バージョン |
|---|---|
| サーバー | Folia 26.3（`folia-api 26.3.build.6-alpha` でビルド） |
| Java | 25 以上 |

## 機能

- **ウェルカムメッセージの自動送信**: サーバー参加時にメッセージを表示します。
- **遅延表示**: 指定した時間（tick 単位）遅らせて表示できます。
- **メニューの案内**: メインサーバーで `/menu` を使えることを案内します。以前の `config.yml` にキーが無くても既定の文面で表示します。
- **サウンド再生**: メッセージの表示時にサウンドを再生します。`sound.name` を空にすると鳴らしません。
- **リッチテキスト対応**: [MiniMessage](https://docs.advntr.dev/minimessage/format.html) でメッセージを装飾できます。
- **プレースホルダー**: `<player>`（または `{player}`）がプレイヤー名に置き換わります。
- **インゲーム設定管理**: コマンドからメッセージの変更、確認、再読み込みができます。

## コマンド

| コマンド | 説明 |
|---|---|
| `/welcome` | ヘルプを表示します |
| `/welcome reload` | `config.yml` と `lang` フォルダを再読み込みします |
| `/welcome set <キー> <値>` | 日本語の文面や設定を変更して保存します |
| `/welcome get <キー>` | 日本語の文面や設定の現在の値を、タグも含めて入力したとおりに表示します |

**設定可能なキー:** `welcome`, `separator`, `url-label`, `url-hover`, `discord-warning`, `discord-note`, `menu-note`, `url`, `delay`

`url` は `http://` か `https://` で始めてください。`delay` は 1 以上の整数です。

## パーミッション

| パーミッション | デフォルト | 説明 |
|---|---|---|
| `vcmc.welcome.admin` | OP | `/welcome` の全機能を使えます |
| `vcmcwelcome.admin` | OP | `vcmc.welcome.admin` の別名です |

## 設定

初回起動時に生成される `config.yml` で設定できます。`/welcome set` で変更した内容もこのファイルに保存されます。

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
  welcome: "<yellow><bold>ようこそ！<aqua><player></aqua><white> さん"
  url-label: "<green><bold>[公式HP] <white>クリックしてアクセス:"
  url-hover: "クリックしてHPを開く"
  discord-warning: "<red><bold>【重要】<gold>Discordサーバーへの参加は <dark_red><u>必須</u></dark_red><gold> です！"
  discord-note: "<gray>※HP内に招待リンクがあります"
  # メインサーバーの /menu の案内。空文字にすると表示しません
  menu-note: "<green><bold>[メニュー]</bold> <white>メインサーバーでは <yellow>/menu</yellow> で、よく使うコマンドをボタンから実行できます"
```

## 多言語対応

プレイヤーのクライアントの言語設定に合わせて、メッセージを自動で切り替えます。ゲーム中に言語を変更すると、次のメッセージから反映されます。

| クライアントの言語 | 表示する言語 | 文面の場所 |
|---|---|---|
| 日本語 | 日本語 | `config.yml` の `messages` |
| English（各地域） | English | `lang/en.yml` |
| 简体中文 | 简体中文 | `lang/zh_cn.yml` |
| 繁體中文（台灣）、文言 | 繁體中文（台灣） | `lang/zh_tw.yml` |
| 繁體中文（香港） | 繁體中文（香港） | `lang/zh_hk.yml` |
| 上記以外 | English | `lang/en.yml` |

繁體中文（香港）の文面は広東語で書いています。コンソールへのメッセージとログは日本語のままです。

`lang` フォルダのファイルは初回起動時に作られます。編集して `/welcome reload` を実行すると反映されます。ファイルに無いキーは jar 内の既定の文面を使います。

`/welcome set` と `/welcome get` が扱うのは日本語の文面です。ほかの言語の文面は `lang` フォルダのファイルを編集してください。

## コード構成

- `WelcomePlugin.kt`: メインクラス。Brigadier でのコマンド登録と、設定の変更・保存を行います。
- `JoinListener.kt`: 参加を検知して、プレイヤーのリージョンでメッセージとサウンドを送ります。
- `WelcomeSettings.kt`: `config.yml` の内容。
- `Messages.kt`, `i18n/`: 受け取る人の言語で文面を作ります。

## ビルド

Java 25 が必要です。

```
mvn package
```

`target/VCMCWelcome-<バージョン>.jar` が生成されます。
