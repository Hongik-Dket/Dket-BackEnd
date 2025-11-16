pragma circom 2.1.6;

include "./membership.circom";

// LEAF_TAG: keccak256("Dket:apply") mod BN254
// NULLIFIER_TAG: keccak256("Dket:pay") mod BN254
component main = Base(
    20,
    0x21893c20f71039c08143fce9ce8cc2246209d41942edd38ff7e003737b20a7a1,
    0x0844240e9aafe11e996165900b7372f1cd87f4553fd0853e30b47650a7ee576c
);