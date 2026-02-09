# Jarvis OBD: Full-Stack AI Automotive Diagnostic Ecosystem 🚗🤖

**Jarvis OBD** is a professional-grade diagnostic and predictive maintenance ecosystem. It bridges the gap between raw vehicular telemetry (ECU data) and senior mechanical engineering expertise. By combining a native **Android client** with a specialized **RAG (Retrieval-Augmented Generation)** backend, the system delivers high-fidelity, context-aware vehicle health reports.

The project is specifically engineered to interpret live engine dynamics against a vectorized knowledge base of official workshop manuals, with deep optimization for **Alfa Romeo (159, Giulia)** and **Audi (A3)** platforms.



---

## 🏗️ System Architecture

### 1. Mobile Engineering (Kotlin & OBD-II)
The Android client, built with **Jetpack Compose**, serves as the hardware-to-cloud gateway, managing the complex physical-to-digital data layer.
* **Protocol Implementation:** Implements low-latency polling for the **ISO 15765-4 (CAN 11/500)** protocol via ELM327 hardware.
* **Advanced PID Monitoring:** The application performs real-time acquisition of critical engine parameters:
    * **Fuel Rail Pressure:** Direct monitoring of the Common Rail injection system.
    * **MAF (Mass Air Flow):** Evaluating intake efficiency and air-fuel ratio consistency.
    * **Calculated Engine Load:** Live stress analysis of the internal combustion process.
    * **Thermal Cycles:** Tracking ECT (Coolant) and IAT (Intake Air) for efficiency mapping.
* **Network Layer:** Utilizes **Retrofit 2** and **Coroutines** to dispatch encrypted diagnostic payloads to the Jarvis infrastructure via secure webhooks.



### 2. AI Diagnostic Orchestration (n8n & RAG)
The backend functions as a **Senior Mechanical Engineer** agent, utilizing a sophisticated RAG pipeline to eliminate "hallucinations" and ensure grounding in technical documentation.
* **Technical RAG Pipeline:** Official workshop manuals are processed using **Recursive Character Text Splitting** (1000 char window / 200 overlap) to preserve the integrity of torque specifications and mechanical tolerances in a **Pinecone Vector Store**.
* **7-Pillar Standardized Audit:** The AI follows a mandatory engineering protocol for every report:
    1. **Timing (Belt/Chain):** Automatic risk flagging based on mileage vs. official intervals.
    2. **Lubrication:** Analysis of oil degradation cycles.
    3. **Transmission:** Proactive clutch and fly-wheel wear estimation for high-mileage units.
    4. **Suspension:** Focus on silent blocks and control arm integrity (specific to Alfa/Audi platforms).
    5. **Post-Treatment (DPF/EGR):** Saturation risk analysis based on engine load profiles.
    6. **Braking Systems:** Theoretical wear tracking.
    7. **Electrical:** Grounded analysis of battery voltage stability and IAT sensor health.

<img width="944" height="620" alt="workflow" src="https://github.com/user-attachments/assets/5090b32e-d25f-4612-9324-e4b308778f39" />


### 3. Infrastructure & Data Sovereignty
* **Jarvis Server:** A private, headless Windows Server node hosting the entire automation stack.
* **Security:** Communication is encapsulated within a **Tailscale VPN Mesh** (Zero-Trust), ensuring no public ports are exposed.
* **Persistence:** All diagnostic sessions and maintenance histories are stored on a local **MariaDB/MySQL** instance, ensuring total data privacy and sovereignty.

---

## 🛠️ Tech Stack

* **Mobile:** Kotlin, Jetpack Compose, Retrofit 2, OkHttp, Bluetooth API.
* **AI & Logic:** n8n (Orchestration), OpenAI GPT-4o-mini, Pinecone (Vector DB), LangChain.
* **Infrastructure:** Windows Server, MariaDB/MySQL, Tailscale VPN, AdGuard Home.

---

## 📂 Project Structure

```text
├── android-app/             # Native Kotlin Source Code
│   ├── ui/                  # Compose screens (Selection, Reading, Diagnostic)
│   ├── network/             # Retrofit client for Webhook communication
│   ├── data/                # Data Models (ObdData, ReportRequest, MaintenanceWork)
│   └── manager/             # ObdManager: Bluetooth and PID logic (ISO 15765-4)
├── n8n-workflows/           # AI Logic & Automation
│   ├── diagnostic_agent.json # RAG Pipeline, 7-Pillar Audit, and HTML Generator
│   └── data_ingestion.json   # Manual vectorization and Pinecone indexing
└── README.md
