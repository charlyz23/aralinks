import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, type AuthResponse, type Role } from "../api";
import { useAuth } from "../auth";

export function Register() {
  const { signIn } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "", srCode: "", university: "BatStateU" });
  const [roles, setRoles] = useState<Role[]>(["LEARNER"]);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const field = (key: keyof typeof form) => ({
    value: form[key],
    onChange: (e: { target: { value: string } }) => setForm({ ...form, [key]: e.target.value }),
  });

  function toggleRole(role: Role) {
    setRoles(roles.includes(role) ? roles.filter((r) => r !== role) : [...roles, role]);
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (roles.length === 0) {
      setError("Choose at least one role.");
      return;
    }
    setError("");
    setBusy(true);
    try {
      signIn(await api<AuthResponse>("/auth/register", { method: "POST", body: { ...form, roles } }));
      navigate(roles.includes("TUTOR") ? "/tutor-settings" : "/dashboard", { replace: true });
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <form className="card narrow" onSubmit={submit}>
      <h1>Create your account</h1>
      <label>Full name<input required autoComplete="name" {...field("name")} /></label>
      <label>Email<input type="email" required autoComplete="email" {...field("email")} /></label>
      <label>Password<input type="password" required minLength={8} autoComplete="new-password" {...field("password")} /></label>
      <label>SR code<input required {...field("srCode")} /></label>
      <label>University<input required {...field("university")} /></label>
      <fieldset>
        <legend>I want to</legend>
        <label className="check"><input type="checkbox" checked={roles.includes("LEARNER")} onChange={() => toggleRole("LEARNER")} /> Learn</label>
        <label className="check"><input type="checkbox" checked={roles.includes("TUTOR")} onChange={() => toggleRole("TUTOR")} /> Tutor</label>
      </fieldset>
      {error && <p className="error" role="alert">{error}</p>}
      <button type="submit" disabled={busy}>{busy ? "Creating…" : "Create account"}</button>
      <p className="muted">Already registered? <Link to="/login">Log in</Link></p>
    </form>
  );
}
