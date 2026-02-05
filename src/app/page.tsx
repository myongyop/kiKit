"use client";

import { useState } from "react";
import { invoke } from "@tauri-apps/api/core";

export default function Home() {
  const [printers, setPrinters] = useState<any[]>([]);
  const [selectedPrinter, setSelectedPrinter] = useState<string>("");
  const [status, setStatus] = useState<string>("");

  const listPrinters = async () => {
    try {
      setStatus("Listing printers...");
      const res: any = await invoke("plugin:printer|list_printers");
      console.log("Printers:", res);
      setPrinters(res.printers || []);
      setStatus(`Found ${res.printers?.length || 0} printers`);
    } catch (e: any) {
      console.error(e);
      setStatus("Error listing printers: " + e);
    }
  };

  const connectPrinter = async () => {
    if (!selectedPrinter) {
      setStatus("No printer selected");
      return;
    }
    try {
      setStatus(`Connecting to ${selectedPrinter}...`);
      const res: any = await invoke("plugin:printer|connect_printer", {
        deviceName: selectedPrinter,
      });
      console.log("Connect result:", res);
      if (res.success) {
        setStatus("Connected (Permission granted)");
      } else {
        setStatus("Connection failed: " + res.message);
      }
    } catch (e: any) {
      console.error(e);
      setStatus("Error connecting: " + e);
    }
  };

  const printTest = async () => {
    if (!selectedPrinter) {
      setStatus("No printer selected");
      return;
    }
    try {
      setStatus("Printing...");
      // Simple ESC/POS "Hello World" + Feed
      // ESC @ (Init) = 27, 64
      // Text "Hello World"
      // LF = 10
      // Feed 3 lines
      const text = "Hello World via Tauri!\n\n\n";
      const encoder = new TextEncoder();
      const textBytes = Array.from(encoder.encode(text));
      const data = [27, 64, ...textBytes];

      const res: any = await invoke("plugin:printer|print_raw", {
        deviceName: selectedPrinter,
        data: data,
      });
      console.log("Print result:", res);
      if (res.success) {
        setStatus("Print success!");
      } else {
        setStatus("Print failed");
      }
    } catch (e: any) {
      console.error(e);
      setStatus("Error printing: " + e);
    }
  };

  return (
    <div style={{ padding: 20 }}>
      <h1>Printer Test</h1>

      <div style={{ marginBottom: 20 }}>
        <button onClick={listPrinters} style={{ padding: 10, marginRight: 10 }}>
          List Printers
        </button>
      </div>

      <div style={{ marginBottom: 20 }}>
        <select
          style={{ padding: 10, width: "100%", marginBottom: 10 }}
          value={selectedPrinter}
          onChange={(e) => setSelectedPrinter(e.target.value)}
        >
          <option value="">Select a printer</option>
          {printers.map((p) => (
            <option key={p.deviceName} value={p.deviceName}>
              {p.productName} ({p.deviceName})
            </option>
          ))}
        </select>
        <button onClick={connectPrinter} style={{ padding: 10, marginRight: 10 }}>
          Connect (Request Permission)
        </button>
      </div>

      <div style={{ marginBottom: 20 }}>
        <button onClick={printTest} style={{ padding: 10 }}>
          Print Test Receipt
        </button>
      </div>

      <div style={{ marginTop: 20, padding: 10, border: "1px solid #ccc" }}>
        <strong>Status:</strong> {status}
      </div>
    </div>
  );
}
