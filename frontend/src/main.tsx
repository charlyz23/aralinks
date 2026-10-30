import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Link, Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { AuthProvider, RequireAuth, RequireRole, useAuth } from "./auth";
import { Admin } from "./pages/Admin";
import { Classes } from "./pages/Classes";
import { Dashboard } from "./pages/Dashboard";
import { Login } from "./pages/Login";
import { Register } from "./pages/Register";
import { TutorProfile } from "./pages/TutorProfile";
import { Tutors } from "./pages/Tutors";
import { TutorSettings } from "./pages/TutorSettings";
import "./styles.css";

function Header() {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();
  return (
    <header className="site-header">
      <Link to="/" className="brand">Aralinks</Link>
      <nav>
        <Link to="/tutors">Find tutors</Link>
        <Link to="/classes">Classes</Link>
        {user ? (
          <>
            {user.roles.includes("ADMIN")
              ? <Link to="/admin">Admin</Link>
              : <Link to="/dashboard">Dashboard</Link>}
            <button className="link" onClick={() => { signOut(); navigate("/login"); }}>Log out</button>
          </>
        ) : (
          <Link to="/login">Log in</Link>
        )}
      </nav>
    </header>
  );
}

function App() {
  return (
    <>
      <Header />
      <main>
        <Routes>
          <Route path="/" element={<Navigate to="/tutors" replace />} />
          <Route path="/tutors" element={<Tutors />} />
          <Route path="/tutors/:id" element={<TutorProfile />} />
          <Route path="/classes" element={<Classes />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/dashboard" element={<RequireAuth><Dashboard /></RequireAuth>} />
          <Route path="/tutor-settings" element={<RequireAuth><RequireRole role="TUTOR"><TutorSettings /></RequireRole></RequireAuth>} />
          <Route path="/admin" element={<RequireAuth><RequireRole role="ADMIN"><Admin /></RequireRole></RequireAuth>} />
          <Route path="*" element={<p className="muted">Page not found.</p>} />
        </Routes>
      </main>
    </>
  );
}

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
);
