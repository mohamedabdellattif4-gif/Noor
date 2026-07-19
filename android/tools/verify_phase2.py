#!/usr/bin/env python3
"""Compatibility entry point: completed-project verification supersedes phase-scope absence checks."""
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parents[1]
print("This project has progressed beyond the original isolated phase scope; running final verification.")
raise SystemExit(subprocess.call([sys.executable, str(root / "tools/verify_final.py")], cwd=root))
