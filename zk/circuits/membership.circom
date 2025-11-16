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

template Base(depth, LEAF_TAG, NULLIFIER_TAG) {
    // private
    signal input IC;
    signal input pathElements[depth];
    signal input pathIndex[depth];

    // public
    signal input  sessionId;
    signal input  root;

    component hLeaf1 = Poseidon(2);
    hLeaf1.inputs[0] <== IC;
    hLeaf1.inputs[1] <== sessionId;

    component hLeaf2 = Poseidon(2);
    hLeaf2.inputs[0] <== hLeaf1.out;
    hLeaf2.inputs[1] <== LEAF_TAG;
    signal leafHash;
    leafHash <== hLeaf2.out;

    component mi = MerkleInclusion(depth);
    mi.leafHash <== leafHash;
    for (var i = 0; i < depth; i++) {
        mi.pathElements[i] <== pathElements[i];
        mi.pathIndex[i]    <== pathIndex[i];
    }
    mi.root === root;

    // nullifier = Poseidon(IC, sessionId, NULLIFIER_TAG)
    component hNull = Poseidon(3);
    hNull.inputs[0] <== IC;
    hNull.inputs[1] <== sessionId;
    hNull.inputs[2] <== NULLIFIER_TAG;

    signal output sessionId_pub;
    signal output root_pub;
    signal output nullifier_pub;

    sessionId_pub       <== sessionId;
    root_pub            <== root;
    nullifier_pub       <== hNull.out;
}