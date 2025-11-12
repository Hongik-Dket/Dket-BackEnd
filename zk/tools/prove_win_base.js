#!/usr/bin/env node

const fs = require("fs");
const os = require("os");
const path = require("path");
const { spawnSync } = require("child_process");
const { plonk } = require("snarkjs");

function arg(name) {
    const i = process.argv.indexOf(name);
    if (i < 0 || i === process.argv.length - 1) {
        console.error(`Missing arg ${name}`);
        process.exit(2);
    }
    return process.argv[i + 1];
}

(async () => {
    try {
        const wasmPath  = arg("--wasm");
        const genPath   = arg("--gen");
        const zkeyPath  = arg("--zkey");
        const inputPath = arg("--input");

        const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), "wtns-"));
        const wtnsPath = path.join(tmpDir, "out.wtns");

        const gen = spawnSync(process.execPath, [genPath, wasmPath, inputPath, wtnsPath], {
            encoding: "utf8"
        });
        if (gen.status !== 0) {
            const msg = (gen.stderr || gen.stdout || "").trim();
            throw new Error(`witness generation failed: ${msg}`);
        }
        if (!fs.existsSync(wtnsPath)) throw new Error("witness file not created");

        const wtnsBuff = fs.readFileSync(wtnsPath);
        const { proof, publicSignals } = await plonk.prove(zkeyPath, wtnsBuff);

        const calldata = (await plonk.exportSolidityCallData(proof, publicSignals)).trim();
        const m = calldata.match(/^\s*(\[[\s\S]*?\])\s*(\[[\s\S]*\])\s*$/);
        if (!m) { console.error("Unexpected calldata format"); process.exit(1); }

        const proofArr = JSON.parse(m[1]);
        const pubsArr  = JSON.parse(m[2]).map(String);

        process.stdout.write(JSON.stringify({ proof: proofArr, publicSignals: pubsArr }) + "\n");

        try { fs.rmSync(tmpDir, { recursive: true, force: true }); } catch {}
        process.exit(0);
    } catch (e) {
        process.stderr.write(e && e.message ? e.message : String(e));
        process.exit(1);
    }
})();