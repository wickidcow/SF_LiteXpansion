# Cargo Configurator: component-preserving presentation

Replace deprecated String lore/display-name access and ChatColor messages in the Cargo Configurator with the native Adventure API. Existing lore and template components stay intact instead of round-tripping through legacy strings. Only the generated two-line copy suffix is constructed anew.

The historical size rules remain: clearing preserves same-length customized lore, copying replaces exactly one default-plus-two-line suffix, and other lengths retain the old append behavior. Item ID, recipes, cargo type restrictions, permissions, copied block settings, asynchronous load ordering and event cancellation are unchanged. The LX2 legacy reader, JSON writer, migration fingerprint, PDC key names and Doctor migration functions are retained verbatim. No new storage format or migration is introduced.

Twelve component tests protect absent/empty lore, template isolation, rich hover/style preservation, repeated copy behavior and 500 deterministic input layouts against both previous String-list algorithms. These are presentation/helper tests, not a claim of live cargo-transfer or historical-world coverage. They add test-only JUnit dependencies; no new runtime dependency is shipped.

The normal floor/intermediate/26.3 builds and exact coordinated core/bundle integration must validate this commit before release. No version bump, merge or release is performed by this change.
