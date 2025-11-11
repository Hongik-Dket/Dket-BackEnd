package com.example.demo.global.zkp.win;

import com.example.demo.global.response.exception.CustomException;
import com.example.demo.global.response.status.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.example.demo.global.util.Hexes.bigIntToBe32;
import static com.example.demo.global.util.Hexes.to0xHex;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WinProverService {

    private final WinInputBuilder inputBuilder;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${zk.root}")
    private String zkRoot;

    @Value("${zk.nodeExec}")
    private String nodeExec;

    private Path wasm()  { return Path.of(zkRoot, "build", "win_base_js", "win_base.wasm"); }
    private Path gen()   { return Path.of(zkRoot, "build", "win_base_js", "generate_witness.js"); }
    private Path zkey()  { return Path.of(zkRoot, "build", "win_base.zkey"); }
    private Path prover(){ return Path.of(zkRoot, "tools", "prove_win_base.js"); }

    @Data
    public static class WinProof {
        private String proofHex;
        private String paymentNullifierHex32;
    }

    public WinProof prove(Long sessionId, int leafIndex, String icHex) {
        try {
            Map<String, Object> json = inputBuilder.build(sessionId, leafIndex, icHex);

            Path tmpInput = Files.createTempFile("win-input-", ".json");
            om.writeValue(tmpInput.toFile(), json);

            var pb = new ProcessBuilder(
                    nodeExec, prover().toString(),
                    "--wasm",  wasm().toString(),
                    "--gen",   gen().toString(),
                    "--zkey",  zkey().toString(),
                    "--input", tmpInput.toString()
            );
            pb.redirectErrorStream(true);

            var p = pb.start();
            var timeout = Duration.ofMinutes(2);
            if (!p.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)) {
                p.destroyForcibly();
                throw new CustomException(ErrorStatus.ZKP_PROVE_TIMEOUT);
            }

            String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            if (p.exitValue() != 0) {
                throw new CustomException(ErrorStatus.ZKP_INVALID_RETURN);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> map = om.readValue(out, Map.class);
            String proofHex = (String) map.get("proofHex");
            List<String> pubs = (List<String>) map.get("publicSignals");
            if (pubs == null || pubs.size() != 3) {
                log.error("prove stderr/stdout: {}", out);
                throw new CustomException(ErrorStatus.ZKP_INVALID_RETURN);
            }

            BigInteger nullifier = new BigInteger(pubs.get(2));

            var winProof = new WinProof();
            winProof.setProofHex(proofHex);
            winProof.setPaymentNullifierHex32(to0xHex(bigIntToBe32(nullifier)));

            Files.deleteIfExists(tmpInput);

            return winProof;
        } catch (IOException | InterruptedException e) {
            log.error("Session [{}], leafIndex: {} WIN prove 실패", sessionId, leafIndex, e);
            throw new CustomException(ErrorStatus.ZKP_PROVE_FAILED);
        }
    }
}