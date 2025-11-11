package com.example.demo.global.zkp.poseidon;

import com.example.demo.domain.lottery.repository.ApplicantsSnapshotItemRepository;
import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.example.demo.global.util.Hexes.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PoseidonMerkleService {

    private final Poseidon poseidon;

    private final ApplicantsSnapshotItemRepository applicantsSnapshotItemRepository;

    @Value(value = "${zk.depth}")
    private int depth;

    private static final BigInteger BN254_P = new BigInteger(
            "21888242871839275222246405745257275088548364400416034343698204186575808495617"
    );

    private static BigInteger fr(String hex0x) {
        byte[] be = hexToBytes(hex0x);

        if (be.length != 32) {
            be = directBe32(be);
        }

        return new BigInteger(1, be).mod(BN254_P);
    }

    public String rootHex(List<String> leafHexes) {
        if (leafHexes == null || leafHexes.isEmpty()) {
            return "0x" + "00".repeat(64);
        }

        List<BigInteger> level = new ArrayList<>(leafHexes.size());
        for (String hx : leafHexes) {
            BigInteger leafFr = fr(hx);
            BigInteger leafHash = poseidon.hash(leafFr);
            level.add(leafHash);
        }

        while (level.size() > 1) {
            List<BigInteger> next = new ArrayList<>((level.size() + 1) / 2);
            for (int i = 0; i < level.size(); i += 2) {
                BigInteger left = level.get(i);
                BigInteger right = (i + 1 < level.size()) ? level.get(i + 1) : left; // 홀수면 복제
                BigInteger parent = poseidon.hash(left, right);
                next.add(parent);
            }
            level = next;
        }

        byte[] be32 = bigIntToBe32(level.get(0));

        return to0xHex(be32);
    }

    public record Path(List<String> siblingsHex32, List<Integer> indexBits) {}

    public Path pathOf(Long sessionId, int leafIndex) {
        List<String> leafHexes = applicantsSnapshotItemRepository.findWinnerLeafHexes(sessionId);
        if (leafHexes == null || leafHexes.isEmpty()) {
            throw new CustomException(ErrorStatus.SNAPSHOT_WINNER_LEAFS_EMPTY);
        }
        if (leafIndex < 0 || leafIndex >= leafHexes.size()) {
            throw new CustomException(ErrorStatus.LOTTERY_INVALID_INDEX);
        }

        List<BigInteger> level = new ArrayList<>(leafHexes.size());
        for (String hx : leafHexes) {
            BigInteger leafFr = fr(hx);
            level.add(poseidon.hash(leafFr));
        }

        List<String> siblings = new ArrayList<>(depth);
        List<Integer> indexBits = new ArrayList<>(depth);

        int idx = leafIndex;
        int d = 0;

        while (level.size() > 1 && d < depth) {
            int n = level.size();

            int sibIdx = idx ^ 1;
            if (sibIdx >= n) sibIdx = idx;

            siblings.add(to0xHex(bigIntToBe32(level.get(sibIdx))));
            indexBits.add(idx & 1);

            List<BigInteger> next = new ArrayList<>((n + 1) / 2);
            for (int i = 0; i < n; i += 2) {
                BigInteger L = level.get(i);
                BigInteger R = (i + 1 < n) ? level.get(i + 1) : L;
                next.add(poseidon.hash(L, R));
            }

            level = next;
            idx >>= 1;
            d++;
        }

        while (d < depth) {
            siblings.add(to0xHex(bigIntToBe32(level.get(0))));
            indexBits.add(0);
            d++;
        }

        return new Path(siblings, indexBits);
    }

}
