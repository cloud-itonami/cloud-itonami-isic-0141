# cloud-itonami-isic-0141

Open Occupation Blueprint for **ISIC Rev. 4 0141**: Raising of cattle and buffaloes.

This repository designs a forkable OSS blueprint for a **cattle-raising operations
coordinator**: a facility-management and record-keeping robot manages herd logging,
veterinary appointment scheduling, and supply procurement under a governor-gated actor,
so a cattle operation keeps its own operational records and maintains full transparency
over decisions.

**Maturity: `:implemented`.** `src/cattleops/` implements the
`CattleOpsActor` as a `langgraph.graph/state-graph` (`cattleops.actor`) wired to a
`CattleOpsAdvisor` (`cattleops.advisor`) and an independent
`RanchingOperationsGovernor` (`cattleops.governor`), following the itonami actor
pattern (ADR-2607011000): `:intake -> :advise -> :govern -> :decide -+-> :commit
(:ok?) +-> :request-approval (:escalate?, human-in-the-loop interrupt) +-> :hold
(:hard?)`. 11 tests / 20 assertions green (`clojure -M:test`).

## What this does NOT do

This actor coordinates **back-office logistics only**. It explicitly does **NOT**:

- **Direct animal handling** — remains the rancher's exclusive authority
- **Veterinary treatment decisions** — remains the veterinarian/rancher authority
- **Slaughter or culling decisions** — economic and ethical authority remains human
- **Direct treatment administration** — any proposal for direct treatment is a hard block

## HARD invariants (always hold, never overridable)

1. **facility-registered** — the herd must belong to a registered facility
2. **no-direct-execution** — proposal `:effect` must be `:propose` (governor never
   directly handles animals, never administers treatment, never orders slaughter)
3. **facility-exists** — the target facility must be verified and in the store
4. **no-treatment-order** — proposals of type `:administer-treatment` or `:order-slaughter`
   are unconditionally blocked

## Always-escalate operations (human sign-off, regardless of confidence)

- `:flag-animal-health-concern` — any welfare concern → automatic escalation
- `:order-supplies` over cost threshold (default 500 currency units)

## Operational requests

```text
:log-herd-record
  — record herd count, weight, health status
  — requires registered facility

:schedule-veterinary-visit
  — propose a veterinary appointment
  — does NOT make treatment decisions

:flag-animal-health-concern
  — surface a disease, injury, or welfare concern
  — ALWAYS escalates for human review

:order-supplies
  — procurement for feed, veterinary supplies, equipment
  — escalates if cost exceeds threshold
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
CattleOpsAdvisor -> RanchingOperationsGovernor -> approve operation, or escalate for human sign-off
        |
        v
robot actions (gated) + operating records + audit ledger
```

No automated operation can dispatch a robot action the governor refuses, suppress an
operating record, or hide a health concern without governor approval and audit evidence.

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
clojure -M:test
```

## License

AGPL-3.0-or-later.
