import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, type Tutor } from "../api";

export function Tutors() {
  const [keyword, setKeyword] = useState("");
  const [tutors, setTutors] = useState<Tutor[] | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let current = true;
    const timer = setTimeout(() => {
      api<Tutor[]>(`/tutors?q=${encodeURIComponent(keyword)}`)
        .then((found) => { if (current) { setTutors(found); setError(""); } })
        .catch((e: Error) => { if (current) setError(e.message); });
    }, 250);
    return () => { current = false; clearTimeout(timer); };
  }, [keyword]);

  return (
    <>
      <h1>Find a tutor</h1>
      <p className="muted">Book one-on-one help from fellow students.</p>
      <input
        className="search"
        type="search"
        aria-label="Search tutors"
        placeholder="Search by name, course or subject"
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
      />
      {error && <p className="error" role="alert">{error}</p>}
      {tutors === null && !error && <p className="muted">Loading…</p>}
      {tutors?.length === 0 && <p className="muted">No tutors match that search.</p>}
      <div className="grid">
        {tutors?.map((tutor) => (
          <article className="card" key={tutor.id}>
            <h2><Link to={`/tutors/${tutor.id}`}>{tutor.name}</Link></h2>
            <p className="muted">{tutor.university}</p>
            <p>{tutor.bio}</p>
            <p>{tutor.courses.map((c) => c.name).join(" · ")}</p>
            <p>
              <strong>₱{tutor.hourlyRate}/hr</strong>
              {" · "}
              {tutor.reviewCount > 0 ? `${tutor.averageRating.toFixed(1)}/5 (${tutor.reviewCount})` : "No reviews yet"}
            </p>
          </article>
        ))}
      </div>
    </>
  );
}
