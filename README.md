# cloud-itonami-isic-0141

Open Occupation Blueprint for **ISIC Rev. 4 0141**: Raising of cattle and buffaloes.

This repository implements a forkable OSS **cattle-raising operations
coordinator**: a facility-management and record-keeping robot manages herd logging,
veterinary appointment scheduling, and supply procurement under a governor-gated actor,
so a cattle operation keeps its own operational records and maintains full transparency
over decisions.

**Maturity: `:implemented`.** `src/cattleops/` implements the
`CattleOpsAdvisor` (`cattleops.advisor`) and the independent
`RanchingOperationsGovernor` (`cattleops.governor`), composed by
`cattleops.operation` following the itonami actor pattern (ADR-2607011000):
`advise -> govern -> phase-gate -> commit | escalate | hold`. 30 tests /
90 assertions green (`clojure -M:test`).

`cattleops.operation` is a synchronous stub of this flow (see its
docstring) — production wiring into a `langgraph-clj` StateGraph with
`interrupt-before`/checkpoint-based human-in-the-loop resume for escalated
operations is deferred, mirroring `cloud-itonami-isic-1010`'s own
`meatprocessing.operation`.

## What this does NOT do

This actor coordinates **back-office logistics only**. It explicitly does **NOT**:

- **Direct animal handling** — remains the rancher's exclusive authority
- **Veterinary treatment decisions** — remains the veterinarian/rancher authority
- **Slaughter or culling decisions** — economic and ethical authority remains human
- **Direct treatment administration** — any proposal for direct treatment is a hard block

## HARD invariants (always hold, never overridable)

1. **facility-not-registered** — the request's `facility-id` must resolve to a
   registered facility in the Store before any proposal can proceed
2. **no-execution** — every proposal's `:effect` must be `:propose` (the governor
   never directly handles animals, never administers treatment, never orders
   slaughter)
3. **treatment-or-slaughter-blocked** — `:administer-treatment` and
   `:order-slaughter` proposals are unconditionally, permanently blocked
4. **op-not-allowed** — any op outside the closed allowlist below is rejected
5. **herd-count-invalid** — `:log-herd-record` with a non-positive count is rejected

## Always-escalate operations (human sign-off, regardless of confidence)

- `:flag-animal-health-concern` — any welfare concern → automatic escalation
- `:order-supplies` over its category cost threshold (default 500 currency
  units; see `cattleops.facts/supply-categories`)
- Any proposal with confidence below the Governor's floor (0.7)

## Operational requests (closed allowlist, all `:effect :propose`)

```text
:log-herd-record
  — record herd count, weight, health status
  — requires a registered facility; non-positive counts are rejected

:schedule-veterinary-visit
  — propose a veterinary appointment
  — does NOT make treatment decisions

:flag-animal-health-concern
  — surface a disease, injury, or welfare concern
  — ALWAYS escalates for human review

:order-supplies
  — procurement for feed, veterinary supplies, equipment
  — escalates if cost exceeds its category threshold
```

## Robotics premise

All cloud-itonami verticals are designed on the premise that a **robot performs the
physical domain work**. Here a facility-management robot handles:

- Herd record logging and entry
- Appointment scheduling and reminders
- Supply inventory and ordering
- Audit ledger maintenance

The **RanchingOperationsGovernor** is the independent safety layer that gates all proposals
before a robot action is executed. The governor never dispatches hardware directly;
`:high`/`:safety-critical` actions (such as escalated health concerns or high-cost
supply orders) require human sign-off.

## Core Contract

```text
operational request (log, schedule, concern, order)
        |
        v
CattleOpsAdvisor -> RanchingOperationsGovernor -> phase gate -> commit, or escalate for human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated operation can dispatch a robot action the governor refuses, suppress an
operating record, or hide a health concern without governor approval and audit evidence.

## Module structure

Mirrors `cloud-itonami-isic-1010` (`meatprocessing.*`) module-for-module:

- `cattleops.facts` — reference data: supply-category cost thresholds, species
- `cattleops.registry` — pure independent verification functions (cost/count/confidence)
- `cattleops.store` — `Store` protocol + in-memory `MemStore` (facility registration lookup)
- `cattleops.advisor` — `Advisor` protocol + `MockAdvisor` (the sealed LLM/decision node)
- `cattleops.governor` — `RanchingOperationsGovernor`: hard invariants + escalation gates
- `cattleops.phase` — 0→3 rollout phase gate
- `cattleops.operation` — composes advisor → governor → phase into one operation run
- `cattleops.sim` — demo runner (`clojure -M:run`)

## Capability layer

Resolves via [`kotoba-lang/occupation`](https://github.com/kotoba-lang/occupation)
(ISIC Rev. 4 `0141`). Required capabilities:

- :robotics
- :identity
- :forms
- :audit-ledger

See [`docs/business-model.md`](docs/business-model.md) and
[`docs/operator-guide.md`](docs/operator-guide.md).

## Testing

```bash
clojure -M:test   # 30 tests / 90 assertions
clojure -M:lint   # clj-kondo, 0 errors / 0 warnings
clojure -M:run    # demo runner
```

## License

AGPL-3.0-or-later.
