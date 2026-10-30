import { useCallback, useEffect, useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { api, errorMessage, formatSlot, type BookingRequest, type Session, type User } from "../api";
import { useAuth } from "../auth";

type Run = (action: () => Promise<unknown>) => Promise<void>;

export function Dashboard() {
  const { user } = useAuth();
  const [requests, setRequests] = useState<BookingRequest[]>([]);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [loaded, setLoaded] = useState(false);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    const [foundRequests, foundSessions] = await Promise.all([
      api<BookingRequest[]>("/requests"),
      api<Session[]>("/sessions"),
    ]);
    setRequests(foundRequests);
    setSessions(foundSessions);
    setLoaded(true);
  }, []);

  useEffect(() => {
    load().catch((e) => setError(errorMessage(e)));
  }, [load]);

  const run: Run = async (action) => {
    setError("");
    try {
      await action();
      await load();
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  if (!user) return null;
  if (user.roles.includes("ADMIN")) return <Navigate to="/admin" replace />;
  const incoming = requests.filter((r) => r.tutorId === user.id);
  const outgoing = requests.filter((r) => r.learnerId === user.id);
  const isTutor = user.roles.includes("TUTOR");
  const isLearner = user.roles.includes("LEARNER");

  return (
    <div className="stack">
      <div className="spread">
        <div>
          <h1>Welcome, {user.name}</h1>
          <p className="muted">{user.university}</p>
        </div>
        {isTutor && <Link to="/tutor-settings">Edit tutor profile</Link>}
      </div>
      {error && <p className="error" role="alert">{error}</p>}
      {!loaded && !error && <p className="muted">Loading…</p>}

      {isTutor && (
        <section>
          <h2>Requests for you</h2>
          {incoming.length === 0 && <p className="muted">No requests yet.</p>}
          <ul className="list">
            {incoming.map((r) => (
              <li key={r.id}>
                <span>
                  <strong>{r.learnerName}</strong> · {r.course.name}<br />
                  <span className="note">{formatSlot(r.date, r.startTime, r.endTime)}{r.message ? ` · "${r.message}"` : ""}</span>
                </span>
                {r.status === "PENDING" ? (
                  <span className="row">
                    <button onClick={() => run(() => api(`/requests/${r.id}/accept`, { method: "POST" }))}>Accept</button>
                    <button className="secondary" onClick={() => run(() => api(`/requests/${r.id}/decline`, { method: "POST" }))}>Decline</button>
                  </span>
                ) : <span className="badge">{r.status}</span>}
              </li>
            ))}
          </ul>
        </section>
      )}

      {isLearner && (
        <section>
          <h2>Your requests</h2>
          {outgoing.length === 0 && <p className="muted">Nothing yet. <Link to="/tutors">Find a tutor</Link> to send your first request.</p>}
          <ul className="list">
            {outgoing.map((r) => (
              <li key={r.id}>
                <span>
                  <strong>{r.course.name}</strong> with {r.tutorName}<br />
                  <span className="note">{formatSlot(r.date, r.startTime, r.endTime)}</span>
                </span>
                {r.status === "PENDING" ? (
                  <span className="row">
                    <span className="badge">PENDING</span>
                    <button className="secondary" onClick={() => run(() => api(`/requests/${r.id}/cancel`, { method: "POST" }))}>Cancel</button>
                  </span>
                ) : <span className="badge">{r.status}</span>}
              </li>
            ))}
          </ul>
        </section>
      )}

      <section>
        <h2>Sessions</h2>
        {sessions.length === 0 && <p className="muted">No sessions yet.</p>}
        <div className="stack">
          {sessions.map((s) => <SessionCard key={s.id} session={s} user={user} run={run} />)}
        </div>
      </section>
    </div>
  );
}

function SessionCard({ session, user, run }: { session: Session; user: User; run: Run }) {
  const isTutor = session.tutorId === user.id;
  return (
    <article className="card stack">
      <div className="spread">
        <div>
          <strong>{session.course.name}</strong>
          <p className="note">
            {formatSlot(session.date, session.startTime, session.endTime)} · {isTutor
              ? session.attendees.map((a) => a.name).join(", ") || "No learners yet"
              : `with ${session.tutorName}`}
          </p>
        </div>
        <span className="badge">{session.status}</span>
      </div>
      {session.meetingLink && <a href={session.meetingLink} target="_blank" rel="noreferrer">Join meeting</a>}
      {isTutor ? <TutorActions session={session} run={run} /> : <LearnerActions session={session} userId={user.id} run={run} />}
    </article>
  );
}

function TutorActions({ session, run }: { session: Session; run: Run }) {
  const [link, setLink] = useState(session.meetingLink ?? "");
  const [absent, setAbsent] = useState<string[]>([]);

  if (session.status !== "SCHEDULED") {
    return (
      <p className="note">
        {session.attendees.map((a) => `${a.name}: ${a.attended ? "attended" : "absent"}, ${a.paid ? "paid" : "unpaid"}`).join(" · ")}
      </p>
    );
  }

  const attendedIds = session.attendees.map((a) => a.learnerId).filter((id) => !absent.includes(id));
  return (
    <>
      <div className="row">
        <input aria-label="Meeting link" type="url" placeholder="https://meet.google.com/…" value={link} onChange={(e) => setLink(e.target.value)} />
        <button className="secondary" onClick={() => run(() => api(`/sessions/${session.id}/meeting-link`, { method: "PUT", body: { meetingLink: link } }))}>Save link</button>
      </div>
      {session.attendees.map((a) => (
        <label className="check" key={a.learnerId}>
          <input
            type="checkbox"
            checked={!absent.includes(a.learnerId)}
            onChange={(e) => setAbsent(e.target.checked ? absent.filter((id) => id !== a.learnerId) : [...absent, a.learnerId])}
          />
          {a.name} attended ({a.paid ? "paid" : "unpaid"})
        </label>
      ))}
      <div className="row">
        <button onClick={() => run(() => api(`/sessions/${session.id}/complete`, { method: "POST", body: { attendedLearnerIds: attendedIds } }))}>Mark completed</button>
        <button className="danger" onClick={() => run(() => api(`/sessions/${session.id}/cancel`, { method: "POST" }))}>Cancel session</button>
      </div>
    </>
  );
}

function LearnerActions({ session, userId, run }: { session: Session; userId: string; run: Run }) {
  const me = session.attendees.find((a) => a.learnerId === userId);
  const [method, setMethod] = useState("GCASH");
  const [rating, setRating] = useState("5");
  const [comment, setComment] = useState("");
  if (!me) return null;

  return (
    <>
      {session.status !== "CANCELLED" && (me.paid ? <p className="success">Paid ₱{session.costPerLearner}</p> : (
        <div className="stack">
          <div className="row">
            <span>₱{session.costPerLearner}</span>
            <select aria-label="Payment method" value={method} onChange={(e) => setMethod(e.target.value)}>
              <option value="GCASH">GCash</option>
              <option value="CARD">Card</option>
              <option value="CASH">Cash</option>
            </select>
            <button onClick={() => run(() => api(`/sessions/${session.id}/payments`, { method: "POST", body: { method } }))}>Pay</button>
          </div>
          <p className="note">Demo payment: no real money moves.</p>
        </div>
      ))}
      {session.status === "COMPLETED" && !me.attended && <p className="note">You were marked absent, so you can't review this session.</p>}
      {session.status === "COMPLETED" && me.attended && (me.reviewed ? <p className="note">You reviewed this session.</p> : (
        <div className="stack">
          <div className="row">
            <select aria-label="Rating" value={rating} onChange={(e) => setRating(e.target.value)}>
              {[5, 4, 3, 2, 1].map((n) => <option key={n} value={n}>{n}/5</option>)}
            </select>
            <input aria-label="Comment" placeholder="How was the session?" maxLength={1000} value={comment} onChange={(e) => setComment(e.target.value)} />
            <button onClick={() => run(() => api(`/sessions/${session.id}/reviews`, { method: "POST", body: { rating: Number(rating), comment } }))}>Submit review</button>
          </div>
        </div>
      ))}
    </>
  );
}
