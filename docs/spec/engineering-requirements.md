# TextTools X — Engenharia de Requisitos (ER)

| Campo | Valor |
|---|---|
| Status | Draft para implementação |
| Data | 2026-07-27 |
| Base | Fork-and-refactor de `corphish/TextTools` v2.2.3 (`com.corphish.quicktools`) |
| Suíte | minSdk 29 / targetSdk 37 / Kotlin JVM 17 / Compose + Hilt |

## 1. Contexto e Problema

O TextTools atual injeta ferramentas no **menu de seleção de texto** do Android via
`intent-filter` `android.intent.action.PROCESS_TEXT`. Cada funcionalidade é uma
**feature compilada no APK**: `Feature` (catálogo estático) + `Activity` própria +
`activity-alias` no manifest + `UseCase` + `Repository` + `functions`. Adicionar uma
feature exige **edição de 9 arquivos + rebuild + reinstalação**.

**Problema**: o usuário quer crescimento ilimitado (ações, geração por IA, skills)
**sem rebuild por nova funcionalidade**, com gestão absoluta (criar/ativar/
desativar/reordenar/editar/importar/exportar) a partir de objetos em runtime.

## 2. Objetivos / Não-objetivos

### Objetivos
- **O-1** Quantidade **ilimitada** de ações gerenciáveis sem rebuild.
- **O-2** Gestão absoluta: CRUD, ativar/desativar, reordenar, duplicar, testar.
- **O-3** Geração de ações por IA (descrição natural → ação válida).
- **O-4** Sistema de skills (importar/exportar/compartilhar/encadear).
- **O-5** Substituição in-place do texto selecionado quando o campo é editável.
- **O-6** Reusar o esqueleto do TextTools (DI, theme, manifest, toggle, transforms).

### Não-objetivos (escopo explícito fora)
- N-O1 Carregar código nativo (DEX) em runtime — frágil e restrito no Android 14+.
- N-O2 Publicar na Play Store (distribuição por sideload/F-Droid/CI).
- N-O3 Suporte a iOS/desktop.
- N-O4 Entradas **renomeáveis dinamicamente** direto no menu copiar/colar
  (impossível no Android — ver §3).

## 3. Restrições "hard" do Android (não-negociáveis, fundam a auditoria)

| # | Restrição | Implicação no design |
|---|---|---|
| R1 | Itens do menu de seleção = `activity-alias` declarados **estaticamente** no `AndroidManifest.xml` em compile-time. | **Não existe** "número ilimitado de itens diretos no menu". |
| R2 | `android:label` de `activity-alias`/`intent-filter` é **recurso imutável** em runtime. | Não dá pra renomear itens de menu dinamicamente. |
| R3 | `PackageManager.setComponentEnabledSetting` só **liga/desliga** aliases existentes. | Mecanismo de toggle é reusável, mas não cria entradas novas. |
| R4 | `Intent.EXTRA_PROCESS_TEXT` (entrada) + `EXTRA_PROCESS_TEXT_READONLY` (flag) + `setResult(RESULT_OK, intent.putExtra(EXTRA_PROCESS_TEXT_RESULT, texto))` (saída) é o **único** canal de substituição in-place. | Substituição só funciona em campos **editáveis**; não-editáveis → clipboard. |

**Conclusão da auditoria**: a dinamidade ilimitada **só pode viver dentro de um launcher**
(1 item de menu fixo → Activity que lista ações lidas de um DB). O "número de itens no
menu" é **sempre fixo** (≥1). O "número de ações" é **ilimitado** porque mora no DB.

## 4. Personas

| Persona | Necessidade |
|---|---|
| **Usuário power** | Muitas ações rápidas; criar as suas; nunca reinstalar. |
| **Curador** | Importar skills de outros; organizar por tags; compartilhar. |
| **Criador assistido** | Descrever em linguagem natural → IA gera a ação. |

## 5. Requisitos Funcionais (testáveis)

### Menu de contexto e launcher
- **FR-1** O app expõe **exatamente uma** entrada no menu de seleção de texto
  ("Quick Actions") via `activity-alias` com `PROCESS_TEXT` + `mimeType=text/plain`.
- **FR-2** Ao acionar, abre `LauncherActivity` que lista as `TextAction` habilitadas,
  ordenadas por `order`, lidas do DB em runtime.
- **FR-3** O usuário pode **ocultar/mostrar** a entrada inteira (toggle do alias via
  `PackageManager`), persistido em prefs.

### Modelo de ação (o "objeto")
- **FR-4** Toda funcionalidade é uma instância de `TextAction` persistida (Room),
  **não** código compilado.
- **FR-5** Criar/editar/duplicar/excluir uma `TextAction` **não exige rebuild**.
- **FR-6** Ativar/desativar muda só o campo `enabled`; ação desativada não aparece no launcher.
- **FR-7** Reordenar muda só `order`; reflete imediatamente no launcher.

### Execução
- **FR-8** Ao escolher uma ação, o `HandlerRegistry` resolve o `ActionHandler` pelo
  `type` e executa com o `configJson` parseado.
- **FR-9** `resultMode` define o destino: `REPLACE` (substitui in-place se editável,
  senão clipboard), `COPY` (clipboard), `DISPLAY` (mostra resultado), `SHARE` (Intent `ACTION_SEND`).
- **FR-10** Ações longas (IA, script) mostram progresso e são canceláveis.

### Geração por IA
- **FR-11** Tela "Gerar com IA": usuário descreve → backend de IA retorna `TextAction`
  JSON válido → **preview com teste real** sobre texto de exemplo → salvar no DB.
- **FR-12** A ação gerada entra no launcher como qualquer outra; **zero rebuild**.

### Skills
- **FR-13** Skill = pacote serializável (JSON) de 1+ `TextAction` (+ metadados).
- **FR-14** Importar skill de arquivo/clipboard/URL → valida schema → insere no DB.
- **FR-15** Exportar seleção/todas as ações como skill (arquivo em storage compartilhado).
- **FR-16** **Pipeline**: skill cujas ações executam em cadeia (saída de A → entrada de B).

### Gestão (Manager UI)
- **FR-17** Tela de gestão: lista todas as ações com toggle, editar, reordenar (drag), duplicar, excluir, testar.
- **FR-18** Filtro por tag/tipo/origem; busca por nome.
- **FR-19** Galeria de ícones predefinidos (vetoriais) escolhida por `iconKey`.

### Persistência e portabilidade
- **FR-20** DB interno (Room) como fonte de verdade; **backup automático** via `allowBackup`.
- **FR-21** Export/import do DB inteiro (JSON) para storage compartilhado — portabilidade entre installs.

## 6. Requisitos Não-Funcionais

| ID | Requisito | Meta |
|---|---|---|
| NFR-1 | Latência do launcher (abrir → listar) | < 150 ms p/ 100 ações |
| NFR-2 | Latência de handler nativo (TRANSFORM/EXTRACT) | < 50 ms |
| NFR-3 | IA online: timeout configurável, fallback graceful | default 20 s |
| NFR-4 | Script sandbox: sem rede/arquivo por padrão | capabilities opt-in por ação |
| NFR-5 | Privacidade: nenhuma telemetria; texto nunca sai do dispositivo exceto via handler explícito (IA) | auditável |
| NFR-6 | Acessibilidade: Compose semântica, contraste Material You, suporte a TalkBack | AA |
| NFR-7 | Build reproduzível; APK release minificado (R8) com regras de keep | — |
| NFR-8 | Migração de schema Room versionada | auto-migration onde possível |

## 7. Modelo de Ação (o "objeto" — fronteira entre runtime e funcionalidade)

```
TextAction {
  id: Long (PK, autogen)
  name: String                 // rótulo no launcher
  description: String?
  type: ActionType             // qual handler executa (compile-time)
  resultMode: ResultMode       // REPLACE | COPY | DISPLAY | SHARE
  configJson: String           // config específica do tipo (JSON)
  iconKey: String              // chave na galeria de ícones
  enabled: Boolean
  order: Int
  tags: List<String>           // serializado
  source: Source               // MANUAL | AI | IMPORTED
  pipeline: List<Long>?        // ids em cadeia (type=PIPELINE)
  createdAt, updatedAt: Long
}
```

- `type` é o **único** campo que toca a fronteira compile-time (handler).
- Todo o resto é **dado**, mutável em runtime, persistido.

## 8. Taxonomia de Handlers (a fronteira compile-time)

Cada `ActionType` mapeia a um `ActionHandler` compilado. **Novo tipo = rebuild (raro).**
Nova instância de tipo existente = linha no DB (**zero rebuild**).

| ActionType | O que faz | Config exemplo |
|---|---|---|
| `TRANSLATE` | Traduz (ML Kit on-device, N pares) | `{"from":"en","to":"pt"}` |
| `TRANSFORM` | Aplica 1 de ~45 transforms do `TextFunctions` | `{"op":"UPPERCASE"}` / `{"op":"BOLD_SANS"}` / `{"op":"SORT_LINES"}` |
| `EXTRACT` | Regex/extractor → lista | `{"pattern":"\\b\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}\\b","join":"\\n"}` |
| `ANALYZE` | Estatísticas (chars/palavras/frequência/regex) → display | `{"metrics":["chars","words","emails"]}` |
| `TEMPLATE` | Substitui placeholder `{{text}}` | `{"template":"https://g.co/search?q={{text}}","encode":true}` |
| `FIND_REPLACE` | Regex find/replace na seleção | `{"find":"foo","replace":"bar","regex":false,"ignoreCase":true,"all":true}` |
| `SCRIPT` | Executa QuickJS sandboxed (text→text) | `{"code":"return s.toUpperCase()"}` |
| `AI_PROMPT` | LLM com template `{{text}}` | `{"prompt":"Resuma: {{text}}","model":"...","jsonSchema":null}` |
| `PIPELINE` | Encadeia ações (A→B→C) | `{"steps":[12,7,3]}` (ids) |

**Justificativa do set**: `SCRIPT` + `AI_PROMPT` cobrem ~qualquer ideia nova sem código
nativo → minimiza o caso raro de rebuild. `TRANSFORM`/`EXTRACT` reaproveitam ~45
funções já existentes no repo (case, wrap, sort, unicode fonts, regex extractors, count).

## 9. Auditoria de Reuso do TextTools → Decisão

### O que **fica** (reuso direto, ~50% do esqueleto)
| Asset | Por que reusar |
|---|---|
| `namespace`, `build.gradle.kts`, signing, SDK levels | Infra madura |
| `TextToolsApplication` (`@HiltAndroidApp`) + `AppModule` | DI scaffold |
| `ui/theme` (Material You, `QuickToolsTheme`) | Tema pronto |
| `NoUIActivity` (base `handleIntent` + auto-finish) | Base do launcher |
| `ContextMenuOptionsRepositoryImpl` (toggle de alias via `PackageManager`) | Reusado para o único alias do launcher |
| `functions/TextFunctions` (~45 transforms), `NumberFunctions`, `TextClassifierFunctions` | Viram implementação de `TRANSFORM`/`EXTRACT`/`ANALYZE` |
| `text/TextReplacementManager` | Lógica de find/replace → handler `FIND_REPLACE` |
| Manifest skeleton + intent-filters (`PROCESS_TEXT`, `VIEW`/https) | Padrão provado |
| Fork tooling (`scripts/`, `gh-fork-sync-merge`, `.github/workflows/release.yml`) | Manutenção do fork |

### O que **sai** (substituído)
| Asset | Motivo | Substituto |
|---|---|---|
| `features/Feature.kt` (`Feature.LIST` estático) | Catálogo compile-time | Room-backed `TextAction` store |
| `repository/ContextMenuOptionsRepository` (`FeatureIds` enum, `_multiFeatureAliasMapping`) | 1 alias por feature | 1 alias único + registry dinâmico |
| `activities/OptionsActivity` (`_options` hardcoded) | Lista fixa | `LauncherActivity` lendo DB |
| `activities/*Activity` (9 Activities de feature) | 1 Activity por feature | Launcher + handlers |
| `AppMode.SINGLE/MULTI` | Não faz sentido sem múltiplos aliases | Removido (modo único = launcher) |

### Decisão
**Fork-and-refactor** (não greenfield). Reuso ~50% do esqueleto; substituição cirúrgica do
núcleo "catálogo estático → object store". Branch: `cnmfs/texttools-x` a partir de
`cnmfs/merged-workflow`.

## 10. Critérios de Aceite (MVP)

- [ ] AC-1: Instalar APK → selecionar texto em qualquer app → "Quick Actions" aparece.
- [ ] AC-2: Launcher lista ações do DB; escolher uma executa o handler correto.
- [ ] AC-3: Em campo editável, `TRANSLATE` substitui o texto selecionado in-place.
- [ ] AC-4: Em campo não-editável, resultado vai ao clipboard.
- [ ] AC-5: Criar/editar/desativar ação no Manager reflete no launcher **sem rebuild**.
- [ ] AC-6: Gerar ação por IA → preview → salvar → aparece no launcher.
- [ ] AC-7: Importar/exportar skill JSON funciona round-trip.
- [ ] AC-8: Pipeline executa cadeia A→B corretamente.

## 11. Riscos e Mitigações

| Risco | Prob | Impacto | Mitigação |
|---|---|---|---|
| Script malicioso em skill importada | Média | Alto | QuickJS sandbox; capabilities opt-in; warning na importação |
| IA gerar JSON inválido | Alta | Médio | Schema validation + retry + preview obrigatório antes de salvar |
| Custo/token de IA online | Média | Médio | Cache de prompts; default on-device quando disponível; budget configurável |
| Migração de schema quebrar DB do usuário | Baixa | Alto | Room auto-migration + fallback + export automático pré-migração |
| UX de 2 toques (launcher) rejeitada | Média | Médio | Busca instantânea no launcher + atalhos fixos (Design B) como opcional |
| Android 14+ DCL restrictions | Baixa | Médio | Não usar DEX loading (SCRIPT via QuickJS interpretado, não DEX) |

## 12. Decisões em Aberto (bloqueiam início da SD detalhada → implementação)

1. **Persistência**: DB interno (recomendado) vs. externo editável em `/storage/emulated/0/`.
2. **Set inicial de handlers**: manter os 9 do §8?
3. **Backend de IA**: API online (qual? OpenAI/OpenRouter/Gemini/DeepSeek) vs. on-device
   (MediaPipe/Gemma) vs. ambos via abstração.
4. **Engine de script**: QuickJS (poderoso) vs. DSL restrito (simples).

**Recomendação técnica** (a confirmar pelo usuário):
interno + set dos 9 + abstração com provider OpenAI-compat default + QuickJS.
