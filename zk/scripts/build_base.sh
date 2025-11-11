#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CIR="${ROOT}/circuits/win_base.circom"
OUT="${ROOT}/build"
PTAU="${OUT}/powersOfTau28_hez_final_20.ptau"

mkdir -p "${OUT}"

circom "${CIR}" --r1cs --wasm --sym -l "${ROOT}/node_modules" -o "${OUT}"

snarkjs plonk setup "${OUT}/win_base.r1cs" "${PTAU}" "${OUT}/win_base.zkey"

snarkjs zkey export verificationkey "${OUT}/win_base.zkey" "${OUT}/win_base.vkey.json"
snarkjs zkey export solidityverifier "${OUT}/win_base.zkey" "${OUT}/WinVerifier_plonk.sol"

echo "OK: ${OUT}/WinVerifier_plonk.sol"