# Historical phase verifiers

These scripts preserve the original phase-isolation checks. They intentionally reject dependencies
and modules introduced by later phases and therefore are archival only after project completion.
The compatibility scripts in `tools/` run `verify_final.py`, which is the authoritative completed-
project gate.
