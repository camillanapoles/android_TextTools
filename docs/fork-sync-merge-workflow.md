# Workflow: Fork Sync + Merge Agnóstico

## Propósito

Manter um fork sincronizado com o upstream **sem perder commits personalizados**, preservando um histórico linear (rebase-style) num branch de feature que pode crescer paralelamente à `main`.

## Arquitetura do Padrão

```
upstream/main ── A ── B ── C ── D ── E ── F ── G          ← main sincronizada
                                │
               cnmfs/merged-workflow ── [X] ── [Y] ── [Z]   ← seus commits cherry-pickados + novos
                                    ↑
                              rebase periódico ──► E ── F ── G ── X ── Y ── Z
```

## Regras de Ouro

1. **Nunca commitar direto na `main`** — ela é espelho do upstream.
2. **Todo trabalho vai no branch `cnmfs/<feature>`**, criado a partir da `main` sincronizada.
3. **Atualização**: `git rebase main` no branch de feature (linear, sem merge commits).
4. **Commits do fork original** entram por cherry-pick (merge commits são descartados).

---

## Script Automatizado

```bash
./scripts/fork-sync-merge.sh <upstream-remote> <upstream-branch> <prefixo>
```

### Exemplo concreto (deste repo)

```bash
# 1. Adicionar remote upstream (uma vez só)
git remote add corphish https://github.com/corphish/TextTools.git

# 2. Executar o workflow completo
./scripts/fork-sync-merge.sh corphish main cnmfs
```

### O que ele faz em cada etapa

| Fase | Ação | Branch criada |
|------|------|---------------|
| 1 | Backup dos commits divergentes | `cnmfs/backup-fork-sync` |
| 2 | Hard reset da `main` para upstream | `main` (reescrita) |
| 3 | Force push da `main` para origin | — |
| 4 | Cherry-pick dos commits não-merge | `cnmfs/merged-fork-sync` |
| 5 | Push da `cnmfs/merged-fork-sync` | — |
| 6 | Push do branch de backup | — |

---

## Crescimento Paralelo (Rebase-style)

Depois que o workflow inicial rodar, o padrão para continuar é:

### 1. Desenvolver no branch de feature

```bash
git checkout cnmfs/merged-workflow
# ... faz commits ...
git commit -m "minha nova funcionalidade"
git push origin cnmfs/merged-workflow
```

### 2. Sincronizar com upstream periodicamente

```bash
# Busca atualizações do upstream
git fetch corphish main

# Rebateia o branch de feature sobre a main atualizada
git checkout cnmfs/merged-workflow
git rebase corphish/main

# Se houver conflitos, resolva e continue:
#   git add .
#   git rebase --continue

# Publica com force-with-lease (seguro: só sobrescreve se ninguém mexeu)
git push origin cnmfs/merged-workflow --force-with-lease
```

### 3. Abrir PR / Mesclar quando quiser

```bash
# Opção A: PR no GitHub (cria PR do cnmfs/merged-workflow → main)
gh pr create --base main --head cnmfs/merged-workflow

# Opção B: Merge local na main (se tiver permissão)
git checkout main
git merge cnmfs/merged-workflow   # fast-forward possible
git push origin main
```

---

## Por que Rebase e não Merge?

| Merge | Rebase |
|-------|--------|
| Cria commit de merge | Histórico linear |
| Mistura histórico | Cada commit é seu |
| Difícil de entender o que é seu | Fácil de ver contribuições |
| `git log` poluído | `git log` limpo |

No fluxo de fork, rebase é ideal porque **seus commits parecem ter sido feitos sempre na ponta do upstream** — sem baggage de merges passados.

---

## Resolução de Conflitos Comuns

### Cherry-pick vazio (já contido no upstream)

```bash
git cherry-pick --skip
```

### Conflito real durante cherry-pick

```bash
# Edite os arquivos conflitantes
git add .
git cherry-pick --continue
```

### Conflito durante rebase

```bash
# Edite os arquivos conflitantes
git add .
GIT_EDITOR=true git rebase --continue   # mantém mensagem original
```

---

## Diagrama Visual

```
ANTES (fork defasado)
  upstream/main ── A ── B ── C ── ... ── N
  origin/main    ── A ── B ── C ── X ── Y ── Z  (3 ahead, 64 behind)

DEPOIS (workflow executado)
  origin/main    ── A ── B ── C ── ... ── N     ← sync
  cnmfs/merged  ── A ── B ── C ── ... ── N ── X ── Y ── Z  ← linear

CRESCIMENTO PARALELO
  origin/main    ── ... ── N ── N+1 ── N+2
  cnmfs/merged  ── ... ── N ── X ── Y ── Z ── W
                               └── rebase ──► N ── N+1 ── N+2 ── X ── Y ── Z ── W
```

## Notas

- O `backup-fork-sync` nunca é deletado — serve como registro imutável do estado original antes do sync.
- `--force-with-lease` é preferível a `--force` em branches compartilhados (protege contra overwrite de trabalho alheio).
- Para forks com muitos commits próprios (dezenas+), considere particionar em múltiplos branches de feature em vez de um único branch gigante.
