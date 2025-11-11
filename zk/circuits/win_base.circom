pragma circom 2.1.6;

include "circomlib/circuits/poseidon.circom";

template MerkleInclusion(depth) {
    // inputs
    signal input leafHash;
    signal input pathElements[depth];
    signal input pathIndex[depth];

    // output
    signal output root;

    signal cur[depth + 1];
    component hP[depth];
    signal left[depth];
    signal right[depth];

    for (var i = 0; i < depth; i++) {
        pathIndex[i] * (pathIndex[i] - 1) === 0;
    }

    cur[0] <== leafHash;

    for (var i = 0; i < depth; i++) {
        left[i] <== cur[i] + pathIndex[i] * (pathElements[i] - cur[i]);
        right[i] <== pathElements[i] + pathIndex[i] * (cur[i] - pathElements[i]);

        hP[i] = Poseidon(2);
        hP[i].inputs[0] <== left[i];
        hP[i].inputs[1] <== right[i];
        cur[i + 1] <== hP[i].out;
    }

    root <== cur[depth];
}

template WinBase(depth, PAY_TAG_CONST) {
    // private
    signal input IC;
    signal input pathElements[depth];
    signal input pathIndex[depth];

    // public
    signal input  sessionId;
    signal input  winnersRoot;
    signal input paymentNullifier;

    component hLeaf = Poseidon(2);
    hLeaf.inputs[0] <== IC;
    hLeaf.inputs[1] <== sessionId;
    signal leaf;
    leaf <== hLeaf.out;

    component hLeafHash = Poseidon(1);
    hLeafHash.inputs[0] <== leaf;
    signal leafHash;
    leafHash <== hLeafHash.out;

    component mi = MerkleInclusion(depth);
    mi.leafHash <== leafHash;
    for (var i = 0; i < depth; i++) {
        mi.pathElements[i] <== pathElements[i];
        mi.pathIndex[i]    <== pathIndex[i];
    }
    mi.root === winnersRoot;

    // paymentNullifier = Poseidon(IC, sessionId, PAY_TAG_CONST)
    component hNull = Poseidon(3);
    hNull.inputs[0] <== IC;
    hNull.inputs[1] <== sessionId;
    hNull.inputs[2] <== PAY_TAG_CONST;
    hNull.out === paymentNullifier;

    signal output sessionId_pub;
    signal output winnersRoot_pub;
    signal output paymentNullifier_pub;

    sessionId_pub        <== sessionId;
    winnersRoot_pub      <== winnersRoot;
    paymentNullifier_pub <== paymentNullifier;
}

// PAY_TAG_CONST: keccak256("Dket:pay") mod BN254
component main = WinBase(
    20,
    0x0844240e9aafe11e996165900b7372f1cd87f4553fd0853e30b47650a7ee576c
);