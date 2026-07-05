# 使ったプロンプトと評価

演習中に Codex CLI に投げた主要なプロンプトを記録する。
`EXERCISES.html` に記載の機能実装や、修正提案に関するプロンプトを記述する。

## 書き方

- どのフェーズで使ったか (例: 投稿一覧、いいね機能、リファクタ)
- 実際に投げたプロンプト本文 (省略せずコピペ)
- 結果: 効いた / 部分的に効いた / 効かなかった
- 振り返り: 次に同じ状況が来たらどう変えるか

---

## プロンプト 1(サンプル)

**フェーズ**: サンプルデータ(シーダー)の追加

**プロンプト本文**:

```
src\main\resources\db\migration\V1__init.sql に基づくH2用のシーダーを作成し、実行する。テストデータとして10個程度のシードデータを作成。H2で起動時に自動投入する。
```

**結果**: 効いた

```
• 提案どおり、H2 プロファイル起動時だけ seed が自動適用される構成にしました。

  差分:

  - src/main/resources/application-h2.yml:20: H2 の Flyway location に classpath:db/seed/h2 を追加。
  - src/main/resources/db/seed/h2/R__seed_posts_h2.sql:6: posts に 10 件投入する H2 専用 repeatable migration を追加。posts_seq は次
    ID が 11 になるよう更新。

  - src/test/resources/application-h2.yml:17: テストは空 DB 前提を維持するため seed location を外す設定を追加。
  - src/test/java/com/example/tsubuyaki/db/H2SeedMigrationTest.java:18: seed 適用で 10 件入ることと、次 sequence 値が 11 になることを
    検証。

  検証:

  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Dtest=H2SeedMigrationTest test 成功
  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Ph2 spring-boot:run "-Dspring-boot.run.arguments=--spring.main.web-
    application-type=none" 成功。ログで Migrating schema "PUBLIC" with repeatable migration "seed posts h2" を確認。

  - .\mvnw -B -Dmaven.repo.local=C:/home/codex/.m2/repository -Ph2 verify 成功。警告: Rule violated for bundle tsubuyaki-board: lines
    covered ratio is 0.48, but expected minimum is 0.60。日本語要約: 現在のカバレッジは 48% で 60% 未満ですが、このプロファイルでは
    fail 設定ではないためビルドは成功しています。
```

**振り返り**:

シーダーは追加できた。
検証方法を指定しなかったためハーネスであらかじめ定義された検証方法を自動的に起動した。
CI検証は成功しているが、実際に画面表示の確認はできていない(未実装のため)。

---

## プロンプト 2

**フェーズ**:

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 3

**フェーズ**:

**プロンプト本文**:

```

```

**結果**: 効いた / 部分的に効いた / 効かなかった

**振り返り**:

---

## プロンプト 4 以降

3 件目以降も追加可能。書ければ書くほど良い。

---

## プロンプト 1

**フェーズ **: M1 投稿一覧機能

**プロンプト本文**:

```
# タスク: M1 投稿一覧機能 (GET /posts) の TDD 実装

Spring Boot (Java) と Thymeleaf を使用して、以下の仕様を満たす「投稿一覧表示機能」を実装してください。
まずは提示したテストケースを満たすテストコードを実装し、その後、テストをパスする最小限の製品コードを構築します。

## 1. 満たすべき仕様 (受入基準)

### A. バックエンド (Controller / Service / Repository)
- **エンドポイント**: `GET /posts`
- **データ取得ルール**: 
  - データベースから投稿データを取得する際、**新着順 (`created_at DESC`)** でソートすること。
  - 取得件数は **最大 50 件** に制限すること（51件以上あっても50件のみ返す）。
- **Modelへの格納**: 
  - `model` の `posts` 属性 (`model.addAttribute("posts", ...)`）に、取得した投稿の `List<Post>` を格納すること。
- **画面遷移**: 
  - 遷移先として `posts/list`（Thymeleafテンプレート）を返すこと。

### B. フロントエンド (View: `src/main/resources/templates/posts/list.html`)
- **0件時の表示**: 投稿が 0 件の場合、画面に「まだ投稿はありません」という文言を表示する。
- **データ存在時の表示**: 
  - 投稿が存在する場合、各投稿を **「投稿者」「内容」「投稿日」の順** に表示する。
  - 「内容」のテキストは、画面幅に応じて **適度な文字数で自動的に折り返す** ようにCSS（`word-break: break-all;` や `white-space: pre-wrap;` など）を適切に適用すること。
- **更新ボタン**: 
  - 画面内に「更新」ボタンを設けること。
  - ボタンを押下した際、`posts/` (または `/posts`) にリクエストを送り、ページがリロードされる挙動にすること。

## 2. 必須テストケース (最初に実装すること)
製品コードを実装する前に、以下の4つの観点を検証する統合テスト（MockMvc等を使用）を作成してください。

1. **0件時の挙動テスト**: 投稿が0件の場合に、画面内に「まだ投稿はありません」という文言が含まれていること。
2. **50件上限のテスト**: データベースに51件以上の投稿が存在する場合、画面（またはModel内）に新着の50件しか含まれていないこと。
3. **更新ボタンの検証**: 画面内に更新ボタンが存在し、その遷移先またはアクションが `posts/` へのリクエストになっていること。
4. **表示順・項目の検証**: 取得された投稿が、画面上で「投稿者」「内容」「投稿日」の順番で正しく配置・表示されていること。

## 3. 進め方の指示
1. まず、上記「2. 必須テストケース」を満たすテストコードのモックまたは実装案を提示してください。
2. テストコードの合意が取れたら、それをパスするための最小限の製品コード（Repository -> Service -> Controller -> HTML）をステップ・バイ・ステップで実装してください。一気にすべてのコードを書き換えず、確認を取りながら進めてください。
```

**結果**: 効いた 


**振り返り**:
実際の一覧画面に投稿内容の表示確認はできていない(未実装のため)。

---

## プロンプト 2

**フェーズ**: M2 投稿作成フォーム表示機能

**プロンプト本文**:

```
# タスク: M2 投稿作成フォーム表示機能 (GET /posts/new) の TDD 実装

  Spring Boot (Java) と Thymeleaf を使用して、以下の仕様を満たす「投稿作成フォーム画面の表示機能」を実装してください。
  まずは提示したテストケースを満たすテストコードを実装し、その後、テストをパスする最小限の製品コードを構築します。
  ※なお、投稿の登録処理（POST /posts）は別タスクとするため、今回は含めません。

  ### A. バックエンド (Controller)
  - **エンドポイント**: `GET /posts/new`
  - **フォームオブジェクトの準備**:

  ### B. フロントエンド (View: `src/main/resources/templates/posts/form.html`)
  - **入力フォームの配置**:
    - 画面内に「投稿者（author）」と「内容（content）」を入力するための `<form>` 要素を配置すること。
    - 各入力項目は、バックエンドから渡された `postForm` オブジェクトのプロパティと正しくバインド（`th:field` 等を使用）できるように構築すること。
  - **戻るリンク**:
    - フォーム画面内から、投稿一覧画面（`/posts`）へ戻るためのリンク（`<a>` タグ等）またはボタンを設けること。

  ## 2. 必須テストケース (最初に実装すること)
  製品コードを実装する前に、以下の2つの観点を検証する統合テスト（MockMvc等を使用）を作成してください。

  1. **画面表示およびModelの検証**:
     - `GET /posts/new` にアクセスした際、ステータス200でフォーム画面が返ってくること。
     - 遷移先のビュー名が `posts/form` であること。
     - Modelに空のFormオブジェクト（`postForm`）が正しく設定されていること。
  2. **HTML要素の存在検証**:
     - 表示されたHTML内に、「投稿者」および「内容」の入力項目（`<input>` や `<textarea>` 等）が存在すること。
     - 投稿一覧（`/posts`）へ戻るための正確なリンク（`href="/posts"` など）が存在すること。

  ## 3. 進め方の指示
  1. まず、上記「2. 必須テストケース」を満たすテストコードの実装案を提示してください。
  2. テストコードの合意が取れたら、それをパスするための最小限の製品コード（Formクラス -> Controller -> `posts/form.html` テンプレートの枠組み）をステッ
  プ・バイ・ステップで実装してください。
```

**結果**: 効いた 


**振り返り**:
 現状の PostForm は body プロパティだが、プロンプトにて content としてしまったため、
 不要な差分が発生したため、元のbodyへ戻した。

---

## プロンプト 3

**フェーズ**: M3: 投稿登録

**プロンプト本文**:

```
# タスク: M3 投稿登録機能 (POST /posts) の TDD 実装

Spring Boot (Java) と Thymeleaf を使用して、以下の厳密な仕様を満たす「投稿登録機能」を実装してください。
すでに実装済みの M2 で定義した `PostForm`（プロパティ: `author`, `body`）を活用し、まずは仕様を満たすテストケースを構築してから、製品コードの実装に進みます。

## 1. 満たすべき仕様 (受入基準)

### A. バックエンド (Controller / Service / Repository)
- **エンドポイント**: `POST /posts`
- **引数の受け取り**: `PostForm` オブジェクトを `@ModelAttribute` 経由で受け取り、同時にバリデーション（`@Valid` または `@Validated`）を実行すること。
- **バリデーションルール (厳密)**:
  - **投稿者 (`author`)**: `1文字以上、30文字以下` であること。かつ、**空白（スペース）のみの入力はNG** とすること。
  - **内容 (`body`)**: `1文字以上、280文字以下` であること。かつ、**空白（スペース）のみの入力はNG** とすること。
  - ※実装時は `@NotBlank`, `@Size(min = 1, max = 30)`, `@Size(min = 1, max = 280)` などのアノテーションを適切に使用してください。
- **正常系（成功時）の処理挙動**:
  - バリデーションを通過した場合、データをデータベースに保存すること。
  - 保存完了後は、**HTTPステータス「302（Found）」** で、**投稿一覧画面（`/posts`）へリダイレクト（PRGパターン）** させること。
- **異常系（失敗時）の処理挙動**: 
  - 入力値にエラーがある（`BindingResult.hasErrors()` が true）場合は、データベースへの保存を行わないこと。
  - **HTTPステータス「200（OK）」** を返し、投稿作成画面のビュー（**`posts/form`**）を再表示すること。

### B. フロントエンド (View: `src/main/resources/templates/posts/form.html` の修正)
- **エラーメッセージの表示**: 
  - バリデーションエラーが発生して画面が再表示された際、`author` および `body` の各入力項目の近くに、エラーメッセージが日本語で表示されるように Thymeleaf の `th:errors` などを配置すること。

## 2. 必須テストケース (最初に実装すること)
製品コードを実装・修正する前に、以下の観点を検証する統合テスト（MockMvc等を使用）を作成してください。

1. **正常系登録・リダイレクトの検証 (302)**: 
   - `POST /posts` に有効なパラメータ（例: 境界値である1文字の入力や、上限以内の文字列）を送信した際、**HTTPステータスが 302** であること。
   - リダイレクト先のURLが `/posts` であること。
2. **異常系（バリデーションエラー）の検証 (200)**: 
   - 以下のケースを送信した際、**HTTPステータスが 200** で返り、遷移先のビュー名が `posts/form` であること。また、`BindingResult` にエラーが含まれていること。
     - `author` が空、または空白スペースのみ、または31文字以上
     - `body` が空、または空白スペースのみ、または281文字以上

## 3. 進め方の指示
1. まず、上記「2. 必須テストケース」を満たすテストコードの実装案（MockMvcの `.andExpect(status().isFound())` や `isOk()` などのアサーションを含む）を提示してください。
2. テストコードの合意が取れたら、それをパスするための最小限の製品コードをステップ・バイ・ステップで実装・修正してください。
```

**結果**: 効いた

```
• M3投稿登録機能をTDDで実装しました。

  追加・変更内容:

  - 追加: src/test/java/com/example/tsubuyaki/controller/PostCreateIntegrationTest.java
      - 正常系: POST /posts が 302 で /posts へリダイレクトし、DB保存されること
      - create(PostForm) を追加し、Post を保存

  - 変更: src/main/java/com/example/tsubuyaki/controller/PostController.java
      - @PostMapping("/posts") を追加

  検証:

  - ./mvnw -B -Ph2 clean verify
      - Tests: 19件成功
      - Checkstyle: 0件
      - SpotBugs: 0件
  - 実HTTP確認
      - 正常POST: 302 http://localhost:8080/posts
      - 異常POST: 200、エラーメッセージ表示
      - 一覧に登録内容が表示されることを確認済み

  確認用サーバーは停止済みです。セットアップ系ファイルの既存変更は今回触っていません。
```

**振り返り**:
 投稿実行時、Oracleとの日付型が合わないエラーが発生。エラー内容をCodexに展開し、修正した。
 OracleはTIMESTAMP(6)、Javaはjava.time.Instant となっていたため、java.time.LocalDateTimeに修正

---

## プロンプト 4

**フェーズ**: M4: 投稿詳細

**プロンプト本文**:

```
# タスク: M4 投稿詳細機能 (GET /posts/{id}) の TDD 実装

Spring Boot (Java) と Thymeleaf を使用して、以下の仕様を満たす「投稿詳細表示機能」を実装してください。
テスト駆動開発 (TDD) のアプローチに則り、まずは仕様を満たすテストケースを構築し、それをパスする最小限の製品コードを段階的に構築します。

また、今回はコードの理解を深めるため、**バックエンドおよびフロントエンドの修正箇所・新規実装箇所には、処理内容が明確にわかる日本語のコメントを必ず記述**してください。

## 1. 満たすべき仕様 (受入基準)

### A. バックエンド (Controller / Service / Repository)
- **エンドポイント**: `GET /posts/{id}`（`{id}` は各投稿のプライマリキー）
- **正常系の挙動 (データが存在する場合)**:
  - パス変数（Path Variable）から `id` を受け取り、データベースから該当する投稿データを1件取得すること。
  - `model` の `post` 属性 (`model.addAttribute("post", ...)`）に、取得した `Post` エンティティを格納すること。
  - 遷移先のビューとして **`posts/detail`**（Thymeleafテンプレート: `posts/detail.html`）を返すこと。
- **異常系の挙動 (指定された id が存在しない場合)**:
  - データベースに該当する `id` の投稿が存在しない場合は、**HTTPステータス「404 (Not Found)」** をブラウザに返すこと。
  - ※カスタム例外（例: `ResourceNotFoundException`）をスローし、`@ResponseStatus(HttpStatus.NOT_FOUND)` や `@ControllerAdvice` でハンドリングする実装を推奨します。

### B. フロントエンド (View: `src/main/resources/templates/posts/detail.html`)
- **詳細情報の表示**: 
  - 画面内に、対象の投稿の「投稿者（author）」「内容（body）」「投稿日（created_at）」を表示すること。
  - 「内容」は M1 同様、画面幅に応じて適度な文字数で自動的に折り返すようにCSSを適切に適用すること。
- **戻るリンク**: 
  - 詳細画面内から、投稿一覧画面（`/posts`）へ戻るためのリンク（`<a>` タグ等）を設けること。

### C. 実装コードへの記述ルール (必須)
- 新しく追加・修正するJavaコード（Controller, Service, Repository等）およびHTMLテンプレート内の処理ブロックには、**どのような意図や処理を行っているかが一目でわかる日本語コメント**を必ず含めてください。

## 2. 必須テストケース (最初に実装すること)
製品コードを実装・修正する前に、以下の2つの観点を検証する統合テスト（MockMvc等を使用）を作成してください。

1. **正常系（データ存在時）の検証 (200)**: 
   - 存在する `id`（例: `1`）を指定して `GET /posts/1` にアクセスした際、**HTTPステータスが 200 (OK)** であること。
   - 遷移先のビュー名が `posts/detail` であること。
   - Modelに指定した投稿データ（`post`）が正しく格納されていること。
2. **異常系（データ不在時）の検証 (404)**: 
   - 存在しない `id`（例: `999`）を指定して `GET /posts/999` にアクセスした際、**HTTPステータスが 404 (Not Found)** で返ってくること。

## 3. 進め方の指示
1. まず、上記「2. 必須テストケース」を満たすテストコードの実装案（MockMvcの `.andExpect(status().isOk())` や `isNotFound()` などのアサーション、および日本語コメントを含む骨組み）を提示してください。
2. テストコードの合意が取れたら、それをパスするための最小限の製品コードをステップ・バイ・ステップで実装・修正してください。すべての修正箇所に分かりやすい日本語コメントが入っていることを確認しながら進めます。
```

**結果**: 効いた 

```
• 実装しました。M4の GET /posts/{id} は、存在する投稿なら posts/detail に model.post を渡し、存在しないIDなら PostNotFoundException 経由で404を返します。

  変更箇所:

  - src/test/java/com/example/tsubuyaki/controller/PostDetailIntegrationTest.java:27: 200正常系、404異常系、表示内容、一覧戻りリンクを検証
  - src/main/java/com/example/tsubuyaki/controller/PostController.java:35: GET /posts/{id} を追加
  - src/main/java/com/example/tsubuyaki/service/PostService.java:26: ID検索と未存在時例外を追加
  - src/main/java/com/example/tsubuyaki/service/PostNotFoundException.java:6: 404返却用例外を追加
  - src/main/resources/templates/posts/detail.html:1: 投稿者、本文、投稿日、一覧リンクを表示

  検証:

  - RED確認: /posts/{id} 未実装時に Status expected:<200> but was:<404> で失敗することを確認。
  - GREEN確認: ./mvnw -Dtest=PostDetailIntegrationTest -DforkCount=0 test は BUILD SUCCESS、2件成功。
  - 全体確認: ./mvnw -B -Ph2 -DforkCount=0 verify は BUILD SUCCESS、21件成功、Checkstyle違反0、SpotBugs警告0。
```

**振り返り**:
 /posts/{id}で投稿詳細画面が表示され、存在しないidで404となることを確認した。
 404の場合はエラー画面が表示されることが望ましいが、現時点では未実装。

---

## プロンプト 5

**フェーズ**: S1: いいね

**プロンプト本文**:

```
/plan 「S1: いいね」機能をTDDサイクルで実装する計画を作ってください。
@education/EXERCISES.md の「S1: いいね」要件、および @AGENTS.md の開発規約を受入基準とします。
まず、作成予定のテーブル定義やハッシュ生成ロジックのコンポーネント配置などの実装計画を組み立ててください。

### 1. 求める機能（ゴール）
- `POST /posts/{id}/likes` へのリクエストで、いいねの状態をトグル（登録/解除）させてください。
  - **成功時の挙動**: トグル処理が完了した後は、**HTTPステータス「302（Found）」** で、元の投稿詳細画面（`redirect:/posts/{id}`）へリダイレクトさせてください。
- 同一の clientHash が再度ボタンを押した場合はいいねを解除します。
  - ※ clientHash 仕様: 接続元の「IPアドレス + User-Agent」を SHA-256 でハッシュ化した文字列の先頭 8 文字。
  - バックエンドでは `HttpServletRequest` からこれらを取得してロジックを組み立ててください。
- 投稿詳細画面（@src/main/resources/templates/posts/detail.html）に、現在のいいね総数と、いいねをリクエストする Like ボタン（フォーム）を表示してください。
- 存在しない id へのリクエストは 404 としてください。

### 2. 実装の制約・手順
- **データモデル拡張**: いいね情報を永続化するため、新しい Flyway マイグレーション DDL（例: src/main/resources/db/migration/V2__create_likes.sql）を H2/Oracle 両対応で作成し、対応する JPA Entity、Repository を作成してください。
- **アーキテクチャ階層**: Controller -> Service -> Repository -> Entity の一方向依存、および Constructor Injection を徹底してください。
- **TDDサイクル**: 最初に @WebMvcTest や @DataJpaTest を用いて、期待する挙動が失敗するテスト（RED）を1本書き、それを GREEN にする最小実装を行い、最後にリファクタリング（REFACTOR）を行ってください。
  - Controllerのテストでは、MockMvcでリモートIPアドレスや `User-Agent` ヘッダーを擬似的に設定し、ハッシュ生成ロジックが正しく検証できるテストケースを含めてください。
- **セキュリティ**:
  - Thymeleaf テンプレートでは th:utext を絶対に使わず、th:text を使用してください。
  - Like ボタンのフォームには必ず Spring Security の CSRF トークン（_csrf）を含めてください。
  - クエリ発行時は SQL インジェクション対策として必ずバインド変数を使用してください。
- **コードコメント**: 今回はコードの理解を深めるため、**バックエンドおよびフロントエンドの修正箇所・新規実装箇所には、処理内容が明確にわかる日本語のコメントを必ず記述**してください。

### 3. 完了条件
- `./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify` を実行し、BUILD SUCCESS になること（カバレッジ 80% 以上を維持）。
- Repository / Service / Controller それぞれに1本以上のテストが存在すること。
```

**結果**: 効いた 

```
• S1「いいね」機能を GREEN まで実装しました。

  主な追加・変更:

  - Like 永続化: src/main/resources/db/migration/V2__create_post_likes.sql:1, src/main/java/com/example/tsubuyaki/domain/
    PostLike.java:1, src/main/java/com/example/tsubuyaki/repository/PostLikeRepository.java:1

  - Controller / 詳細画面: src/main/java/com/example/tsubuyaki/controller/PostController.java:1, src/main/resources/templates/
    posts/detail.html:1

  - Controller: src/test/java/com/example/tsubuyaki/controller/PostLikeIntegrationTest.java:1
  - Repository: src/test/java/com/example/tsubuyaki/repository/PostLikeRepositoryTest.java:1
  - Service: src/test/java/com/example/tsubuyaki/service/PostLikeServiceTest.java:1
  - clientHash: src/test/java/com/example/tsubuyaki/service/ClientHashGeneratorTest.java:1

  確認結果:

  ./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify

  結果は BUILD SUCCESS です。テストは 30件成功 / 失敗0 / エラー0、Checkstyle 違反 0、SpotBugs 0、JaCoCo も All coverage checks
  have been met. まで確認済みです。

---
• 進めてセルフレビューまで確認しました。追加修正は不要です。

  確認済み:

  - th:utext なし
  - 文字列連結の @Query / native SQL なし
  - Like フォームに _csrf あり
  - Controller → Service → Repository → Entity の依存方向維持
  - Repository / Service / Controller テストあり
  - ./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify は BUILD SUCCESS
```

**振り返り**:
 /posts/{id}/likes 投稿詳細画面のLikeボタン押下で「いいね」が増減し、
 別ブラウザからの押下で、増加することを確認した。

---

CodexでAI 駆動開発研修を行っていました。EXERCISESの社内つぶやきボード 機能開発全部が終わっていないため、Antigravityで追加機能（S2: キーワード検索）以降を引き継ぎたいです。
まず、educationフォルダ内の確認及びAGENTS.mdを確認して、Antigravityで引き継ぐ方法を検討してください。ビルド、環境が起動できるならUbuntuは入れなくても良いです。

---

## プロンプト 6

**フェーズ**: S2: キーワード検索

**プロンプト本文**:

```
/plan 「S2: キーワード検索」機能をTDDサイクルで実装する計画を作ってください。
@education/EXERCISES.md の「S2: キーワード検索」要件、および @AGENTS.md の開発規約を受入基準とします。
まず、影響を受ける既存のコントローラーやリポジトリの改修方針、およびSQLインジェクション対策の設計計画を組み立ててください。

### 1. 求める機能（ゴール）
- 投稿一覧画面（@src/main/resources/templates/posts/list.html）に、キーワード入力用の検索ボックス（`<form>`）を配置してください。
- `GET /posts?q=検索キーワード` のリクエストを受け付け、投稿本文（body）に対する部分一致（LIKE検索）を行って結果を一覧表示してください。
  - **ソートと件数制限**: 検索結果も従来の全件表示と同様に、**新着順（`created_at DESC`）で最大 50 件**を返す仕様としてください。
- 検索キーワード（`q`）が未指定、空文字、または**全角・半角スペースのみ**の場合は、検索処理は行わず、従来の全件新着順表示を維持してください（バックエンド側で適切にトリミング処理を行うこと）。
- **検索結果が0件の場合**: 画面に「該当する投稿は見つかりませんでした」（またはM1共通の「まだ投稿はありません」など、プロジェクトの既存仕様に合わせた文言）を表示するように制御してください。

### 2. 実装の制約・手順
- **アーキテクチャとクエリ設計**: 
  - 検索ロジックは既存の Controller -> Service -> Repository の階層構造に則って実装してください。
  - SQLインジェクションを防ぐため、Repository層では文字列連結によるJPQL/SQL構築を厳禁とし、Spring Data JPAの派生クエリ（例: `findByBodyContainingOrderByCreatedAtDesc` など）を最優先で利用するか、`@Query` を使う場合は必ずバインド変数（名前付きパラメータ）を使用してください。
- **TDDサイクル**: 最初に検索クエリの有無や境界値に応じた挙動を検証する失敗テスト（RED）をテストクラスに書き、それを GREEN にする最小実装を行い、最後にリファクタリング（REFACTOR）を行ってください。
  - テストケースには「キーワードが空の場合」「部分一致でヒットする場合」「ヒットせず0件になる場合」を含めてください。
- **セキュリティ・UI**:
  - Thymeleaf テンプレートでの `th:utext` の使用を禁止し、必ず `th:text` を使用してXSSを保護してください。
  - 検索フォーム（GETリクエスト）の入力値を画面に再表示（リテイン）させる際、`model.addAttribute("query", q)` などの属性名で安全にバインドし、検索ボックスに値が残るようにしてください。
- **コードコメント**: 今回はコードの理解を深めるため、**バックエンドおよびフロントエンドの修正箇所・新規実装箇所には、処理内容が明確にわかる日本語のコメントを必ず記述**してください。

### 3. 完了条件
- `./mvnw -B -Ph2 -Pcoverage-day3 -Pstrict verify` を実行し、BUILD SUCCESS になること（カバレッジ 80% 以上を維持）。
- キーワード検索に関する正常系・異常系のテストが適切に存在すること。

```

**結果**: 効いた 

```
「S2: キーワード検索」機能の TDD 開発（GREENフェーズ・REFACTORフェーズ・最終VERIFYフェーズ）がすべて完了いたしました。

実施した検証および成果は以下の通りです。

各層（Repository、Service、Controller、HTMLテンプレート）に、設計通りのキーワード検索ロジック、トリミング・フォールバック機能、0件時メッセージの切り替え、検索文字のリテイン処理、および詳細な日本語コメントを実装しました。
最終検証コマンド（$env:JAVA_HOME="E:\Program Files\Android\Android Studio\jbr"; .\mvnw.cmd -B -Ph2 -Pcoverage-day3 -Pstrict verify）を実行し、以下の要件を満たして BUILD SUCCESS になることを確認しました。
テスト件数が合計40件に増加し、すべて正常にパスしています。
仕上げ用の厳格な静的解析（Checkstyle, SpotBugs）にすべて合格しています。
JaCoCoのカバレッジが80%以上の基準（coverage-day3）をクリアしています。
```

**振り返り**:
 Antigravityにて追加機能の実装「S2: キーワード検索」が行えることを確認
 「AGENTS.md」は効いている。

---

## プロンプト 7

**フェーズ**: S3: 投稿者名フィールド拡張

**プロンプト本文**:

```
「S3: 投稿者名フィールド拡張」について検討してください。
　→カラーを10色としてください（提案は5色程度）
　→カラーテーマを後から変更しても対応できるようにRGBカラーで保存してください。
```

**結果**: 効いた

```
「S3: 投稿者名フィールド拡張（簡易アバター色選択機能）」機能について、テスト（48件）・静的解析・カバレッジがすべて正常にパスし、BUILD SUCCESS を獲得して完了いたしました。

実施した検証および成果は以下の通りです。

Flywayマイグレーション（V3__add_color_to_posts.sql）を作成し、postsテーブルに color カラム（デフォルト値 #000000）を追加しました。
ドメイン（Post）および DTO（PostForm）に color フィールドを追加し、入力画面（form.html）に10色の選択肢（ブラック #000000 が初期選択）を追加、不正な形式での送信を検証するバリデーションを設定しました。
一覧・詳細画面にて、DBから取得したRGBカラーコードを th:style にインラインスタイルとして動的適用することで、将来のテーマや色定義の変更にもCSSを修正せずDB値のみで対応可能な構造を実装しました。またアバター枠をCSS（app.css）に定義しました。
最終検証コマンド（$env:JAVA_HOME="E:\Program Files\Android\Android Studio\jbr"; .\mvnw.cmd -B -Ph2 -Pcoverage-day3 -Pstrict verify）が正常にパスすることを確認しました。
```

**振り返り**:
 簡易的な指示でも、壁打ち後、問題なく機能拡張できた。
 デザインの変更も実施。
---

## プロンプト 8

**フェーズ**: C1: タグ機能

**プロンプト本文**:

```
COULDの「C1: タグ機能」について検討してください。

 投稿作成時（create メソッド）に、本文（body）からハッシュタグをパースするロジックを組み込みます。
  ハッシュタグの抽出正規表現：
→投稿作成時、タグ追加のテキストボックスから追加する機能としたい

 すでにDBに存在するタグは再利用（TagRepository.findByName で検索）
  存在しないタグは新規登録
  投稿オブジェクトの tags に関連...
→追加したタグが自分が投稿した合わない場合は削除したいので、削除案も検討してください。
```

**結果**: 効いた

```
COULD選択課題の「C1: タグ機能」（登録・個別解除、浮いたタグの自動物理削除対応）に関するすべてのGREENフェーズの実装（Tag/TagRepository/TagController/例外の新規作成、Post/PostForm/PostService/各テンプレートの改修）を完了いたしました。
現在、すべてのテストがパスし、カバレッジ基準（80%以上 -Pstrict -Pcoverage-day3）をクリアするかどうかの品質検証ビルドをバックグラウンドで開始しております。
ビルドが完了するまでしばらくお待ちください。
COULD選択課題「C1: タグ機能」（登録・個別解除、浮いたタグの自動物理削除対応）の実装および、厳格な品質検証（テスト57件のパス、静的解析エラーゼロ、カバレッジ80%以上クリア）がすべて完了いたしました。

変更点はすべて git にステージング（git add）済みとなっております。
詳細な変更内容については 
walkthrough.md
 をご参照ください。
動作確認をされる場合は、以下の起動コマンドを実行してください。
PowerShell の場合: $env:JAVA_HOME="E:\Program Files\Android\Android Studio\jbr" .\mvnw.cmd -Ph2 spring-boot:run
コマンドプロンプトの場合: set JAVA_HOME=E:\Program Files\Android\Android Studio\jbr mvnw.cmd -Ph2 spring-boot:run
```

**振り返り**:
 投稿作成時タグの追加が可能、詳細画面でタグの削除が可能なことを確認した。

---

## プロンプト 9

**フェーズ**: C2: 投稿削除

**プロンプト本文**:

```
COULDの「C2: 投稿削除」について検討してください。

```

**結果**: 効いた

```
COULD選択課題の「C2: 投稿削除（論理削除）」の GREEN（実装）フェーズおよび厳格な品質検証（テスト63件すべてパス、静的解析エラーゼロ、カバレッジ80%以上クリア）がすべて完了いたしました。
変更点はすべて git にステージング（git add）済みとなっております。
→削除の確認をポップアップではなく画面での確認としたい

投稿詳細画面上での「インライン削除確認UI」の実装、および検証ビルド（テスト63件パス、静的解析エラーゼロ、カバレッジ80%以上クリア）がすべて完了いたしました。
変更内容はすべて git にステージング（git add）済みとなっております。

```

**振り返り**:
 削除機能について追加で以下を依頼し、問題なく実装できた。
```
以下内容を追加したいので検討してください。
・削除した投稿を戻すために、「ごみ箱」を一覧画面上部に追加、「ごみ箱」押下で削除された一覧が表示できる削除一覧画面を作成。削除一覧画面にて削除された投稿を戻せるようにする。
また、削除一覧画面にてごみ箱を空にするアイコンを作成し、押下すると、削除一覧にも出ないようにする（論理削除）
```

---

## プロンプト 10

**フェーズ**: C3: REST API

**プロンプト本文**:

```
COULDの「C3: REST API」について検討してください。
```

**結果**: 効いた

```
COULDの「C3: REST API」について、詳細な実装計画を検討し、 
implementation_plan.md
 に追加（更新）いたしました。

提案内容の要約：

REST API の実装:
GET /api/posts エンドポイントを @RestController で新規追加し、論理削除・完全削除されていない最新の投稿50件を JSON 形式で返却します。
JSONシリアライズ用に PostResponse DTOを作成し、投稿ID、投稿者、本文、アバター色、投稿日時（ISO-8601形式文字列）、および関連するタグ名のリストを返却します。
簡易 OpenAPI ドキュメントの提供:
静的な openapi.json （OpenAPI 3.0形式の仕様定義ファイル）を src/main/resources/static/openapi.json に配置します。
GET /api-docs エンドポイントを定義し、この仕様定義ファイルを application/json としてクライアントへ配信します。
セキュリティ設定（SecurityConfig.java）の修正:
/api/posts と /api-docs に認証なしでアクセスできるよう requestMatchers().permitAll() に追加します。
APIクライアントからの呼び出しに対応するため、/api/** へのCSRF保護を無効化（除外）します。
検証計画:
MockMvcを使用した PostApiIntegrationTest を新規作成し、JSONレスポンスの妥当性、削除済みデータ除外の検証、および /api-docs の配信検証を実施します。
こちらの計画で進めてよろしければ、TDDサイクルに沿ってREDフェーズ（失敗するテストの追加、およびAPI仕様書の作成）から開始いたします。ご指示をお願いいたします。

→レスポンスJSONに削除された投稿も取得できるようにしたい。以下を検討していますが、その他いい案ありますか？
指定なしは、有効な投稿のみ取得「/api/posts」
all指定で削除済みも取得「/api/posts?all」 
レスポンスに「deleted_at：int(未削除=0、削除済み=1、ごみ箱から削除済み=2)」追加

```

**振り返り**:
 削除された投稿も「/api/posts?all=true」で取得できるようにした。

---

## プロンプト 11

**フェーズ**: その他 投稿編集機能

**プロンプト本文**:

```
投稿編集機能を追加したいです。投稿者以外は編集できないようにしてください
```

**結果**: 効いた / 部分的に効いた / 効かなかった

```
投稿者本人制限付きの投稿編集機能、編集日時（edited_at）の記録・画面表示、および REST API / OpenAPI 仕様書（editedAt フィールドの追加）のグリーンフェーズ実装と検証がすべて完了いたしました。

実施した内容
データベースマイグレーション:
posts テーブルに投稿作成者判定用の client_hash および編集日時を保持する edited_at カラムを追加するマイグレーションを適用しました。
ビジネスロジックと DTO:
Post エンティティおよび PostResponse DTO にフィールドとゲッターを追加し、投稿本文とアバター色の更新ロジック、および API レスポンスへの編集日時マッピングを実装しました。
PostService に本人制限の検証（ハッシュ判定）およびタグの編集（浮いたタグの自動クリーンアップを含む）を実装しました。
コントローラーおよび画面:
PostController に編集用エンドポイント（GET/POST /posts/{id}/edit）を実装しました。
新規に投稿編集用テンプレート（edit.html）を作成し、詳細画面（detail.html）に「編集する」ボタン（作成者本人にのみ表示）および「（編集済み: 日時）」の表示領域を追加しました。
API仕様書:
openapi.json に editedAt フィールドの定義を追加しました。
テストと検証:
新たに PostEditIntegrationTest などのテストを加え、全80件のテストがパスし、Checkstyle/SpotBugs エラーなし、JaCoCo テスト行カバレッジ80%以上を維持した状態でビルド成功することを確認しました。
変更ファイルはすべて git add でステージング済みです。
詳細な設計と実施内容は 
walkthrough.md
 および 
task.md
 をご確認ください。

確認およびコミット・プッシュの実行をお願いいたします。
```

**振り返り**:
 投稿者のみ編集できる投稿編集機能を追加。

---

