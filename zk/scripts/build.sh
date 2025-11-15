#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="${ROOT}/build"
PTAU="${OUT}/powersOfTau28_hez_final_20.ptau"

mkdir -p "${OUT}"

build_one () {
  local SRC="$1"       # win.circom / own.circom
  local PREFIX="$2"    # win / own

  circom "${SRC}" --r1cs --wasm --sym \
    -l "${ROOT}/node_modules" \
    -o "${OUT}"

  snarkjs plonk setup "${OUT}/${PREFIX}.r1cs" "${PTAU}" "${OUT}/${PREFIX}.zkey"

  snarkjs zkey export verificationkey \
    "${OUT}/${PREFIX}.zkey" "${OUT}/${PREFIX}.vkey.json"

  snarkjs zkey export solidityverifier \
    "${OUT}/${PREFIX}.zkey" "${OUT}/${PREFIX}Verifier_plonk.sol"

  echo "OK: ${OUT}/${PREFIX}Verifier_plonk.sol"
}

build_one "${ROOT}/circuits/win.circom" "win"
build_one "${ROOT}/circuits/own.circom" "own"