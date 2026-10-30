import { useEffect, useState } from "react";
import { Client, type IMessage } from "@stomp/stompjs";
import { Activity, Radio } from "lucide-react";
import { api, API_URL } from "../api/client";

interface BackgroundJob {
  jobId: number;
  orderId: number;
  jobType: string;
  status: "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED";
  message: string;
  createdAt: string;
}

export function BackgroundJobMonitor() {
  const [jobs, setJobs] = useState<BackgroundJob[]>([]);
  const [connection, setConnection] = useState("Connecting");

  useEffect(() => {
    let active = true;
    void api.get<BackgroundJob[]>("/api/jobs").then(({ data }) => {
      if (active) setJobs(data);
    }).catch(() => {
      if (active) setConnection("Could not load job history");
    });

    const socketBase = API_URL || `${window.location.protocol}//${window.location.host}`;
    const client = new Client({
      brokerURL: `${socketBase.replace(/^http/, "ws")}/ws-raithu`,
      reconnectDelay: 5000,
      connectHeaders: { Authorization: `Bearer ${localStorage.getItem("raithu_token") ?? ""}` },
      onConnect: () => {
        setConnection("Live");
        client.subscribe("/topic/jobs", (message: IMessage) => {
          const job = JSON.parse(message.body) as BackgroundJob;
          setJobs((current) => [job, ...current.filter((entry) => entry.jobId !== job.jobId)]);
        });
      },
      onWebSocketClose: () => setConnection("Reconnecting"),
      onStompError: () => setConnection("Reconnecting"),
    });
    client.activate();
    return () => {
      active = false;
      void client.deactivate();
    };
  }, []);

  return (
    <section className="job-monitor">
      <div className="section-heading job-heading"><div><span className="eyebrow">THE BEHIND-THE-SCENES</span><h2>Order activity</h2><p>Confirmation emails, invoices and notifications.</p></div>
        <span className={`live-pill ${connection === "Live" ? "is-live" : ""}`}><Radio size={14} /> {connection}</span></div>
      {jobs.length === 0 ? <div className="job-empty"><Activity size={22} /> New paid orders will show their progress here.</div> : (
        <div className="job-table-wrap"><table><thead><tr><th>Order</th><th>Task</th><th>Status</th><th>Update</th></tr></thead>
          <tbody>{jobs.map((job) => <tr key={job.jobId}><td>#{job.orderId}</td><td>{job.jobType.toLowerCase()}</td><td><span className={`job-status status-${job.status.toLowerCase()}`}>{job.status}</span></td><td>{job.message}</td></tr>)}</tbody>
        </table></div>
      )}
    </section>
  );
}
