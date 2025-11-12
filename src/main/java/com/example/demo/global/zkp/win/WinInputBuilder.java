package com.example.demo.global.zkp.win;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.zkp.poseidon.Poseidon;
import com.example.demo.global.zkp.poseidon.PoseidonMerkleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;

import static com.example.demo.global.util.FrCodec.*;
import static com.example.demo.global.util.Hexes.*;
import static com.example.demo.global.util.Hexes.bigIntToBe32;

@Component
@RequiredArgsConstructor
public class WinInputBuilder {

    private final Poseidon poseidon;
    private final PoseidonMerkleService poseidonMerkleService;

    private final SessionRepository sessionRepository;

    @Value("${zk.depth}")
    private int depth;

    @Value("${zk.payTagConstHex}")
    private String payTagConstHex;

    public Map<String, Object> build(Long sessionId, int leafIndex, String icHex) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        var path = poseidonMerkleService.pathOf(sessionId, leafIndex);
        var sibHexes = path.siblingsHex32();
        var indexBits = path.indexBits();

        if (sibHexes.size() != depth || indexBits.size() != depth) {
            throw new CustomException(ErrorStatus.ZKP_DEPTH_MISMATCH);
        }

        BigInteger IC = new BigInteger(1, hexToBytes(icHex));
        BigInteger SID = BigInteger.valueOf(sessionId);
        BigInteger PAY_TAG = new BigInteger(beHexToFrDec(payTagConstHex));

        List<String> pathElements = new ArrayList<>(depth);
        for (String hx : sibHexes) {
            pathElements.add(beHexToFrDec(hx));
        }

        String leaf = to0xHex(bigIntToBe32(poseidon.hash(IC, SID)));
        BigInteger cur = fr(leaf);
        for (int i = 0; i < depth; i++) {
            BigInteger pe = new BigInteger(pathElements.get(i));
            if (indexBits.get(i) == 0) {
                cur = poseidon.hash(cur, pe);
            } else {
                cur = poseidon.hash(pe, cur);
            }
        }

        if (!Arrays.equals(bigIntToBe32(cur), session.getWinnersRoot())) {
            throw new CustomException(ErrorStatus.ZKP_ROOT_MISMATCH);
        }

        String winnersRootFr = new BigInteger(1, session.getWinnersRoot())
                .mod(BN254_P).toString(10);

        BigInteger paymentNullifier = poseidon.hash(IC, SID, PAY_TAG);

        Map<String, Object> json = new HashMap<>();
        json.put("IC", IC.toString());
        json.put("sessionId", SID.toString());
        json.put("winnersRoot", winnersRootFr);
        json.put("paymentNullifier", paymentNullifier.toString());
        json.put("pathElements", pathElements);
        json.put("pathIndex", indexBits);

        return json;
    }

}
