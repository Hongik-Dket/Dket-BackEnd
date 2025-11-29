# :Dket Backend Service

**:Dket(디켓)** 의 백엔드 서버 리포지토리입니다.
이 프로젝트는 **Spring Boot**를 기반으로 구축되었으며, 블록체인(Ethereum Sepolia)과의 통신, IPFS 메타데이터 관리, 그리고 **영지식 증명(ZKP)을 위한 고성능 연산(Rust JNI)** 을 담당합니다.

---

## 🛠 Tech Stack

### Framework & Language
* **Java 17+** / **Spring Boot 3.x**
* **Rust** (for Poseidon Hash Native Interface)
* **Gradle** (Build Tool)

### Blockchain & ZKP
* **Web3j**: 이더리움 스마트 컨트랙트 상호작용
* **Circom & SnarkJS**: ZKP 회로(Circuit) 및 증명(Proof) 관련 파일 관리 (`zk/` 디렉토리)
* **JNI (Java Native Interface)**: Rust로 구현된 Poseidon 해시 함수 연동

### Infrastructure & External APIs
* **AWS RDS (MySQL)**: 메인 데이터베이스
* **AWS S3**: 배너, 포스터 등 정적 이미지 호스팅
* **Pinata (IPFS)**: NFT 메타데이터 및 포토카드 이미지의 탈중앙화 저장

---

## 📂 Project Structure

```bash
dket-backend
├── native/poseidon             # Rust Project: Poseidon Hash 라이브러리 (JNI)
├── zk                          # ZKP Project: Circom 회로 및 빌드 스크립트
│   ├── circuits                # .circom 회로 파일 (win, own 등)
│   ├── scripts                 # 빌드 및 Ptau 다운로드 스크립트
│   └── build                   # (생성됨) 컴파일된 .r1cs, .wasm, .zkey 아티팩트
├── src/main/java               # Spring Boot Application
│   ├── domain                  # 도메인별 비즈니스 로직
│   │   ├── apply               # 티켓 응모 관리 (Keccak Hashing)
│   │   ├── concert             # 공연 정보 및 회차 관리
│   │   ├── lottery             # 추첨 프로세스 (VRF 연동)
│   │   ├── main                # 메인 화면 데이터 구성
│   │   ├── metadata            # 포토카드 및 NFT 메타데이터 생성
│   │   ├── ownership           # 티켓 소유권 이력 관리 (Merkle Tree)
│   │   ├── proof               # ZKP 증명 생성 및 검증 (Entry/Win)
│   │   ├── resale              # 2차 거래(Resale) 및 가격 정책 관리
│   │   ├── ticket              # 티켓 발권, 이체(Transfer) 관리
│   │   └── user                # 사용자 관리 및 여권 인증
│   └── global                  # 전역 설정 및 공통 모듈
│       ├── base                # BaseEntity, Constants
│       ├── config              # AWS, Web3j, Swagger 등 설정
│       ├── infra               # 외부 인프라 연동 (Blockchain, S3, IPFS)
│       ├── response            # API 공통 응답 포맷 및 에러 처리
│       ├── security            # JWT 인증 및 필터 설정
│       ├── util                # 암호화, 인코딩 유틸리티 (KeccakUtil 등)
│       └── zkp                 # Poseidon 해시 및 Circuit 입력값 빌더
└── build.gradle                # Gradle 빌드 설정
````

-----

## 🚀 Getting Started

이 프로젝트를 실행하기 위해서는 **Java(Spring Boot), Rust(Native Lib), Node.js(ZKP Circuit)** 세 가지 환경의 빌드가 모두 완료되어야 합니다.

### 1\. Prerequisites

  * **JDK 17** 이상
  * **Rust (Cargo)** ([Install Guide](https://www.rust-lang.org/tools/install))
  * **Node.js (v16+) & NPM**
  * **Circom** ([Install Guide](https://docs.circom.io/getting-started/installation/))
  * **MySQL Server**

### 2\. Build Native Library (Rust)

머클 트리 연산 가속을 위한 Poseidon 라이브러리를 빌드합니다.

```bash
cd native/poseidon
cargo build --release
cd ../..
```

### 3\. Build ZKP Circuits (Node.js)

`.circom` 회로를 컴파일하고 Trusted Setup(Ptau)을 수행하여 `.zkey`와 `.wasm`을 생성합니다.

```bash
cd zk

# 1. 의존성 설치 (snarkjs, circomlib)
npm install

# 2. Powers of Tau (Ptau) 파일 다운로드
npm run ptau

# 3. 회로 컴파일 및 키 생성 (build.sh 실행)
# 결과물은 zk/build 폴더에 생성됩니다.
npm run build

cd ..
```

### 4\. Configuration (application.yml)

`src/main/resources/application.yml`의 환경 변수를 설정합니다.

```yaml
server:
  port: 8080

spring:
  datasource:
    url: ${DB_URL} 
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
      enabled: true
  session:
    timeout: 5m
  web:
    encoding:
      charset: UTF-8
      enabled: true
      force: true
    locale:
      fixed: Asia/Seoul
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true

cloud:
  aws:
    s3:
      bucket: ${AWS_S3_BUCKET}
      region:
        static: ${AWS_REGION}
      stack:
        auto: false
    credentials:
      accessKey: ${AWS_ACCESS_KEY}
      secretKey: ${AWS_SECRET_KEY}

web3:
  chain-id: 11155111 # Sepolia
  network-url: ${BLOCKCHAIN_RPC_URL}
  private-key: ${ADMIN_WALLET_PRIVATE_KEY} # 선민팅을 수행할 관리자 지갑
  nft-contract-address: ${NFT_CONTRACT_ADDRESS}
  resale-contract-address: ${RESALE_CONTRACT_ADDRESS}

pinata:
  api-key: ${PINATA_API_KEY}
  api-secret: ${PINATA_SECRET_KEY}
  upload-url: "[https://api.pinata.cloud/pinning/pinFileToIPFS](https://api.pinata.cloud/pinning/pinFileToIPFS)"
  gateway: ${PINATA_GATEWAY} # e.g., "your-domain.mypinata.cloud"

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: always

zk:
  depth: 20
  proveTimeoutSec: 600
```

### 5\. Run Application

```bash
./gradlew clean build
./gradlew bootRun
```

-----

## 🔑 Key Features & Logic

### 1\. Fair Randomness & Metadata Shuffling

  * **VRF 기반 셔플링:** 공연 등록 시 **Chainlink VRF** 난수를 시드(Seed)로 활용하여, 전체 좌석과 포토카드 조합(Metadata)을 무작위로 섞습니다.
  * **관리자 선민팅 (Pre-minting):** 섞여진 메타데이터 순서대로 **관리자(Admin) 지갑에 티켓 전량을 미리 민팅**하여, 블록체인 상에 공정하게 기록된 상태로 배포를 준비합니다.

### 2\. Integrity & Ownership Management

  * **응모자 무결성 (Keccak Hash):** 사용자의 응모 내역은 DB에 저장되며, 응모 마감 후 전체 리스트를 정렬하여 **Keccak 해시 함수**로 단일 해시값(Commitment)을 생성, 블록체인에 기록하여 데이터 조작을 방지합니다.
  * **소유자 관리 (Merkle Tree):** 당첨자 및 실제 티켓 소유자 정보는 **Merkle Tree**로 관리됩니다. 소유권 변동 시마다 Root 값이 온체인에 업데이트되어 ZKP 증명의 기준점이 됩니다.

### 3\. Secure Resale Ecosystem

  * **System-Authorized Trading (EIP-712):** 리세일 거래 시 시스템이 발급한 서명을 검증하여, 중복 거래(Race Condition)를 방지하고 플랫폼 외부에서의 비정상적인 거래를 차단합니다.
  * **가격 상한 & 로열티:**
      * **Price Cap:** 원가의 최대 **120%** 까지만 판매 가격을 설정할 수 있어 과도한 프리미엄을 방지합니다.
      * **Royalty:** 판매 수익의 **10%** 는 자동으로 원작자(개최자)에게 배분되는 스마트 컨트랙트 로직이 적용되어 있습니다.

### 4\. Privacy-Preserving Entry (ZKP)

  * **프라이버시 입장:** 사용자는 개인키나 개인정보를 노출하지 않고, 오직 티켓 소유권만을 증명하는 **영지식 증명(ZKP)** QR코드를 통해 입장합니다.
  * **Secure Biometric Auth:** 기기 내 Secure Enclave와 연동된 생체 인증을 통해서만 증명을 생성할 수 있어, **계정 양도 및 대리 입장을 원천 차단**합니다.

-----

## 📜 License

This project is licensed under the MIT License.
