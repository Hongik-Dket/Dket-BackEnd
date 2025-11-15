include "./membership.circom";

// PAY_TAG_CONST: keccak256("Dket:pay") mod BN254
component main = Base(
    20,
    0x0844240e9aafe11e996165900b7372f1cd87f4553fd0853e30b47650a7ee576c
);