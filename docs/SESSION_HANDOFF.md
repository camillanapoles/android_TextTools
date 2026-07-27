# TextTools X — Handoff de Sessão (Checkpoint)

Última atualização: 2026-07-27 · Branch: `cnmfs/texttools-x` · 6 commits à frente de `cnmfs/merged-workflow`

## TL;DR

Fork-and-refactor de `corphish/TextTools` transformado em **TextTools X**: um
engine de ações dinâmicas onde **funcionalidade = objeto** (JSON), e o APK é um
runtime estável. Adicionar/ativar/desativar/editar ações **não exige rebuild**.
O app **está instalado e funcionando** no device (assinado com a chave
`cnmfs-release`), com launcher dinâmico de 12 ações verificado via adb.

## 1. Contexto e arquitetura

- **Repo**: fork de `corphish/TextTools` em `github.com/camillanapoles/android_TextTools`.
- **Padrão**: interpretador / data-driven. APK = runtime; `TextAction` (objeto JSON em `filesDir/textactions.json`) = a funcionalidade.
- **Stack**: Kotlin 2.4 / AGP 9.2.1 / Gradle 9.4.1 / Compose BOM 2026.06 / Hilt / KSP / compileSdk+targetSdk 37, minSdk 29.
- **Especificação**: `docs/spec/engineering-requirements.md` (ER) + `docs/spec/software-design.md` (SD).
- **Skill**: `skills/texttools-extend/SKILL.md` (local ao repo — conhecimento reusável).

### Camadas implementadas
- `actions/model/` — `Enums` (ActionType, ResultMode, Source), `TextAction` (objeto + serde org.json).
- `actions/ActionStore.kt` — persistência JSON + seed de 12 ações + `ActionSeeder`.
- `actions/handler/` — `ActionHandler` (interface), `HandlerRegistry`, e 5 handlers: `TransformHandler` (40+ ops via `TextFunctions`), `ExtractHandler` (regex + presets), `FindReplaceHandler`, `TemplateHandler`, `AnalyzeHandler`.
- `actions/ActionIcons.kt` — resolve iconKey → drawable.
- `activities/OptionsActivity.kt` — **o launcher**: recebe `PROCESS_TEXT`, renderiza a lista dinâmica via `setContent`, executa a ação escolhida inline e devolve o resultado ao sistema (replace in-place).
- `activities/ManagerActivity.kt` + `ActionEditorActivity.kt` + `DisplayResultActivity.kt` — gestão CRUD + criar/editar + mostrar análise.
- `viewmodels/{Launcher,Manager}ViewModel.kt` — `@HiltViewModel`.
- `TextToolsApplication.kt` — força `AppMode.SINGLE` no `onCreate` (fix crítico, ver §5).
- `MainActivity.kt` — adicionado card "TextTools X → Manage Actions".

### ActionType (enum — fronteira compile-time)
Implementados: `TRANSFORM, EXTRACT, FIND_REPLACE, TEMPLATE, ANALYZE`.
Reservados (próximas fases, mesmo registry): `TRANSLATE, SCRIPT, AI_PROMPT, PIPELINE`.

## 2. Histórico de ações (commits)

```
0a4f62c fix(app): add setContent import + use literal PROCESS_TEXT_RESULT extra
35c3f18 fix(app): OptionsActivity renders launcher inline (no for-result delegation)
f555523 fix(app): force SINGLE mode on startup (defeat restored legacy MULTI pref)
b5743ed fix(app): missing imports (fillMaxWidth, Column) + material-icons-core dep
4eca182 fix(app): restore compileSdk/targetSdk 37 (Compose 1.12 AAR requires it)
d21cc33 feat(app): TextTools X dynamic actions engine (feature-as-object)
```

### Jornada de debug (resumo)
1. Implementação completa (data layer + UI + wiring) commitada.
2. **Build local no Termux falha**: Gradle 9.x quebra no Termux (`Service 'SystemInfo' is not available` — incompatibilidade com bionic libc; Gradle 8.x funciona). → Solução: **build via CI** (`.github/workflows/debug-apk.yml`, ubuntu onde Gradle 9 roda).
3. CI: 3 ciclos de fix (compileSdk 37, imports + material-icons-core, setContent + literal extra).
4. APK buildeia (debug). Re-assinado com chave `cnmfs-release` via `apksigner`.
5. **Sintoma "só Text Template funciona"**: diagnosticado via **adb** — `context_menu_mode=MULTI` **restaurado da nuvem** (instalação anterior do corphish TextTools, mesmo package + `allowBackup=true`) fazia o app habilitar os 9 aliases legados e desabilitar o `OptionsActivity`. → Fixes: forçar SINGLE no `Application.onCreate` + `OptionsActivity` renderiza o launcher inline.
6. Verificado via adb: launcher renderiza, ação executa sem crash.

## 3. Estado atual (verificado)

| Item | Estado |
|---|---|
| Branch | `cnmfs/texttools-x`, 6 commits, pushed em `origin` |
| CI build | verde (run `30301607179`, artifact `texttools-debug-apk`) |
| APK no device | instalado, **assinado cnmfs-release**, v2.2.3 |
| Store | 12 ações seedeadas, todas `enabled:true` |
| Modo | `SINGLE` (forçado no startup) |
| Launcher | renderiza (`OptionsActivity` em foreground), **sem crash** |
| Execução de ação | verificada (tap → handler roda → finaliza limpo) |
| Replace in-place | não verificado visualmente (precisa editor real; código devolve em `EXTRA_PROCESS_TEXT` + `EXTRA_PROCESS_TEXT_RESULT`) |
| Working tree | só `gradlew` modificado (mode bit, não commitar) |

## 4. Como buildar / assinar / instalar / testar

> Gradle 9 **não roda neste Termux**. Sempre buildar via CI.

```bash
# 1. Push pra origin/cnmfs/texttools-x dispara .github/workflows/debug-apk.yml
git push origin cnmfs/texttools-x

# 2. Baixar artifact
gh run download <RUN_ID> -D $TMPDIR/ttx

# 3. Re-assinar com cnmfs-release (debug APK -> release-signed)
APK=$(find $TMPDIR/ttx -name "*.apk")
apksigner sign \
  --ks ~/.cnmfs-keystore/cnmfs-keystore.jks \
  --ks-key-alias cnmfs-release \
  --ks-pass pass:"$(cat ~/.cnmfs-keystore/.pass.tmp)" \
  --key-pass pass:"$(cat ~/.cnmfs-keystore/.pass.tmp)" \
  --out TextTools-X-release.apk "$APK"
apksigner verify --verbose TextTools-X-release.apk

# 4. Instalar (mesma sig cnmfs -> -r; sig diferente -> uninstall antes)
adb -s 127.0.0.1:5555 install -r TextTools-X-release.apk
```

### Conexão adb local (Termux → próprio Android)
```bash
adb connect 127.0.0.1:5555   # adbd escuta em 5555 neste device
adb -s 127.0.0.1:5555 shell id   # uid=2000(shell) se autorizado
```

### Testes via adb
```bash
D=127.0.0.1:5555; P=com.corphish.quicktools
# Ver store real (app é debuggable -> run-as funciona)
adb -s $D shell run-as $P cat files/textactions.json
# Ver modo
adb -s $D shell run-as $P grep context_menu_mode shared_prefs/com.corphish.quicktools_preferences.xml
# Disparar PROCESS_TEXT direto no launcher
adb -s $D shell "am start -a android.intent.action.PROCESS_TEXT -t text/plain \
  -n $P/.activities.OptionsActivity --es android.intent.extra.PROCESS_TEXT hello123"
# Foreground / crash
adb -s $D shell dumpsys activity activities | grep topResumedActivity
adb -s $D logcat -d | grep -iE 'FATAL|AndroidRuntime|Caused'
```
**Limitação adb**: `pm enable/disable` de componentes lança `SecurityException` (shell uid 2000 não pode). Só o próprio app alterna via `switchModeTo`.

## 5. Gotchas críticos (operacionais)

1. **Gradle 9 no Termux**: quebra (`SystemInfo`). Buildar só via CI. O template `~/cnmfs-app` (Gradle 8.2) funciona local, mas o toolchain 2026 do TextTools exige AGP 9 → Gradle 9.
2. **`aapt2FromMavenOverride`**: O `gradle.properties` do repo **NÃO** tem (quebraria CI). O override do aapt2 do Termux vive em `~/.gradle/gradle.properties` (nível usuário, não commitado).
3. **Backup restaurado de pref MULTI**: instalações anteriores do corphish TextTools (mesmo package) têm o pref `MULTI` restaurado via Google Auto Backup. Mitigação: `TextToolsApplication.onCreate` força `SINGLE`. Se voltar a acontecer, confirmar com `run-as ... grep context_menu_mode`.
4. **Delegação for-result frágil**: `OptionsActivity` (translucent) NÃO delega pra outra activity — renderiza o launcher inline (padrão original). Não reintroduzir `router.launch(LauncherActivity)`.
5. **`EXTRA_PROCESS_TEXT_RESULT` não resolveu como constante** neste setup — uso o literal `"android.intent.extra.PROCESS_TEXT_RESULT"`.
6. **`LauncherActivity.kt` está dormente** (não referenciado; `OptionsActivity` faz tudo). Pode ser removido numa limpeza.
7. **9 aliases legados + Activities legados permanecem** no código/manifest (mode SINGLE os desabilita). Limpeza completa = remover os 9 `*ActivityAlias` + `*Activity` legados + `Feature.LIST` + `AppMode` (fase de refactor futura).

## 6. Instruções de continuidade (NOVA SESSÃO)

Antes de qualquer coisa:
1. `cd ~/android_TextTools && git checkout cnmfs/texttools-x && git pull`.
2. Ler (nesta ordem):
   - `skills/texttools-extend/SKILL.md` (seção "Evolution: TextTools X")
   - `docs/spec/engineering-requirements.md` (o quê)
   - `docs/spec/software-design.md` (o como)
   - **Este arquivo** (`docs/SESSION_HANDOFF.md`)
3. Confirmar estado: `git log --oneline cnmfs/merged-workflow..HEAD` (esperado: 6 commits até `0a4f62c`).
4. Confirmar device: `adb connect 127.0.0.1:5555 && adb -s 127.0.0.1:5555 shell run-as com.corphish.quicktools cat files/textactions.json` (12 ações).

**Regra de build**: NUNCA rodar `./gradlew` localmente neste Termux (Gradle 9 quebra). Sempre: `git push` → aguardar CI `debug-apk` → `gh run download` → `apksigner sign` (cnmfs) → `adb install -r`.

**Regra de git**: stage paths explícitos (nunca `git add -A`); só commitar seus arquivos; `gradlew` (mode bit) **não** commitar.

## 7. Roadmap (próximas fases — plugam no mesmo `HandlerRegistry`)

- **Fase 3 — IA generator**: `AI_PROMPT` handler (backend OpenAI-compat configurável) + tela "descrever → IA gera TextAction → preview → salvar". Decisão aberta: provider (OpenAI/OpenRouter/Gemini/DeepSeek/on-device).
- **Fase 4 — SCRIPT + Skills**: `SCRIPT` handler (QuickJS sandbox) + skill codec (import/export JSON) + `PIPELINE`.
- **Fase 5 — TRANSLATE**: `TRANSLATE` handler com ML Kit on-device (exemplo-marco: traduz e substitui in-place).
- **Limpeza**: remover legados (aliases/activities/Feature.LIST/AppMode) + `LauncherActivity.kt` dormente.
- **Release**: bump versionCode + `changelogs/vX.Y.Z.txt`; assinatura release via secrets no CI (`.github/workflows/release.yml`).

## 8. Decisões pendentes do usuário (ainda abertas)

1. Backend de IA: OpenAI-compat configurável (recomendado)?
2. Script engine: QuickJS (recomendado)?
3. Confirmar verificação visual do replace in-place num editor real (WhatsApp/Keep).
