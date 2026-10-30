import { useCallback, useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { api, errorMessage, type Course, type Tutor, type TutoringClass } from "../api";
import { useAuth } from "../auth";

export function Classes() {
  const { user } = useAuth();
  const [classes, setClasses] = useState<TutoringClass[] | null>(null);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const load = useCallback(() => api<TutoringClass[]>("/classes").then(setClasses), []);

  useEffect(() => {
    load().catch((e) => setError(errorMessage(e)));
  }, [load]);

  async function run(action: () => Promise<unknown>, done: string) {
    setError("");
    setNotice("");
    try {
      await action();
      await load();
      setNotice(done);
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  const isTutor = user?.roles.includes("TUTOR");
  const isLearner = user?.roles.includes("LEARNER");

  return (
    <div className="stack">
      <div>
        <h1>Group classes</h1>
        <p className="muted">Learn alongside other students at the tutor's hourly rate per learner.</p>
      </div>
      {error && <p className="error" role="alert">{error}</p>}
      {notice && <p className="success" role="status">{notice}</p>}
      {isTutor && user && <NewClassForm tutorId={user.id} run={run} />}
      {classes === null && !error && <p className="muted">Loading…</p>}
      {classes?.length === 0 && <p className="muted">No open classes right now.</p>}
      <div className="grid">
        {classes?.map((c) => (
          <article className="card stack" key={c.id}>
            <div>
              <h2>{c.course.name}</h2>
              <p className="muted">{c.tutorName} · {c.platform}</p>
              <p>₱{c.rate}/hr · {c.availableSlots} of {c.maxCapacity} slots left</p>
            </div>
            {c.tutorId === user?.id ? <OwnClassActions tutoringClass={c} run={run} /> : isLearner ? (
              <button
                disabled={c.availableSlots === 0}
                onClick={() => run(() => api(`/classes/${c.id}/enroll`, { method: "POST" }), `You're enrolled in ${c.course.name}.`)}
              >
                {c.availableSlots === 0 ? "Full" : "Enroll"}
              </button>
            ) : !user && <p className="note"><Link to="/login">Log in</Link> to enroll.</p>}
          </article>
        ))}
      </div>
    </div>
  );
}

type Run = (action: () => Promise<unknown>, done: string) => Promise<void>;

function NewClassForm({ tutorId, run }: { tutorId: string; run: Run }) {
  const [courses, setCourses] = useState<Course[]>([]);
  const [courseId, setCourseId] = useState("");
  const [maxCapacity, setMaxCapacity] = useState("5");
  const [platform, setPlatform] = useState("Google Meet");
  const [classroomLink, setClassroomLink] = useState("");

  useEffect(() => {
    api<Tutor>(`/tutors/${tutorId}`).then((tutor) => {
      setCourses(tutor.courses);
      setCourseId(tutor.courses[0]?.id ?? "");
    }).catch(() => setCourses([]));
  }, [tutorId]);

  if (courses.length === 0) return null;

  function submit(event: FormEvent) {
    event.preventDefault();
    run(() => api("/classes", {
      method: "POST",
      body: { courseId, maxCapacity: Number(maxCapacity), platform, classroomLink: classroomLink || null },
    }), "Class created. Add a session so learners know when it meets.");
  }

  return (
    <form className="card stack" onSubmit={submit}>
      <h2>Create a class</h2>
      <div className="form-grid">
        <label>Course
          <select value={courseId} onChange={(e) => setCourseId(e.target.value)}>
            {courses.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </label>
        <label>Capacity<input type="number" min="1" required value={maxCapacity} onChange={(e) => setMaxCapacity(e.target.value)} /></label>
        <label>Platform<input required value={platform} onChange={(e) => setPlatform(e.target.value)} /></label>
        <label>Meeting link<input type="url" value={classroomLink} placeholder="https://…" onChange={(e) => setClassroomLink(e.target.value)} /></label>
      </div>
      <button type="submit">Create class</button>
    </form>
  );
}

function OwnClassActions({ tutoringClass, run }: { tutoringClass: TutoringClass; run: Run }) {
  const [date, setDate] = useState("");
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("10:00");

  function addSession(event: FormEvent) {
    event.preventDefault();
    run(() => api(`/classes/${tutoringClass.id}/sessions`, {
      method: "POST",
      body: { date, startTime, endTime },
    }), "Session added. It's on your dashboard.");
  }

  return (
    <>
      <form className="stack" onSubmit={addSession}>
        <div className="form-grid">
          <label>Date<input type="date" required value={date} onChange={(e) => setDate(e.target.value)} /></label>
          <label>Start<input type="time" required value={startTime} onChange={(e) => setStartTime(e.target.value)} /></label>
          <label>End<input type="time" required value={endTime} onChange={(e) => setEndTime(e.target.value)} /></label>
        </div>
        <button type="submit" className="secondary">Add session</button>
      </form>
      <button onClick={() => run(() => api(`/classes/${tutoringClass.id}/start`, { method: "POST" }), "Class started. Enrollment is closed.")}>
        Start class and close enrollment
      </button>
    </>
  );
}
