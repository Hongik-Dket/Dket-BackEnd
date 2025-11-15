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

template Base(depth, TAG_CONST) {
    // private
    signal input IC;
    signal input pathElements[depth];
    signal input pathIndex[depth];

    // public
    signal input  sessionId;
    signal input  root;

    component hLeaf = Poseidon(2);
    hLeaf.inputs[0] <== IC;
    hLeaf.inputs[1] <== sessionId;
    signal leafHash;
    leafHash <== hLeaf.out;

    component mi = MerkleInclusion(depth);
    mi.leafHash <== leafHash;
    for (var i = 0; i < depth; i++) {
        mi.pathElements[i] <== pathElements[i];
        mi.pathIndex[i]    <== pathIndex[i];
    }
    mi.root === root;

    // nullifier = Poseidon(IC, sessionId, TAG_CONST)
    component hNull = Poseidon(3);
    hNull.inputs[0] <== IC;
    hNull.inputs[1] <== sessionId;
    hNull.inputs[2] <== TAG_CONST;

    signal output sessionId_pub;
    signal output root_pub;
    signal output nullifier_pub;

    sessionId_pub       <== sessionId;
    root_pub            <== root;
    nullifier_pub       <== hNull.out;
}