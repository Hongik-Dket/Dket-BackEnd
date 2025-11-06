package com.example.demo.global.crypto;

import java.math.BigInteger;

public interface Poseidon {

    BigInteger hash(BigInteger... inputs);

}
