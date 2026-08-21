# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

SDKMAN! Candidates Service — a **Play 2.8 / Scala 2.12.13** HTTP API used by the SDKMAN! CLI to list candidates and resolve versions. Backed by MongoDB (legacy) and, on this branch, increasingly by the SDKMAN State API over HTTP.

- **JDK:** 11.0.14-tem (locked in `.sdkmanrc`).
- **Build tool:** sbt 1.4.9 (system-provided; invoke as `sbt`).
- **Formatter:** scalafmt 3.8.3 (config in `.scalafmt.conf`, max column 100, `align.preset = more`).
- **Test framework:** ScalaTest + Cucumber, with WireMock for HTTP stubs.

## Active work — step 2 of the versions Mongo→Postgres migration

This branch (`state_api_integration`) is part of a platform-wide migration of the `versions` collection from MongoDB to PostgreSQL. The PostgreSQL data is owned by a separate service called the **State API** (`https://state.sdkman.io`), and this service is being switched from direct Mongo reads to HTTP calls against the State API.

**The authoritative, imperative spec for the work to be done on this branch lives at [`specs/state-api-default-version.md`](specs/state-api-default-version.md).** Read it first.

A high-level gap analysis lives in the umbrella control-plane repo at `../../docs/specs/02-candidates-state-api-integration.md` — for context only; the local spec is the source of truth.

## Build, test, lint

All commands use the system-provided `sbt`. Run from this submodule's root.

- **Compile:** `sbt compile`
- **Run all tests:** `sbt test` — includes ScalaTest specs and Cucumber features. No live State API required: HTTP calls are stubbed via WireMock (`test/support/StateApiStubs.scala`).
- **Run a single test class:** `sbt "testOnly <FQCN>"` (e.g. `sbt "testOnly controllers.DefaultControllerSpec"`).
- **Run a single Cucumber feature:** point sbt at the feature path or use the `cucumber.options` system property — see `test/RunCukes.scala` for the runner wiring.
- **Check formatting:** `sbt scalafmtCheck Test/scalafmtCheck` — verify scalafmt without modifying.
- **Format:** `sbt scalafmt Test/scalafmt` — fix formatting in place.
- **Clean:** `sbt clean`.

`sbt test` is the canonical "did I break anything" command for the loop. It must pass before any commit.

## Running the app locally

`sbt run` starts the Play app on **port 9000**.

Prerequisites for the dev server (not the test suite):

- **MongoDB** on `localhost:27017`. Quick start: `docker run -d --net=host --name mongo mongo:3.2`.
- **State API** reachable per `conf/application.conf` (`state-api.{protocol,host,port}`). Defaults to `http://localhost:8080`. Override via env vars `STATE_API_PROTOCOL`, `STATE_API_HOST`, `STATE_API_PORT`.

For most loop work the test suite is sufficient — the dev server is rarely needed.

## Layout

- `app/clients/` — HTTP clients. `StateApiImpl.scala` is the State API client; `RequestBuilder.scala` constructs the WS requests. **This is where most step-2 work happens.**
- `app/controllers/` — Play controllers. `DefaultController.scala` is the focus of the default-version migration; `VersionsController`, `VersionsListController`, `JavaListController`, `ValidationController` already call the State API client (with the contract bugs flagged in the spec).
- `app/domain/` — domain types (`Version`, `Platform`).
- `app/repos/` — Mongo repository wrappers (legacy; still used for `Candidate` metadata).
- `conf/routes` — Play routes.
- `conf/application.conf` — config, including the `state-api { … }` block.
- `test/` — ScalaTest specs (`*Spec.scala`) and Cucumber wiring.
- `test/support/StateApiStubs.scala` — WireMock stub builders; **extend this when adding new State API calls**.
- `features/` — Cucumber `.feature` files.

## Important guardrails

- **State API is consumed as-is.** No changes to State API endpoints or schemas are in scope on this branch. If a call doesn't match, change the call — not the API.
- **Public route contract is stable.** Routes in `conf/routes` keep their existing shape (e.g. `vendor` query param stays `vendor`); translate to State API's `distribution` term inside the client.
- **WireMock stubs must mirror the real State API contract** (verified against <https://state.sdkman.io/swagger/documentation.yaml>) — don't invent endpoint shapes in stubs that the real API doesn't expose.
- **Tests gate commits.** `sbt test` must be green before each commit; `sbt scalafmtCheck Test/scalafmtCheck` must also pass.
