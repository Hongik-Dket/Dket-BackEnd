use ark_bn254::Fr;
use ark_ff::{PrimeField, BigInteger};

#[inline]
fn fr(hex: &str) -> Fr {
    let s = hex.trim_start_matches("0x");
    let be = hex::decode(s).expect("decode hex");
    Fr::from_be_bytes_mod_order(&be)
}

pub struct PoseidonParams {
    pub full_rounds: usize,     // 8
    pub partial_rounds: usize,  // 57
    pub alpha: u64,             // 5
    pub mds: Vec<Vec<Fr>>,      // 3x3
    pub ark: Vec<Vec<Fr>>,      // 65x3
}

pub fn circomlibjs_params_t3() -> PoseidonParams {
    let alpha = 5u64;

    let (full_rounds, partial_rounds, mds, ark) = include!(concat!(
    env!("CARGO_MANIFEST_DIR"),
    "/../../zk/poseidon_t3_rust_consts.rs"
    ));

    PoseidonParams { full_rounds, partial_rounds, alpha, mds, ark }
}

pub fn poseidon_hash_bn254_t3_be_chunks(be_chunks32: &[&[u8]]) -> [u8; 32] {
    use crate::poseidon_circom::poseidon_circom_t3;

    let p = circomlibjs_params_t3();
    let mut inputs = Vec::with_capacity(be_chunks32.len());
    for &chunk in be_chunks32 {
        debug_assert_eq!(chunk.len(), 32);
        inputs.push(Fr::from_be_bytes_mod_order(chunk));
    }

    let out = poseidon_circom_t3(&inputs, p.full_rounds, p.partial_rounds, &p.mds, &p.ark);

    let be = out.into_bigint().to_bytes_be();
    let mut out32 = [0u8; 32];
    let start = 32 - be.len();
    out32[start..].copy_from_slice(&be);
    out32
}