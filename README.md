# Jarvis OBD: AI-Driven Automotive Diagnostic Ecosystem 🚗🤖

**Jarvis OBD** is a high-performance diagnostic and predictive maintenance solution that bridges the gap between raw vehicle data and professional mechanical engineering. Integrated into the **Jarvis Ecosystem**, this project utilizes a native Android client and a sophisticated **RAG (Retrieval-Augmented Generation)** backend to provide context-aware vehicle health reports[cite: 4, 45, 46].

The system analyzes Diagnostic Trouble Codes (DTC) against a technical knowledge base of workshop manuals—specifically optimized for models like the **Alfa Romeo 159**—delivering industrial-grade insights through autonomous AI agents[cite: 5, 54, 55].

---

## 🏗 The Jarvis Infrastructure (Backbone)

The entire system runs on a custom-built, headless Windows server ("Jarvis") optimized for total autonomy and security[cite: 21, 24, 25]:

* **Network Security**: Protected by a **Tailscale VPN Mesh**, allowing encrypted remote access without open ports[cite: 28, 29, 30].
* **Infrastructure Management**: Features a **Telegram-based Remote Ops** bot for system administration, including /screenshot, /status, and /reboot commands with Chat-ID validation[cite: 8, 85, 86, 90].
* **Performance Monitoring**: A proactive **Health Monitor** polls 6 data sources every 10 minutes via PowerShell, using **Linear Regression** to predict storage trends and prevent downtime[cite: 58, 59, 71, 72].
* **DNS Filtering**: Integrates **AdGuard Home** as a DNS Sinkhole to optimize bandwidth and block trackers system-wide[cite: 36, 40, 41].

---

## ✨ Core Engineering Features

* **Industrial ETL Migration**: Successfully migrated the data pipeline from Google Sheets to a local **MariaDB (SQL)** instance[cite: 7, 75, 78]. This move ensures strong typing (DATETIME, FLOAT), total data sovereignty, and high-speed querying[cite: 79, 80, 81].
* **Technical RAG Pipeline**: Implements a recursive splitting strategy (1000 characters with 200 overlap) to maintain technical context during the vectorization of official manuals into **Pinecone**[cite: 6, 48, 49, 52].
* **Smart Diagnostics**: The Android client (Kotlin/Jetpack Compose) connects via Bluetooth to ELM327 adapters to monitor battery voltage and ECU error codes in real-time[cite: 1].
* **Predictive Reporting**: An AI Agent (GPT-4o-mini) cross-references live OBD data with the vector store to assign a color-coded health status (Green/Yellow/Red) and generate automated HTML reports via Gmail[cite: 54, 146].

---

## 🛠 Tech Stack

* **Frontend**: Kotlin, Jetpack Compose, Retrofit, Coroutines[cite: 1].
* **Automation/Orchestration**: n8n, PowerShell (WMI Automation), JavaScript[cite: 1, 59, 81].
* **AI/Vector Databases**: OpenAI GPT-4o-mini, Pinecone Vector Store, Gemini 2.0 Flash[cite: 67, 146, 147].
* **Database Management**: MariaDB (SQL), XAMPP Stack, FileMaker (for logic mapping)[cite: 1, 32, 101, 147].
* **Networking**: Tailscale VPN, AdGuard Home, RDP[cite: 1, 28, 36, 147].

---

## 📂 Project Structure

```text
├── android-app/             # Native Kotlin source code
│   ├── ui/                  # Jetpack Compose Material 3 screens
│   ├── network/             # Retrofit configurations for n8n webhooks
│   └── data/                # Data models for OBD and maintenance logs
├── n8n-workflows/           # JSON exports for automation logic
│   ├── jarvis_diagnostic.json # Main RAG and AI Analysis agent
│   ├── health_monitor.json  # PowerShell resource monitoring
│   └── nas_sort_agent.json  # GPT-based file categorization logic
└── README.md
