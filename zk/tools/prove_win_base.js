#!/usr/bin/env node

const fs = require("fs");
const path = require("path");
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

        const input = JSON.parse(fs.readFileSync(inputPath, "utf8"));

        const { generateWitness } = require(path.resolve(genPath));
        const wtnsBuff = await generateWitness(input, wasmPath);

        const { proof, publicSignals } = await plonk.prove(zkeyPath, wtnsBuff);

        const calldata = await plonk.exportSolidityCallData(proof, publicSignals);
        const [proofHex, pubs] = JSON.parse(calldata);

        process.stdout.write(JSON.stringify({ proofHex, publicSignals: pubs }));
    } catch (e) {
        console.error(e);
        process.exit(1);
    }
})();