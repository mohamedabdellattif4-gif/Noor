#!/usr/bin/env python3
"""Install the newest generated Baseline/Startup Profile into the app source set."""
from __future__ import annotations

import argparse
import hashlib
import shutil
import sys
from pathlib import Path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--search-root",
        type=Path,
        default=Path("baseline-profile/build/outputs"),
        help="Directory containing Macrobenchmark additional output.",
    )
    parser.add_argument(
        "--app-source",
        type=Path,
        default=Path("app/src/main"),
        help="App source directory that receives profile text files.",
    )
    return parser.parse_args()


def newest(paths: list[Path]) -> Path | None:
    existing = [path for path in paths if path.is_file() and path.stat().st_size > 0]
    return max(existing, key=lambda path: path.stat().st_mtime_ns) if existing else None


def digest(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def install(source: Path, destination: Path) -> None:
    payload = source.read_text(encoding="utf-8").strip()
    if not payload:
        raise ValueError(f"Generated profile is empty: {source}")
    if "Lcom/noor/" not in payload:
        raise ValueError(f"Generated profile does not contain Noor classes: {source}")
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, destination)
    print(f"Installed {source} -> {destination} ({destination.stat().st_size} bytes, {digest(destination)})")


def main() -> int:
    args = parse_args()
    if not args.search_root.exists():
        print(f"Profile output directory does not exist: {args.search_root}", file=sys.stderr)
        return 2

    baseline = newest(list(args.search_root.rglob("*-baseline-prof.txt")))
    if baseline is None:
        print("No generated *-baseline-prof.txt file was found", file=sys.stderr)
        return 2
    install(baseline, args.app_source / "baseline-prof.txt")

    startup = newest(list(args.search_root.rglob("*-startup-prof.txt")))
    startup_destination = args.app_source / "startup-prof.txt"
    if startup is not None:
        install(startup, startup_destination)
    elif startup_destination.exists():
        startup_destination.unlink()
        print(f"Removed stale {startup_destination}; generator produced no startup profile")
    else:
        print("Generator produced no separate startup profile; baseline profile remains valid")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
