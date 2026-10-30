import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { api, errorMessage, formatDay, type Availability, type Tutor } from "../api";
import { useAuth } from "../auth";

const DAYS = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];

function toInputDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${date.getFullYear()}-${month}-${day}`;
}

/** The first date from tomorrow on that the tutor has hours. */
function nextOpenDate(availability: Availability[]): string {
  const date = new Date();
  for (let i = 1; i <= 7; i++) {
    date.setDate(date.getDate() + 1);
    if (availability.some((a) => a.day === DAYS[date.getDay()])) break;
  }
  return toInputDate(date);
}

export function TutorProfile() {
  const { id } = useParams();
  const [tutor, setTutor] = useState<Tutor | null>(null);
  const [error, setError] = useState("");

  const load = useCallback(() => {
    api<Tutor>(`/tutors/${id}`).then(setTutor).catch((e) => setError(errorMessage(e)));
  }, [id]);

  useEffect(load, [load]);

  if (error) return <p className="error" role="alert">{error}</p>;
  if (!tutor) return <p className="muted">Loading…</p>;

  return (
    <>
      <h1>{tutor.name}</h1>
      <p className="muted">
        {tutor.university} · ₱{tutor.hourlyRate}/hr ·{" "}
        {tutor.reviewCount > 0 ? `${tutor.averageRating.toFixed(1)}/5 from ${tutor.reviewCount} review${tutor.reviewCount === 1 ? "" : "s"}` : "No reviews yet"}
      </p>
      <p>{tutor.bio}</p>

      <div className="split">
        <div className="stack">
          <BookingForm tutor={tutor} />
          <section>
            <h2>Reviews</h2>
            {tutor.reviews.length === 0 && <p className="muted">No reviews yet.</p>}
            <ul className="list">
              {tutor.reviews.map((r) => (
                <li key={r.id}>
                  <span>{r.comment || "No comment"}<br /><span className="note">{r.learnerName} · {r.dateSubmitted}</span></span>
                  <span className="badge">{r.rating}/5</span>
                </li>
              ))}
            </ul>
          </section>
        </div>
        <aside className="card">
          <h2>Teaches</h2>
          {tutor.courses.map((c) => <p key={c.id}>{c.name}</p>)}
          <h2>Available</h2>
          {tutor.availability.length === 0 && <p className="muted">No hours listed yet.</p>}
          {tutor.availability.map((a) => (
            <p key={a.id}>{formatDay(a.day)}, {a.startTime.slice(0, 5)}–{a.endTime.slice(0, 5)}</p>
          ))}
        </aside>
      </div>
    </>
  );
}

function BookingForm({ tutor }: { tutor: Tutor }) {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [courseId, setCourseId] = useState(tutor.courses[0]?.id ?? "");
  const [date, setDate] = useState(() => nextOpenDate(tutor.availability));
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("10:00");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  if (!user) {
    return <p className="card"><Link to="/login" state={{ from: `/tutors/${tutor.id}` }}>Log in</Link> to book {tutor.name}.</p>;
  }
  if (user.id === tutor.id || !user.roles.includes("LEARNER")) return null;
  if (tutor.courses.length === 0) return <p className="card muted">This tutor hasn't listed any courses yet.</p>;

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setBusy(true);
    try {
      await api("/requests", {
        method: "POST",
        body: { tutorId: tutor.id, courseId, date, startTime, endTime, message },
      });
      navigate("/dashboard");
    } catch (e) {
      setError(errorMessage(e));
      setBusy(false);
    }
  }

  return (
    <form className="card stack" onSubmit={submit}>
      <h2>Request a session</h2>
      <label>Course
        <select value={courseId} onChange={(e) => setCourseId(e.target.value)}>
          {tutor.courses.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
      </label>
      <div className="form-grid">
        <label>Date<input type="date" required value={date} onChange={(e) => setDate(e.target.value)} /></label>
        <label>Start<input type="time" required value={startTime} onChange={(e) => setStartTime(e.target.value)} /></label>
        <label>End<input type="time" required value={endTime} onChange={(e) => setEndTime(e.target.value)} /></label>
      </div>
      <label>Message
        <textarea rows={3} maxLength={1000} value={message} placeholder="What do you need help with?" onChange={(e) => setMessage(e.target.value)} />
      </label>
      {error && <p className="error" role="alert">{error}</p>}
      <button type="submit" disabled={busy}>{busy ? "Sending…" : "Send request"}</button>
    </form>
  );
}
