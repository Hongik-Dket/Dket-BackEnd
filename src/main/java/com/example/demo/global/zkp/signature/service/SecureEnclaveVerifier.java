package com.example.demo.global.zkp.signature.service;

import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECPoint;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPublicKeySpec;

import static com.example.demo.global.util.Hexes.bytesToHex;
import static com.example.demo.global.util.Hexes.hexToBytes;

@Slf4j
public final class SecureEnclaveVerifier {

    private SecureEnclaveVerifier() {}

    public static boolean verify(
            String challenge,
            String signatureHex,
            String publicKeyHex
    ) {
        try {
            byte[] messageBytes = challenge.getBytes(StandardCharsets.UTF_8);

            byte[] signatureBytes = hexToBytes(signatureHex);
            ECPublicKey publicKey = loadP256PublicKeyFromCompressedHex(publicKeyHex);

            Signature verifier = Signature.getInstance("SHA256withECDSA", "BC");
            verifier.initVerify(publicKey);
            verifier.update(messageBytes);

            return verifier.verify(signatureBytes);
        } catch (Exception e) {
            log.error("SecureEnclave signature verify failed", e);
            throw new CustomException(ErrorStatus.SIG_VERIFY_FAILED);
        }
    }

    private static ECPublicKey loadP256PublicKeyFromCompressedHex(String publicKeyHex) {
        try {
            byte[] pubBytes = hexToBytes(publicKeyHex);

            X9ECParameters params = ECNamedCurveTable.getByName("secp256r1");
            ECParameterSpec ecSpec = new ECNamedCurveSpec(
                    "secp256r1",
                    params.getCurve(),
                    params.getG(),
                    params.getN(),
                    params.getH(),
                    params.getSeed()
            );

            ECPoint point = params.getCurve().decodePoint(pubBytes);

            java.security.spec.ECPoint w = new java.security.spec.ECPoint(
                    point.getAffineXCoord().toBigInteger(),
                    point.getAffineYCoord().toBigInteger()
            );

            KeyFactory kf = KeyFactory.getInstance("EC", "BC");
            ECPublicKeySpec keySpec = new ECPublicKeySpec(w, ecSpec);

            return (ECPublicKey) kf.generatePublic(keySpec);
        } catch (Exception e) {
            log.error("Public key decode failed", e);
            throw new CustomException(ErrorStatus.SIG_VERIFY_FAILED);
        }
    }

}