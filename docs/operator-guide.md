# Operator Guide: Cattle-Raising Operations Coordinator

## Overview

The Cattle-Raising Operations Coordinator is a facility-management robot that:

1. **Logs operational data** — herd counts, weights, health notes
2. **Schedules coordination** — veterinary appointments, supply orders
3. **Escalates concerns** — any animal health or welfare issue
4. **Maintains transparency** — audit ledger traces all decisions

The robot is **not** the decision-maker. The rancher/veterinarian make all
decisions about animal welfare, treatment, and economic choices. The robot
**proposes** actions and escalates when human input is needed.

## Operating the Actor

### Prerequisites

1. **Facility Registration** — your facility must be registered in the system
   before any operation can proceed
2. **Authorized User** — operator must be authenticated and authorized
3. **Clear Request Type** — specify what you're doing:
   - `:log-herd-record` — record herd data
   - `:schedule-veterinary-visit` — arrange vet appointment
   - `:flag-animal-health-concern` — report a concern
   - `:order-supplies` — procurement request

### Workflow

1. **Submit Request**
   ```clojure
   {:facility-id "farm-001"
    :op :log-herd-record
    :count 50
    :health-status "healthy"
    :confidence 0.95}
   ```

2. **Actor Processes**
   - `:intake` — receives request
   - `:advise` — advisor generates proposal
   - `:govern` — governor checks hard invariants
   - `:decide` — decision: commit / escalate / hold

3. **Outcomes**
   - **:commit** — operation logged, robot proceeds
   - **:request-approval** — operation escalated, awaits human decision
   - **:hold** — operation blocked (hard violation)

### Escalation Scenarios

**Automatic escalation (always human sign-off):**
- `:flag-animal-health-concern` — any welfare issue
- Supply orders over cost threshold (default 500 currency units)
- Low confidence operations (< 0.7)

**Hard blocks (no override):**
- `:administer-treatment` — treatment decisions are veterinary authority
- `:order-slaughter` — economic decisions are human authority
- Missing/unregistered facility — must register first

### Resuming Escalated Operations

After human review/approval:

```clojure
(actor/approve! graph thread-id)
```

The operation proceeds from the `:request-approval` node directly to `:commit`.

## Audit & Transparency

All operations are logged in an append-only ledger. You can:

- Review the ledger at any time
- Verify each decision (approve/escalate/block)
- Trace all animal health concerns
- Audit all supply orders

The ledger is **never edited or deleted** — it's a permanent record of all
operations and decisions.

## Integration

The actor provides a standard protocol (`cattleops.store/Store`) for backend
integration:

- **Facility lookup** — `(store/facility store facility-id)`
- **Record commit** — `(store/commit-record! store record)`
- **Ledger append** — `(store/append-ledger! store entry)`
- **Ledger read** — `(store/ledger store)`

Implementations include in-memory (testing), database, cloud storage, etc.

## Safety Guarantees

- **No unsupervised decisions** — no animal treatment or welfare decision is
  made by the robot
- **No suppressed concerns** — animal health concerns cannot be hidden or delayed
- **No unlogged operations** — every action is recorded in the audit ledger
- **No direct execution** — the governor gates every robot action

The robot is safe because:
1. It never decides — it proposes
2. It always escalates when needed
3. It never hides information
4. Every action is auditable
