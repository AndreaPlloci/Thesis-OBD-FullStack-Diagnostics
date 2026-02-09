# Jarvis OBD: Full-Stack AI Automotive Diagnostic Ecosystem 🚗🤖

**Jarvis OBD** is a high-performance diagnostic and predictive maintenance ecosystem designed to bridge the gap between raw vehicular telemetry and professional mechanical engineering. Integrated into the broader **Jarvis Infrastructure**, this project leverages a native Android client and a sophisticated **RAG (Retrieval-Augmented Generation)** backend to deliver industrial-grade vehicle health insights.

Unlike standard OBD scanners, this system performs real-time correlation between ECU live data and a vectorized technical knowledge base, enabling autonomous AI agents to provide context-aware troubleshooting.

---

## 🏗️ Infrastructure Architecture (The Backbone)

The ecosystem is hosted on a private, headless Windows Server node ("Jarvis"), engineered for 24/7 orchestration and high availability.

* **Security & Connectivity:** Communication is encapsulated within a **Tailscale VPN Mesh**, creating a zero-trust network environment. This allows the Android client to reach the n8n webhooks securely without exposing any public ports.
* **Proactive System Health:** A dedicated **Health Monitor** polls system vitals (CPU, RAM, Disk I/O) every 30 minutes via **PowerShell and WMI**. Telemetry is logged into a local SQL instance to ensure infrastructure stability during heavy RAG operations.
* **Network Optimization:** **AdGuard Home** is integrated as a network-wide DNS sinkhole, managing telemetry traffic and optimizing query latency.

---

## 📱 Mobile Engineering & OBD-II Protocol

The Android application is a native client built with **Kotlin** and **Jetpack Compose**, designed for low-latency communication with ELM327-compatible hardware.

### Protocol Implementation
The client implements a robust polling mechanism for the **ISO 15765-4 (CAN 11/500)** protocol, managing sequential Parameter ID (PID) requests:
* **Real-time Monitoring:** Tracks critical engine metrics including **Fuel Rail Pressure**, **Mass Air Flow (MAF)**, **Engine Load**, and **Coolant Temperature (ECT)**.
* **Fault Management:** Performs full scans of Diagnostic Trouble Codes (DTCs), retrieving both active and pending errors from the ECU memory.
* **Network Layer:** Utilizes **Retrofit 2** with asynchronous Coroutines to dispatch diagnostic payloads to the AI backend via secure webhooks.



---

## 🧠 AI Orchestration & Technical RAG

The core intelligence of the system relies on a **Retrieval-Augmented Generation (RAG)** pipeline that transforms unstructured technical manuals into actionable data.

* **Vectorization Strategy:** Official workshop manuals (specifically optimized for Alfa Romeo and Audi platforms) are processed using a **Recursive Character Text Splitting** strategy. We utilize a 1000-character window with a 200-character overlap to preserve the integrity of technical tables, torque specifications, and mechanical tolerances.
* **Vector Store:** Embeddings are indexed in a **Pinecone Vector Store**, allowing for high-dimensional semantic searches.
* **Autonomous Reasoning:** An AI Agent (utilizing **GPT-4o-mini**) receives the live OBD-II payload, retrieves the relevant technical context from Pinecone, and synthesizes a professional diagnostic report.



---

## 📊 Data Engineering & SQL Persistence

A critical evolution of the project was the migration from flat-file storage to a structured relational database environment.

* **SQL Integration:** The system utilizes **MariaDB/MySQL** for high-precision data persistence.
* **Relational Schema:** The database manages complex relationships between User IDs, Vehicle VINs, Diagnostic Sessions, and AI-generated Maintenance Reports.
* **Data Sovereignty:** By hosting the database locally on the Jarvis node, the system ensures total privacy of sensitive financial and technical records, facilitating advanced historical trend analysis and predictive maintenance scheduling.



---

## 🛠️ Tech Stack

### Mobile Development
* **Languages:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Networking:** Retrofit 2, OkHttp, Gson
* **Hardware:** Bluetooth API (ELM327 / OBD-II)

### AI & Automation
* **Orchestration:** n8n
* **LLMs:** OpenAI GPT-4o-mini, Google Gemini 2.0 Flash
* **Vector DB:** Pinecone
* **Embeddings:** OpenAI `text-embedding-3-small`

### Infrastructure
* **OS:** Windows Server (Headless)
* **Database:** MariaDB / MySQL
* **Scripting:** PowerShell, JavaScript (Node.js)
* **Networking:** Tailscale VPN, AdGuard Home

---

## 📂 Project Structure

```text
├── android-app/             # Native Android source code
│   ├── ui/                  # Material 3 Compose screens & themes
│   ├── network/             # API services and Retrofit config
│   ├── data/                # Data classes for OBD PIDs and ECU logs
│   └── manager/             # Bluetooth and protocol logic
├── n8n-workflows/           # JSON exports for backend orchestration
│   ├── diagnostic_rag.json  # Main AI Diagnostic & RAG agent
│   ├── server_monitor.json  # System health and SQL logging
│   └── financial_cfo.json   # NL2SQL finance management module
└── README.md
