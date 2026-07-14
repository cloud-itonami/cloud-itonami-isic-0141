# Business Model: Cattle-Raising Operations Coordinator

## Classification

- Repository: `cloud-itonami-isic-0141`
- ISIC Rev. 4: `0141`
- Industry: Raising of cattle and buffaloes
- Social impact: animal-welfare, food-security, rural-employment

## Customer

- Small-to-medium cattle farms and ranches
- Pastoral dairy operations
- Beef cattle operations
- Buffalo raising (South Asia, Sub-Saharan Africa)

## Offer

- Herd management and record-keeping
- Veterinary appointment coordination
- Health and welfare tracking
- Supply procurement coordination
- Audit trail and transparency

## Revenue

- SaaS subscription (per-head-per-month pricing)
- Supply chain integration fees
- API access for veterinary partners
- Data analytics and reporting add-ons

## Trust Controls

- No slaughter or culling decisions without human sign-off
- No direct treatment administration
- All veterinary recommendations are proposals, not commands
- Facility registration is required before any operation
- All animal health concerns are automatically escalated
- High-cost supply orders require approval
- Audit ledger is append-only and never editable

## What we NOT do

- **Veterinary treatment decisions** — the veterinarian decides treatment
- **Animal welfare decisions** — the rancher decides welfare actions
- **Economic decisions** (slaughter, culling, breeding) — remain human authority
- **Direct animal handling** — the robot manages records and logistics only

## Supported Operations

### Herd Record Logging
- Daily herd counts
- Weight tracking
- Health status notes
- Birth/death records (logging only, not decision-making)

### Veterinary Coordination
- Schedule vet visits
- Track vet exam results
- Propose follow-up care (not order it directly)

### Health Concern Escalation
- Flag suspected disease
- Report injuries or welfare concerns
- Automatic escalation to rancher/vet

### Supply Procurement
- Feed orders
- Veterinary supply orders
- Equipment procurement
- Cost threshold escalation for large orders
