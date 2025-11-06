use jni::objects::{JClass, JByteArray};
use jni::sys::{jbyteArray, jint};
use jni::JNIEnv;

mod params_bn254_t3;
mod poseidon_circom;

use params_bn254_t3::poseidon_hash_bn254_t3_be_chunks;

#[no_mangle]
pub extern "system" fn Java_com_example_demo_global_crypto_impl_PoseidonNative_poseidonHash(
    env: JNIEnv,
    _class: JClass,
    concatenated32be: JByteArray,
    elem_count: jint,
) -> jbyteArray {
    let data = env.convert_byte_array(concatenated32be).expect("read input");
    let n = elem_count as usize;
    assert_eq!(data.len(), 32 * n, "input length mismatch");

    // &[u8] → &[&[u8]]
    let mut chunks: Vec<&[u8]> = Vec::with_capacity(n);
    for i in 0..n {
        chunks.push(&data[i * 32..(i + 1) * 32]);
    }

    let out = poseidon_hash_bn254_t3_be_chunks(&chunks);
    env.byte_array_from_slice(&out).expect("alloc out").into_raw()
}