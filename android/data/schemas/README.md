# Room schema history

The Room Gradle plugin is configured to export compiler-generated JSON schemas into this directory.
`NoorDatabase` is currently at version 4 and registers explicit migrations 1→2, 2→3, and 3→4.

No generated schema JSON is committed in this source package yet. A clean build of the current source
can generate the current `4.json`; it cannot reconstruct authoritative historical `1.json`, `2.json`,
or `3.json`. Recover those files only from the corresponding tagged source/build artifacts or another
trusted historical checkout. Do not hand-author or infer them from the bundled SQLite database.

`NoorDatabaseMigrationTest` independently creates the version-1 source schema represented by the
1→2 migration contract, inserts real data, runs the complete 1→4 chain, and lets Room validate the
resulting version-4 schema. Before production release, also commit the compiler-generated `4.json`,
recover any historical schemas required by the supported upgrade policy, and add Room
`MigrationTestHelper` coverage once those authoritative files are available. Every future schema
change must commit its generated JSON and a non-destructive migration test in the same change.
