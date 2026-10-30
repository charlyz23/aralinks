const BASE_URL = import.meta.env.VITE_API_URL ?? "";
const TOKEN_KEY = "aralinks.token";

export type Role = "LEARNER" | "TUTOR" | "ADMIN";

export interface User {
  id: string;
  name: string;
  email: string;
  srCode: string;
  university: string;
  roles: Role[];
  active: boolean;
  yearLevel: number | null;
  program: string | null;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface Course {
  id: string;
  code: string;
  name: string;
  category: string;
  type: string;
}

export interface Availability {
  id: string;
  day: string;
  startTime: string;
  endTime: string;
}

export interface Review {
  id: string;
  learnerName: string;
  rating: number;
  comment: string | null;
  dateSubmitted: string;
}

export interface Tutor {
  id: string;
  name: string;
  university: string;
  hourlyRate: number;
  bio: string | null;
  averageRating: number;
  reviewCount: number;
  courses: Course[];
  availability: Availability[];
  reviews: Review[];
}

export interface BookingRequest {
  id: string;
  learnerId: string;
  learnerName: string;
  tutorId: string;
  tutorName: string;
  course: Course;
  date: string;
  startTime: string;
  endTime: string;
  message: string | null;
  status: string;
}

export interface Attendee {
  learnerId: string;
  name: string;
  attended: boolean;
  paid: boolean;
  reviewed: boolean;
}

export interface Session {
  id: string;
  tutorId: string;
  tutorName: string;
  course: Course;
  date: string;
  startTime: string;
  endTime: string;
  costPerLearner: number;
  totalCost: number;
  status: string;
  meetingLink: string | null;
  attendees: Attendee[];
}

export interface TutoringClass {
  id: string;
  tutorId: string;
  tutorName: string;
  course: Course;
  maxCapacity: number;
  availableSlots: number;
  rate: number;
  platform: string;
  status: string;
}

export function formatDay(day: string): string {
  return day.charAt(0) + day.slice(1).toLowerCase();
}

export function formatSlot(date: string, start: string, end: string): string {
  return `${date}, ${start.slice(0, 5)}–${end.slice(0, 5)}`;
}

export function errorMessage(e: unknown): string {
  return e instanceof Error ? e.message : "Something went wrong. Please try again.";
}

export class ApiError extends Error {
  constructor(readonly status: number, message: string) {
    super(message);
  }
}

export function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setToken(token: string | null): void {
  try {
    if (token) localStorage.setItem(TOKEN_KEY, token);
    else localStorage.removeItem(TOKEN_KEY);
  } catch {
    // Storage can be blocked; the session then simply lasts until refresh.
  }
}

export async function api<T>(path: string, options: { method?: string; body?: unknown } = {}): Promise<T> {
  const token = getToken();
  let response: Response;
  try {
    response = await fetch(`${BASE_URL}/api${path}`, {
      method: options.method ?? "GET",
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    });
  } catch {
    throw new ApiError(0, "Can't reach the server. It may be waking up, so try again in a minute.");
  }

  const text = await response.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    // A host error page while the API wakes up isn't JSON; fall through to the generic message.
  }
  if (!response.ok) {
    if (response.status === 401 && path !== "/auth/login") setToken(null);
    throw new ApiError(response.status, data?.message ?? "Something went wrong. Please try again.");
  }
  return data as T;
}
