# ADR 0001: Record architecture decisions

## Status

Accepted.

## Context

The project has grown to seven services and a shared library. Decisions about
data ownership, transport and consistency were made in review and then lost in
chat history. New contributors rediscovered the same trade-offs repeatedly.

## Decision

Every significant architectural choice gets a short record in `docs/adr/`.
A record contains:

1. Status (proposed, accepted, superseded).
2. Context: the forces at play.
3. Decision: what we will do.
4. Consequences: what becomes easier and what becomes harder.

Records are numbered and immutable. A change of direction adds a new record
that supersedes the old one.

## Consequences

- Reviewers can point at a record instead of re-arguing a decision.
- The history of a choice survives staff changes.
- Writing a record takes time; we only write them for decisions that are hard
  to reverse or likely to be questioned.
