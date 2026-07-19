# Data module

The data module owns Room persistence and implements contracts declared by `:domain`.

Phase 3 contents:

- `NoorDatabase` version 1
- `surahs` and `ayahs` entities
- Reactive Room DAOs
- Transactional `QuranLocalDataSource`
- `DefaultQuranRepository`
- Domain/entity mappers
- Hilt providers and bindings
- Room schema export and migration registry

See `docs/DATABASE.md` for the schema and migration policy.
