package com.example.demo.global.zkp.proof;

import com.example.demo.domain.concert.entity.Session;
import com.example.demo.domain.concert.repository.SessionRepository;
import com.example.demo.domain.lottery.repository.ApplicantsSnapshotItemRepository;
import com.example.demo.domain.ownership.repository.OwnershipRepository;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.example.demo.global.zkp.poseidon.Poseidon;
import com.example.demo.global.zkp.poseidon.PoseidonMerkleService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;

import static com.example.demo.global.base.Constants.APPLY_TAG;
import static com.example.demo.global.base.Constants.OWN_TAG;
import static com.example.demo.global.util.FrCodec.*;
import static com.example.demo.global.util.Hexes.*;
import static com.example.demo.global.util.Hexes.bigIntToBe32;

@Component
@RequiredArgsConstructor
public class InputBuilder {

    private final Poseidon poseidon;
    private final PoseidonMerkleService poseidonMerkleService;

    private final SessionRepository sessionRepository;
    private final ApplicantsSnapshotItemRepository applicantsSnapshotItemRepository;
    private final OwnershipRepository ownershipRepository;

    @Value("${zk.depth}")
    private int depth;

    public Map<String, Object> buildWinInput(Long sessionId, int leafIndex, String icHex) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        List<String> leafHexes = applicantsSnapshotItemRepository.findWinnerLeafHexes(sessionId);
        if (leafHexes == null || leafHexes.isEmpty()) {
            throw new CustomException(ErrorStatus.SNAPSHOT_WINNER_LEAFS_EMPTY);
        }

        return build(session, leafIndex, icHex, leafHexes, session.getWinnersRoot(), APPLY_TAG);
    }

    public Map<String, Object> buildOwnInput(Long sessionId, int leafIndex, String icHex) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new CustomException(ErrorStatus.SESSION_NOT_FOUND));

        List<String> leafHexes = ownershipRepository.findOwnerLeafHexes(sessionId);
        if (leafHexes == null || leafHexes.isEmpty()) {
            throw new CustomException(ErrorStatus.OWN_LEAF_EMPTY);
        }

        return build(session, leafIndex, icHex, leafHexes, session.getWinnersRoot(), OWN_TAG);
    }

    private Map<String, Object> build(Session session, int leafIndex, String icHex,
                                      List<String> leafHexes, byte[] root, BigInteger tag
    ) {
        var path = poseidonMerkleService.pathOf(leafIndex, leafHexes);
        var sibHexes = path.siblingsHex32();
        var indexBits = path.indexBits();

        if (sibHexes.size() != depth || indexBits.size() != depth) {
            throw new CustomException(ErrorStatus.ZKP_DEPTH_MISMATCH);
        }

        BigInteger IC = new BigInteger(1, hexToBytes(icHex));
        BigInteger SID = BigInteger.valueOf(session.getId());

        List<String> pathElements = new ArrayList<>(depth);
        for (String hx : sibHexes) {
            pathElements.add(beHexToFrDec(hx));
        }

        String leaf = to0xHex(bigIntToBe32(poseidon.hash(poseidon.hash(IC, SID), tag)));

        BigInteger cur = fr(leaf);
        for (int i = 0; i < depth; i++) {
            BigInteger pe = new BigInteger(pathElements.get(i));
            if (indexBits.get(i) == 0) {
                cur = poseidon.hash(cur, pe);
            } else {
                cur = poseidon.hash(pe, cur);
            }
        }

        if (!Arrays.equals(bigIntToBe32(cur), root)) {
            throw new CustomException(ErrorStatus.ZKP_ROOT_MISMATCH);
        }

        String rootFr = new BigInteger(1, root)
                .mod(BN254_P).toString(10);

        List<String> pathIndexStr = new ArrayList<>(depth);
        for (int i = 0; i < depth; i++) {
            pathIndexStr.add(indexBits.get(i) == 0 ? "0" : "1");
        }

        Map<String, Object> json = new HashMap<>();
        json.put("IC", beHexToFrDec(icHex));
        json.put("sessionId", SID.toString());
        json.put("root", rootFr);
        json.put("pathElements", pathElements);
        json.put("pathIndex", pathIndexStr);

        return json;
    }

}
