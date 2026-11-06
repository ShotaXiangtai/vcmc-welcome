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
- **サウンド再生**: メッセージの表示時にサウンドを再生します。`sound.name` を空にすると鳴らしません。
- **リッチテキスト対応**: [MiniMessage](https://docs.advntr.dev/minimessage/format.html) でメッセージを装飾できます。
- **プレースホルダー**: `<player>`（または `{player}`）がプレイヤー名に置き換わります。
- **インゲーム設定管理**: コマンドからメッセージの変更、確認、再読み込みができます。

## コマンド

| コマンド | 説明 |
|---|---|
| `/welcome` | ヘルプを表示します |
| `/welcome reload` | `config.yml` を再読み込みします |
| `/welcome set <キー> <値>` | 設定を変更して保存します |
| `/welcome get <キー>` | 現在の設定値を、タグも含めて入力したとおりに表示します |

**設定可能なキー:** `welcome`, `separator`, `url-label`, `url-hover`, `discord-warning`, `discord-note`, `url`, `delay`

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
```

## コード構成

- `WelcomePlugin.kt`: メインクラス。Brigadier でのコマンド登録と、設定の変更・保存を行います。
- `JoinListener.kt`: 参加を検知して、プレイヤーのリージョンでメッセージとサウンドを送ります。
- `WelcomeSettings.kt`: `config.yml` の内容。

## ビルド

Java 25 が必要です。

```
mvn package
```

`target/VCMCWelcome-<バージョン>.jar` が生成されます。
