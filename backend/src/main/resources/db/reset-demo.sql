-- Wipes all app data. Run this, then restart the API with SEED_DEMO_DATA=true.
TRUNCATE users, courses, tutoring_classes, tutoring_sessions, requests, payments RESTART IDENTITY CASCADE;
