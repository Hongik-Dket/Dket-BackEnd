#!/usr/bin/env bash
set -euo pipefail

OUT="$(cd "$(dirname "$0")/.." && pwd)/build"
mkdir -p "$OUT"

PTAU="$OUT/powersOfTau28_hez_final_20.ptau"
if [ ! -f "$PTAU" ]; then
  curl -L -o "$PTAU" https://storage.googleapis.com/zkevm/ptau/powersOfTau28_hez_final_20.ptau
fi
echo "PTAU ready at $PTAU"