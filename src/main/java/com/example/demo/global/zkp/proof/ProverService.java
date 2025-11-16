package com.example.demo.global.zkp.proof;

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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProverService {

    private final InputBuilder inputBuilder;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${ZK_ROOT:/opt/zk}")
    private String zkRoot;

    @Value("${NODE_BIN:node}")
    private String nodeExec;

    @Value("${zk.proveTimeoutSec:300}")
    private long proveTimeoutSec;

    private Path prover(){ return Path.of(zkRoot, "tools", "prove_membership.js"); }

    @Data
    public static class Proof {
        private List<String> proof;
        private List<String> publicSignals;
    }

    public Proof proveWin(Long sessionId, int leafIndex, String icHex) {
        try {
            Map<String, Object> json = inputBuilder.buildWinInput(sessionId, leafIndex, icHex);

            Path tmpInput = Files.createTempFile("win-input-", ".json");
            om.writeValue(tmpInput.toFile(), json);

            Path wasm = Path.of(zkRoot, "build", "win_js", "win.wasm");
            Path gen = Path.of(zkRoot, "build", "win_js", "generate_witness.js");
            Path zkey = Path.of(zkRoot, "build", "win.zkey");

            Proof proof = prove(sessionId, leafIndex, wasm, gen, zkey, tmpInput);
            log.info("WIN prove success: sessionId={}, leafIndex={}", sessionId, leafIndex);

            return proof;
        } catch (IOException | InterruptedException e) {
            log.error("WIN prove failed: sessionId={}, leafIndex={}", sessionId, leafIndex, e);
            throw new CustomException(ErrorStatus.ZKP_PROVE_FAILED);
        }
    }

    public Proof proveOwn(Long sessionId, int leafIndex, String icHex) {
        try {
            Map<String, Object> json = inputBuilder.buildOwnInput(sessionId, leafIndex, icHex);

            Path tmpInput = Files.createTempFile("own-input-", ".json");
            om.writeValue(tmpInput.toFile(), json);

            Path wasm = Path.of(zkRoot, "build", "own_js", "own.wasm");
            Path gen = Path.of(zkRoot, "build", "own_js", "generate_witness.js");
            Path zkey = Path.of(zkRoot, "build", "own.zkey");

            Proof proof = prove(sessionId, leafIndex, wasm, gen, zkey, tmpInput);
            log.info("OWN prove success: sessionId={}, leafIndex={}", sessionId, leafIndex);

            return proof;
        } catch (IOException | InterruptedException e) {
            log.error("OWN prove failed: sessionId={}, leafIndex={}", sessionId, leafIndex, e);
            throw new CustomException(ErrorStatus.ZKP_PROVE_FAILED);
        }
    }

    private Proof prove(Long sessionId, int leafIndex,
                        Path wasm, Path gen, Path zkey,
                        Path tmpInput) throws IOException, InterruptedException {

        var pb = new ProcessBuilder(
                nodeExec, prover().toString(),
                "--wasm",  wasm.toString(),
                "--gen",   gen.toString(),
                "--zkey",  zkey.toString(),
                "--input", tmpInput.toString()
        );
        pb.redirectErrorStream(true);

        log.info("Spawn Node:... : sessionId={}, leafIndex={}", sessionId, leafIndex);
        var p = pb.start();
        var timeout = Duration.ofSeconds(proveTimeoutSec);
        if (!p.waitFor(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)) {
            p.destroyForcibly();
            throw new CustomException(ErrorStatus.ZKP_PROVE_TIMEOUT);
        }

        log.info("Parsing results... : sessionId={}, leafIndex={}", sessionId, leafIndex);
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (p.exitValue() != 0) {
            log.error(out);
            throw new CustomException(ErrorStatus.ZKP_INVALID_RETURN);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> map = om.readValue(out, Map.class);

        List<String> proof = (List<String>) map.get("proof");
        List<String> pubs  = (List<String>) map.get("publicSignals");

        if (proof == null || proof.size() != 24 || pubs == null || pubs.size() != 3) {
            throw new CustomException(ErrorStatus.ZKP_INVALID_RETURN);
        }

        var result = new Proof();
        result.setProof(proof);
        result.setPublicSignals(pubs);

        Files.deleteIfExists(tmpInput);

        return result;
    }
}