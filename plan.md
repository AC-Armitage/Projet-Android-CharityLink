# CharityLink project plan

This plan outlines the steps to go from the current UI prototype to a fully functioning app.

## Phase 0: Project basics

- Confirm product scope and roles (donor, association, admin).
- Finalize navigation map and screens list.
- Decide backend (Firebase recommended for Android-first).

## Phase 1: Data and auth foundation

- Implement auth (email/password) and role assignment.
- Create data models in code (User, Association, Need, Donation, Update, Notification).
- Define backend schema and security rules.
- Build a repository layer with basic CRUD for each entity.

## Phase 2: Core donor experience

- Donor home: list urgent needs, associations, search/filter.
- Association detail: profile + needs list + donate CTA.
- Need detail: status, progress, donate form.
- Donation flow: create donation, update need totals.
- Donation history and impact tracking.

## Phase 3: Core association experience

- Association dashboard: metrics and needs list.
- Create/edit need (money, food, clothes, mixed).
- Manage donations and update need status.
- Post updates with photos.

## Phase 4: Notifications and messaging

- Push notifications for donation updates and status changes.
- In-app notifications list.
- Optional: messaging between donor and association.

## Phase 5: Admin and verification

- Admin verification for associations.
- Moderation tools for reported content.
- Audit and analytics dashboards (optional).

## Phase 6: Polish and release

- QA testing across devices.
- Performance tuning (lists, image loading).
- Crash reporting and analytics setup.
- App store assets and release checklist.

## Suggested order of implementation

1. Auth + role selection + basic profile setup.
2. Firestore collections and security rules.
3. Donor home data integration.
4. Association home data integration.
5. Donation flow and progress updates.
6. Updates and notifications.
7. Admin verification.
8. Final polish and release.

## Acceptance checklist

- Users can sign up, select role, and complete onboarding.
- Donors can browse, filter, and donate to needs.
- Associations can create and manage needs.
- Donations update need progress accurately.
- Notifications and updates are delivered.
- App is stable, fast, and verified for release.
