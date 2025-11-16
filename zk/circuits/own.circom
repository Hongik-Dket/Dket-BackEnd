pragma circom 2.1.6;

include "./membership.circom";

// LEAF_TAG: keccak256("Dket:own") mod BN254
// NULLIFIER_TAG: keccak256("Dket:enter") mod BN254
component main = Base(
    20,
    0x0b1ad30a3769ffdbb6a73732c1599ee47f8fcdb4c3fd7b39a51fd63183e0c4c7,
    0x26d1241dc48e6914dc2f93b04149abc1a4bd2f94cb7990c6647c35800be80722
);