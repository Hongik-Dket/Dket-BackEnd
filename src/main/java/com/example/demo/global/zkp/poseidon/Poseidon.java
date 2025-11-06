package com.example.demo.global.zkp.poseidon;

import java.math.BigInteger;

public interface Poseidon {

    BigInteger hash(BigInteger... inputs);

}
