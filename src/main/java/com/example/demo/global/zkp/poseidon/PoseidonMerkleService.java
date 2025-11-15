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

import static com.example.demo.global.util.FrCodec.fr;
import static com.example.demo.global.util.Hexes.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PoseidonMerkleService {

    private final Poseidon poseidon;

    private final ApplicantsSnapshotItemRepository applicantsSnapshotItemRepository;

    @Value(value = "${zk.depth}")
    private int depth;

    private List<BigInteger> buildZeroes() {
        List<BigInteger> ZERO = new ArrayList<>(depth + 1);
        ZERO.add(BigInteger.ZERO);
        for (int i = 0; i < depth; i++) {
            ZERO.add(poseidon.hash(ZERO.get(i), ZERO.get(i)));
        }
        return ZERO;
    }

    public String rootHex(List<String> leafHexes) {
        List<BigInteger> ZERO = buildZeroes();

        if (leafHexes == null || leafHexes.isEmpty()) {
            return to0xHex(bigIntToBe32(ZERO.get(depth)));
        }

        List<BigInteger> level = new ArrayList<>(leafHexes.size());
        for (String hx : leafHexes) {
            level.add(fr(hx));
        }

        for (int i = 0; i < depth; i++) {
            int n = level.size();

            List<BigInteger> next = new ArrayList<>((n + 1) / 2);
            for (int j = 0; j < n; j += 2) {
                BigInteger L = level.get(j);
                BigInteger R = (j + 1 < n) ? level.get(j + 1) : ZERO.get(i);
                next.add(poseidon.hash(L, R));
            }
            level = next;
        }

        BigInteger root = level.get(0);

        return to0xHex(bigIntToBe32(root));
    }

    public record Path(List<String> siblingsHex32, List<Integer> indexBits) {}

    public Path pathOf(int leafIndex, List<String> leafHexes) {
        if (leafIndex < 0 || leafIndex >= leafHexes.size()) {
            throw new CustomException(ErrorStatus.LOTTERY_INVALID_INDEX);
        }

        List<BigInteger> ZERO = buildZeroes();

        List<BigInteger> level = new ArrayList<>(leafHexes.size());
        for (String hx : leafHexes) {
            level.add(fr(hx));
        }

        List<String> siblings = new ArrayList<>(depth);
        List<Integer> indexBits = new ArrayList<>(depth);

        int idx = leafIndex;

        for (int i = 0; i < depth; i++) {
            int n = level.size();

            int sibIdx = idx ^ 1;
            BigInteger siblingVal = (sibIdx < n) ? level.get(sibIdx) : ZERO.get(i);
            siblings.add(to0xHex(bigIntToBe32(siblingVal)));
            indexBits.add(idx & 1);

            List<BigInteger> next = new ArrayList<>((n + 1) / 2);
            for (int j = 0; j < n; j += 2) {
                BigInteger L = level.get(j);
                BigInteger R = (j + 1 < n) ? level.get(j + 1) : ZERO.get(i);
                next.add(poseidon.hash(L, R));
            }
            level = next;
            idx >>= 1;
        }

        return new Path(siblings, indexBits);
    }

}
