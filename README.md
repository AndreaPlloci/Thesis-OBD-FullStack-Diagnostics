# Thesis OBD Data Analyzer - Full-Stack Ecosystem 🚗

This repository hosts a complete end-to-end diagnostic system that connects vehicle hardware to AI-driven predictive maintenance reports.



## 🏗️ System Architecture
1. **Data Acquisition (Mobile):** A native Android app scans the vehicle's ECU via Bluetooth to retrieve real-time data like battery voltage and Diagnostic Trouble Codes (DTC).
2. **Transmission:** Structured payloads are sent via secure webhooks to the Jarvis backend using Retrofit with a 60-second timeout configuration for high reliability.
3. **AI Processing (Backend):** An n8n orchestration engine receives the data, performs RAG (Retrieval-Augmented Generation) against an automotive knowledge base in Pinecone, and generates a detailed report.
4. **Delivery:** The final report is formatted in HTML and delivered via Gmail.

## 📱 Mobile App Features (Kotlin)
* **Jetpack Compose UI:** Modern declarative interface for vehicle selection and scan monitoring.
* **Robust Networking:** Implementation of the Singleton pattern for the `RetrofitClient` to manage API calls efficiently.
* **Dynamic Database:** Supports Audi A3 and Alfa Romeo (159, Giulia) with specific engine and transmission mapping.

## 🤖 AI & Automation (n8n)
* **Vector Search:** Queries the `automotive-knowledge-base` index in Pinecone to find relevant technical procedures.
* **Intelligent Analysis:** Uses a Senior Mechanical Engineer prompt to interpret DTC codes and provide predictive maintenance advice.

---
*Developed as a core part of an Engineering Thesis focusing on Industrial IoT and AI optimization.*
