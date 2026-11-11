# Contributing to SawtFlow AI

Thank you for your interest in improving SawtFlow AI. Contributions are welcome when they preserve the project's Arabic-first experience, human approval safeguards, and privacy expectations.

شكرًا لاهتمامك بالمساهمة في SawtFlow AI. نرحب بالمساهمات التي تحافظ على تجربة عربية أولًا، وضوابط الموافقة البشرية، ومتطلبات الخصوصية.

## Before you start | قبل البدء

- Read the project [README](README.md) and inspect the relevant source files.
- Check [TODO.md](TODO.md) before starting a feature.
- For database changes, review [database/schema.sql](database/schema.sql).
- Do not commit API keys, database credentials, customer recordings, personal data, or provider tokens.
- Keep changes focused and explain behavior changes in the pull request.

## Development setup | إعداد بيئة التطوير

```bash
git clone https://github.com/mrm6676/SawtFlow.git
cd SawtFlow
python3 -m http.server 3000 --directory site
```

Open the homepage at <http://localhost:3000/> and the Studio at <http://localhost:3000/studio/>.

The current frontend is a static site. Server-side speech, database, and business-provider integrations must remain behind authenticated server APIs; never connect the browser directly to MySQL or expose private credentials in `site/`.

## What to contribute | مجالات المساهمة

Useful contributions include:

- Arabic dialect and Arabic-English mixed-speech handling.
- Transcript correction and intent-detection improvements.
- RTL layout, accessibility, responsive behavior, and theme improvements.
- Studio edge cases and safe mock fixtures.
- Server API design for order lookup and approved actions.
- MySQL migrations, indexes, data-retention improvements, and auditability.
- Documentation, reproducible tests, and deployment guidance.

## Branches and commits | الفروع والالتزامات

- Create a focused branch from the latest `main` branch.
- Use a concise imperative commit subject, for example:
  - `Add delayed shipment fixture`
  - `Improve Arabic transcript review`
  - `Document approval workflow`
- Keep unrelated formatting or generated files out of a feature change.
- Do not rewrite shared history or force-push without explicit maintainer approval.

## Pull requests | طلبات الدمج

A pull request should include:

1. A short summary of the problem and solution.
2. The affected files or routes.
3. Manual verification steps and expected results.
4. Screenshots or a short recording for visible UI changes, including both light and dark themes when relevant.
5. Database migration notes for schema changes.
6. Security and privacy considerations for audio, transcript, customer, or order data.
7. Any follow-up work that is intentionally out of scope.

Before requesting review, verify that:

```bash
python3 -m http.server 3000 --directory site
```

loads both `/` and `/studio/`, and that `/manus-routes.json` remains valid when routes change.

## Safety requirements | متطلبات الأمان

SawtFlow separates understanding from execution. Contributions must not bypass the approval gate for consequential actions such as cancellation or modification.

- Customer speech may suggest an action; it must not execute one by itself.
- Validate the actor, order state, and allowed fields on the server.
- Use transactions or row locking for business writes where required.
- Use idempotency keys for retriable action requests.
- Write audit events for lookup, approval, rejection, completion, and failure.
- Preserve before/after snapshots where an action changes business state.
- Treat external provider payloads and transcripts as untrusted input.
- Minimize stored personal data and follow defined retention rules.

## UI and language guidelines | إرشادات الواجهة واللغة

- Keep Arabic copy natural, clear, and suitable for business customer service.
- Preserve RTL layout behavior and test mixed Arabic-English text.
- Provide an English equivalent for new user-facing concepts when bilingual UI is expected.
- Preserve keyboard accessibility, readable contrast, visible focus states, and responsive behavior.
- Prefer existing design tokens, icons, animations, and components over one-off replacements.
- Keep animations subtle and avoid motion that blocks task completion.

## Database changes | تغييرات قاعدة البيانات

When changing `database/schema.sql` or adding a migration:

- State the target dialect and MySQL version.
- Use `utf8mb4` for Arabic-compatible text.
- Add indexes and foreign keys deliberately.
- Preserve audit history; do not introduce application paths that update or delete audit events.
- Make destructive changes explicit and provide a migration or rollback plan.
- Do not include production data in fixtures or commits.

## Reporting issues | الإبلاغ عن المشكلات

When reporting a bug, include:

- Browser and operating system.
- Language and theme selected.
- Exact route or Studio step.
- Reproduction steps.
- Expected and actual behavior.
- Console or server errors with secrets and personal data removed.
- A minimal fixture or sample input when it is safe to share.

For security vulnerabilities, do not open a public issue with exploit details. Contact the project maintainers privately through the repository's configured security channel.

## License | الترخيص

By contributing to this repository, you agree that your contributions may be distributed under the [MIT License](LICENSE), unless a different written agreement applies.

بمساهمتك في هذا المستودع، فإنك توافق على توزيع مساهماتك بموجب [ترخيص MIT](LICENSE)، ما لم يوجد اتفاق كتابي مختلف.
