import { useCallback, useEffect, useState, type FormEvent } from "react";
import { api, errorMessage, formatDay, type Course, type Tutor } from "../api";
import { useAuth } from "../auth";

const DAYS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

export function TutorSettings() {
  const { user } = useAuth();
  const [tutor, setTutor] = useState<Tutor | null>(null);
  const [courses, setCourses] = useState<Course[]>([]);
  const [rate, setRate] = useState("");
  const [bio, setBio] = useState("");
  const [day, setDay] = useState("MONDAY");
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("17:00");
  const [error, setError] = useState("");
  const [saved, setSaved] = useState("");

  const load = useCallback(() => {
    Promise.all([api<Tutor>(`/tutors/${user!.id}`), api<Course[]>("/courses")])
      .then(([foundTutor, foundCourses]) => {
        setTutor(foundTutor);
        setCourses(foundCourses);
        setRate(String(foundTutor.hourlyRate));
        setBio(foundTutor.bio ?? "");
      })
      .catch((e) => setError(errorMessage(e)));
  }, [user]);

  useEffect(load, [load]);

  async function update(path: string, method: string, body?: unknown) {
    setError("");
    setSaved("");
    try {
      setTutor(await api<Tutor>(path, { method, body }));
      setSaved("Saved.");
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  function saveProfile(event: FormEvent) {
    event.preventDefault();
    update("/me/tutor", "PUT", { hourlyRate: rate, bio });
  }

  function addHours(event: FormEvent) {
    event.preventDefault();
    update("/me/tutor/availability", "POST", { day, startTime, endTime });
  }

  if (!tutor) return error ? <p className="error" role="alert">{error}</p> : <p className="muted">Loading…</p>;

  return (
    <div className="stack">
      <h1>Tutor profile</h1>
      <p className="muted">Learners can book you once you've set a rate, chosen a course and added your hours.</p>
      {error && <p className="error" role="alert">{error}</p>}
      {saved && <p className="success" role="status">{saved}</p>}

      <form className="card stack" onSubmit={saveProfile}>
        <h2>Rate and bio</h2>
        <label>Hourly rate (₱)<input type="number" min="0" step="1" required value={rate} onChange={(e) => setRate(e.target.value)} /></label>
        <label>Bio<textarea rows={3} maxLength={1000} value={bio} onChange={(e) => setBio(e.target.value)} /></label>
        <button type="submit">Save</button>
      </form>

      <section className="card stack">
        <h2>Courses</h2>
        {courses.map((course) => {
          const teaches = tutor.courses.some((c) => c.id === course.id);
          return (
            <label className="check" key={course.id}>
              <input
                type="checkbox"
                checked={teaches}
                onChange={() => update(`/me/tutor/courses/${course.id}`, teaches ? "DELETE" : "POST")}
              />
              {course.code} · {course.name}
            </label>
          );
        })}
      </section>

      <section className="card stack">
        <h2>Available hours</h2>
        {tutor.availability.length === 0 && <p className="muted">No hours added yet.</p>}
        <ul className="list">
          {tutor.availability.map((a) => (
            <li key={a.id}>
              <span>{formatDay(a.day)}, {a.startTime.slice(0, 5)}–{a.endTime.slice(0, 5)}</span>
              <button className="danger" type="button" onClick={() => update(`/me/tutor/availability/${a.id}`, "DELETE")}>Remove</button>
            </li>
          ))}
        </ul>
        <form className="form-grid" onSubmit={addHours}>
          <label>Day
            <select value={day} onChange={(e) => setDay(e.target.value)}>
              {DAYS.map((d) => <option key={d} value={d}>{formatDay(d)}</option>)}
            </select>
          </label>
          <label>Start<input type="time" required value={startTime} onChange={(e) => setStartTime(e.target.value)} /></label>
          <label>End<input type="time" required value={endTime} onChange={(e) => setEndTime(e.target.value)} /></label>
          <button type="submit" className="secondary">Add hours</button>
        </form>
      </section>
    </div>
  );
}
