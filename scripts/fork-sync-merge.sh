#!/usr/bin/env bash
# ==============================================================
# fork-sync-merge.sh — Workflow Agnóstico para Fork Sync + Merge
# ==============================================================
# Uso:
#   ./scripts/fork-sync-merge.sh <upstream-remote> <upstream-branch> <feature-prefixo>
#
# Exemplo:
#   ./scripts/fork-sync-merge.sh corphish main cnmfs
#
# O que faz:
#   1. Backup dos commits divergentes em <prefixo>/backup-<feature>
#   2. Sincroniza main com upstream (hard reset + force push)
#   3. Cria branch rebaseada <prefixo>/merged-<feature> com cherry-pick
#   4. Push de tudo pro origin
#
# Pré-condições:
#   - Estar na raiz do repositório
#   - remote upstream já adicionado (ex: git remote add upstream <url>)
#   - working tree limpo (git status limpo)
# ==============================================================

set -euo pipefail

UPSTREAM="${1:-corphish}"
UPSTREAM_BRANCH="${2:-main}"
PREFIX="${3:-cnmfs}"

LOCAL_BRANCH="main"  # branch local que espelha o upstream

echo "🔍 Coletando commits divergentes entre $LOCAL_BRANCH e $UPSTREAM/$UPSTREAM_BRANCH ..."
BEHIND=$(git rev-list --count "$UPSTREAM/$UPSTREAM_BRANCH" ^"$LOCAL_BRANCH" 2>/dev/null || echo "?")
AHEAD=$(git rev-list --count "$LOCAL_BRANCH" ^"$UPSTREAM/$UPSTREAM_BRANCH" 2>/dev/null || echo "?")

echo "   $AHEAD commits à frente, $BEHIND commits atrás de $UPSTREAM/$UPSTREAM_BRANCH"
echo ""

# ==============================================================
# FASE 1 — Backup dos commits divergentes
# ==============================================================
BACKUP_BRANCH="$PREFIX/backup-fork-sync"
echo "📦 FASE 1: Backup em $BACKUP_BRANCH"
if git rev-parse --verify "$BACKUP_BRANCH" >/dev/null 2>&1; then
  echo "   ⚠️  Branch $BACKUP_BRANCH já existe. Deletando e recriando..."
  git branch -D "$BACKUP_BRANCH"
fi
git branch "$BACKUP_BRANCH" "$LOCAL_BRANCH"
echo "   ✅ Backup criado: $BACKUP_BRANCH"
echo ""

# ==============================================================
# FASE 2 — Resetar main para upstream
# ==============================================================
echo "🔄 FASE 2: Resetando $LOCAL_BRANCH para $UPSTREAM/$UPSTREAM_BRANCH ..."
git checkout "$LOCAL_BRANCH"
git reset --hard "$UPSTREAM/$UPSTREAM_BRANCH"
echo "   ✅ $LOCAL_BRANCH agora em $(git rev-parse --short HEAD)"
echo ""

# ==============================================================
# FASE 3 — Push da main sincronizada (force)
# ==============================================================
echo "📤 FASE 3: Push force da $LOCAL_BRANCH para origin..."
git push origin "$LOCAL_BRANCH" --force
echo "   ✅ origin/$LOCAL_BRANCH sincronizado com $UPSTREAM/$UPSTREAM_BRANCH"
echo ""

# ==============================================================
# FASE 4 — Criar branch rebaseada com cherry-pick
# ==============================================================
MERGE_BRANCH="$PREFIX/merged-fork-sync"
echo "🌿 FASE 4: Criando $MERGE_BRANCH a partir de $LOCAL_BRANCH ..."
if git rev-parse --verify "$MERGE_BRANCH" >/dev/null 2>&1; then
  echo "   ⚠️  Branch $MERGE_BRANCH já existe. Deletando e recriando..."
  git branch -D "$MERGE_BRANCH"
fi
git checkout -b "$MERGE_BRANCH"

# Coletar os commits a cherry-pick (excluindo merges)
# Pega da lista ao contrário (ordem cronológica) para cherry-pick sequencial
COMMITS=$(git log --reverse --no-merges --format="%H" "$UPSTREAM/$UPSTREAM_BRANCH".."$BACKUP_BRANCH" 2>/dev/null || true)

if [ -z "$COMMITS" ]; then
  echo "   ⚠️  Nenhum commit não-merge encontrado para cherry-pick."
else
  echo "   Cherry-picking commits não-merge:"
  echo "$COMMITS" | while read -r sha; do
    msg=$(git log --format="%s" -1 "$sha")
    echo "     → ${sha:0:7} $msg"
  done

  # Cherry-pick em lote com tratamento de vazios
  for sha in $COMMITS; do
    if git cherry-pick "$sha" 2>/dev/null; then
      echo "       ✅ ${sha:0:7} aplicado"
    else
      # Se o cherry-pick falhar, verifica se é vazio ou conflito real
      if git status --porcelain | grep -q .; then
        echo "       ⛔ ${sha:0:7} — CONFLITO! Resolva manualmente e continue com 'git cherry-pick --continue'"
        echo "       Comando pausado. Após resolver, execute: git cherry-pick --continue"
        exit 1
      else
        echo "       ⏭️  ${sha:0:7} vazio (já aplicado), pulando..."
        git cherry-pick --skip
      fi
    fi
  done
fi

echo ""
echo "   ✅ $MERGE_BRANCH criada com commits cherry-pickados"
echo ""

# ==============================================================
# FASE 5 — Push do branch mesclado
# ==============================================================
echo "📤 FASE 5: Push de $MERGE_BRANCH para origin..."
git push origin "$MERGE_BRANCH" --force
echo "   ✅ origin/$MERGE_BRANCH publicado"
echo ""

# ==============================================================
# FASE 6 — Push do backup
# ==============================================================
echo "📤 FASE 6: Push de $BACKUP_BRANCH para origin..."
git push origin "$BACKUP_BRANCH" --force
echo "   ✅ origin/$BACKUP_BRANCH publicado"
echo ""

# ==============================================================
# Final
# ==============================================================
echo "═══════════════════════════════════════════════════════════"
echo "  🎉 Fork sync concluído!"
echo ""
echo "  $LOCAL_BRANCH         → sincronizada com $UPSTREAM/$UPSTREAM_BRANCH"
echo "  $BACKUP_BRANCH        → backup dos commits originais"
echo "  $MERGE_BRANCH         → rebase-style com cherry-pick"
echo ""
echo "  Para continuar crescendo em paralelo:"
echo "    git checkout $MERGE_BRANCH"
echo "    # faz commits..."
echo "    git fetch $UPSTREAM $UPSTREAM_BRANCH"
echo "    git rebase $UPSTREAM/$UPSTREAM_BRANCH"
echo "    git push origin $MERGE_BRANCH --force-with-lease"
echo "═══════════════════════════════════════════════════════════"
