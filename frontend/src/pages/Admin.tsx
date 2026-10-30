import { useCallback, useEffect, useState, type FormEvent } from "react";
import { api, errorMessage, formatSlot, type Session, type TutoringClass, type User } from "../api";
import { useAuth } from "../auth";

export function Admin() {
  const { user: me } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [classes, setClasses] = useState<TutoringClass[]>([]);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [course, setCourse] = useState({ code: "", name: "", category: "", type: "Lecture" });
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    const [foundUsers, foundClasses, foundSessions] = await Promise.all([
      api<User[]>("/admin/users"),
      api<TutoringClass[]>("/classes"),
      api<Session[]>("/admin/sessions"),
    ]);
    setUsers(foundUsers);
    setClasses(foundClasses);
    setSessions(foundSessions);
  }, []);

  useEffect(() => {
    load().catch((e) => setError(errorMessage(e)));
  }, [load]);

  async function run(action: () => Promise<unknown>) {
    setError("");
    try {
      await action();
      await load();
    } catch (e) {
      setError(errorMessage(e));
    }
  }

  function addCourse(event: FormEvent) {
    event.preventDefault();
    run(async () => {
      await api("/admin/courses", { method: "POST", body: course });
      setCourse({ code: "", name: "", category: "", type: "Lecture" });
    });
  }

  const field = (key: keyof typeof course) => ({
    value: course[key],
    onChange: (e: { target: { value: string } }) => setCourse({ ...course, [key]: e.target.value }),
  });

  return (
    <div className="stack">
      <h1>Admin</h1>
      {error && <p className="error" role="alert">{error}</p>}

      <section>
        <h2>Users</h2>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Name</th><th>Email</th><th>Roles</th><th>Status</th><th></th></tr></thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.name}</td>
                  <td>{u.email}</td>
                  <td>{u.roles.join(", ")}</td>
                  <td>{u.active ? "Active" : "Deactivated"}</td>
                  <td>
                    {u.id !== me?.id && (
                      <button
                        className={u.active ? "danger" : "secondary"}
                        onClick={() => run(() => api(`/admin/users/${u.id}/active`, { method: "PATCH", body: { active: !u.active } }))}
                      >
                        {u.active ? "Deactivate" : "Reactivate"}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section>
        <h2>Session records</h2>
        {sessions.length === 0 && <p className="muted">No sessions yet.</p>}
        <div className="table-wrap">
          <table>
            <thead><tr><th>Course</th><th>When</th><th>Tutor</th><th>Learners</th><th>Total</th><th>Status</th></tr></thead>
            <tbody>
              {sessions.map((s) => (
                <tr key={s.id}>
                  <td>{s.course.name}</td>
                  <td>{formatSlot(s.date, s.startTime, s.endTime)}</td>
                  <td>{s.tutorName}</td>
                  <td>{s.attendees.map((a) => a.name).join(", ")}</td>
                  <td>₱{s.totalCost}</td>
                  <td>{s.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section>
        <h2>Open class listings</h2>
        {classes.length === 0 && <p className="muted">No open classes.</p>}
        <ul className="list">
          {classes.map((c) => (
            <li key={c.id}>
              <span>{c.course.name} · {c.tutorName}</span>
              <button className="danger" onClick={() => run(() => api(`/admin/classes/${c.id}`, { method: "DELETE" }))}>Remove</button>
            </li>
          ))}
        </ul>
      </section>

      <form className="card stack" onSubmit={addCourse}>
        <h2>Add a course</h2>
        <div className="form-grid">
          <label>Code<input required {...field("code")} /></label>
          <label>Name<input required {...field("name")} /></label>
          <label>Category<input required {...field("category")} /></label>
          <label>Type<input required {...field("type")} /></label>
        </div>
        <button type="submit">Add course</button>
      </form>
    </div>
  );
}
