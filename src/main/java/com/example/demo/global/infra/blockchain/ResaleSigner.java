package com.example.demo.global.infra.blockchain;

import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Sign;
import org.web3j.crypto.StructuredDataEncoder;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class ResaleSigner {

    private static final String TEMPLATE_PATH = "eip712/permit_purchase_template.json";

    private final Credentials creds;
    private final long chainId;
    private final String verifyingContract;

    public String signPermitPurchase(String buyer, Long resaleId, BigInteger tokenId, BigInteger priceWei, BigInteger expireAt) {
        validateParameters(tokenId, priceWei, expireAt);

        return signPermitPurchase(
                buyer,
                Long.toString(resaleId),
                tokenId.toString(),
                priceWei.toString(),
                expireAt.toString()
        );
    }

    private String signPermitPurchase(String buyer, String resaleId, String tokenId, String priceWei, String expireAt) {
        try {
            String typedDataJson = buildTypedDataJsonFromTemplate(
                    verifyingContract,
                    chainId,
                    buyer,
                    resaleId,
                    tokenId,
                    priceWei,
                    expireAt
            );

            byte[] digest = new StructuredDataEncoder(typedDataJson).hashStructuredData();

            Sign.SignatureData sig = Sign.signMessage(digest, creds.getEcKeyPair(), false);

            byte[] sigBytes = new byte[65];
            System.arraycopy(sig.getR(), 0, sigBytes, 0, 32);
            System.arraycopy(sig.getS(), 0, sigBytes, 32, 32);
            sigBytes[64] = sig.getV()[0];

            String signature = Numeric.toHexString(sigBytes);
            log.debug("EIP712 signature ok buyer={} resaleId={} sig={}", buyer, resaleId, signature);

            return signature;
        } catch (Exception e) {
            log.error("Failed to sign PermitPurchase", e);
            throw new CustomException(ErrorStatus.RESALE_AUTH_SIGN_FAILED);
        }
    }

    private static String buildTypedDataJsonFromTemplate(String verifyingContract, long chainId,
                                                         String buyer, String resaleId, String tokenId,
                                                         String priceWei, String expireAt
    ) {
        try (InputStream in = ResaleSigner.class.getClassLoader().getResourceAsStream(TEMPLATE_PATH)) {

            if (in == null) {
                throw new CustomException(ErrorStatus.RESALE_AUTH_NO_TEMPLATE);
            }

            String template = new String(in.readAllBytes(), StandardCharsets.UTF_8);

            return template
                    .replace("{{CHAIN_ID}}", String.valueOf(chainId))
                    .replace("{{CONTRACT}}", verifyingContract)
                    .replace("{{BUYER}}", buyer)
                    .replace("{{RESALE_ID}}", resaleId)
                    .replace("{{TOKEN_ID}}", tokenId)
                    .replace("{{PRICE}}", priceWei)
                    .replace("{{EXPIRE_AT}}", expireAt);
        } catch (IOException e) {
            log.error("Failed to build TypedDataJson", e);
            throw new CustomException(ErrorStatus.RESALE_AUTH_TYPEDDATA_FAILED);
        }

    }

    private static String strip0x(String hex) {
        if (hex == null) {
            return null;
        }

        String h = hex.trim();
        return (h.startsWith("0x") || h.startsWith("0X")) ? h.substring(2) : h;
    }

    private static String to0xHexAddress(String addr) {
        if (addr == null) {
            throw new CustomException(ErrorStatus.RESALE_AUTH_INVALID_ADDRESS);
        }

        String a = addr.trim();
        if (!a.startsWith("0x") && !a.startsWith("0X")) {
            a = "0x" + a;
        }

        if (a.length() != 42) {
            throw new CustomException(ErrorStatus.RESALE_AUTH_INVALID_ADDRESS);
        }

        return a;
    }

    private static void validateParameters(BigInteger tokenId, BigInteger priceWei, BigInteger expireAt) {
        if (tokenId == null || priceWei == null || expireAt == null
                || tokenId.signum() < 0 || expireAt.signum() < 0) {
            throw new CustomException(ErrorStatus.RESALE_AUTH_WRONG_PARAMETER);
        }

        if (expireAt.compareTo(new BigInteger("18446744073709551615")) > 0) {
            throw new CustomException(ErrorStatus.RESALE_AUTH_WRONG_PARAMETER);
        }
    }
}
