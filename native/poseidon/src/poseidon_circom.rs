use ark_bn254::Fr;
use ark_ff::Field;

#[inline(always)]
fn sbox_a5(x: &mut Fr) {
    // x^5 = x * x^4
    let x2 = *x * *x;     // x^2
    let x4 = x2 * x2;     // x^4
    *x = x4 * *x;         // x^5
}

#[inline(always)]
fn mat_vec_mul(mds: &[Vec<Fr>], state: &[Fr; 3]) -> [Fr; 3] {
    let mut out = [Fr::ZERO; 3];
    for i in 0..3 {
        let mut acc = Fr::ZERO;
        acc += mds[i][0] * state[0];
        acc += mds[i][1] * state[1];
        acc += mds[i][2] * state[2];
        out[i] = acc;
    }
    out
}

pub fn poseidon_perm_circom(
    state: &mut [Fr; 3],
    rf: usize,
    rp: usize,
    mds: &[Vec<Fr>],
    ark: &[Vec<Fr>],
) {
    let total = rf + rp;
    debug_assert_eq!(ark.len(), total);
    debug_assert_eq!(mds.len(), 3);

    let rf_half = rf / 2;
    for r in 0..total {
        // 1) add round constants
        state[0] += ark[r][0];
        state[1] += ark[r][1];
        state[2] += ark[r][2];

        // 2) S-box
        let full = r < rf_half || r >= rf_half + rp;
        if full {
            sbox_a5(&mut state[0]);
            sbox_a5(&mut state[1]);
            sbox_a5(&mut state[2]);
        } else {
            sbox_a5(&mut state[0]);
        }

        // 3) MDS
        *state = mat_vec_mul(mds, state);
    }
}

/// t=3, rate=2, capacity=1
pub fn poseidon_circom_t3(inputs: &[Fr], rf: usize, rp: usize, mds: &[Vec<Fr>], ark: &[Vec<Fr>]) -> Fr {
    let mut state = [Fr::ZERO, Fr::ZERO, Fr::ZERO];

    if inputs.len() <= 2 {
        if inputs.len() >= 1 { state[1] = inputs[0]; }
        if inputs.len() == 2 { state[2] = inputs[1]; }
        poseidon_perm_circom(&mut state, rf, rp, mds, ark);

        return state[0];
    }

    let mut iter = inputs.iter();
    loop {
        state[1] += *iter.next().unwrap();
        if let Some(x2) = iter.next() { state[2] += *x2; }
        poseidon_perm_circom(&mut state, rf, rp, mds, ark);

        if iter.as_slice().is_empty() { break; }

        state[1] = Fr::ZERO;
        state[2] = Fr::ZERO;
    }

    state[0]
}

