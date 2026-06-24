# Spec: Streaming EMBL Sequence Writer

**Status:** Implemented
**Created:** 2026-06-24

## Background & Problem

`EmblSequenceWriter` required the entire sequence as an in-memory `byte[]`. For large genome assemblies (hundreds of MB to several GB) this is prohibitive. The `SQ` header records per-base counts and must be emitted before the sequence body, which forced two full passes over the array.

`EmblEntryWriter` constructed the writer inline with no override hook, making it impossible for a subclass to substitute a streaming path without duplicating the entire `write()` method.

## Goal

Allow a consumer (gff3tools) to write an arbitrarily large sequence to an EMBL flat file with peak heap in the tens of MB by:

1. A streaming constructor on `EmblSequenceWriter` that accepts precomputed base counts and a `java.io.Reader` instead of a `byte[]`.
2. A protected `writeSequence(Writer)` hook on `EmblEntryWriter` that a subclass can override to supply the streaming path.

The existing `byte[]` path and all current callers are unchanged.

## Streaming API

**Constructors:**

```java
EmblSequenceWriter(Entry entry, long totalBases,
                   Map<Character, Long> baseCounts, Reader reader)
EmblSequenceWriter(Entry entry, long totalBases,
                   Map<Character, Long> baseCounts, Reader reader, long crc)
```

- `totalBases` — sequence length; used for the `SQ` header `BP;` count and the final position counter.
- `baseCounts` — lowercase `a/c/g/t` → count. `other` is derived as `totalBases − (a+c+g+t)`, not by summing non-acgt map entries, so the map may be incomplete.
- `reader` — positioned at the start of the sequence. **Not closed by `write()`**; the caller retains ownership.
- `crc` — optional CRC32 value; emitted as `<crc> CRC32;` in the `SQ` header when non-zero. Omit (4-arg constructor) to suppress it.
- `write()` returns `false` and writes nothing when `totalBases == 0`.

**Hook:**

```java
// EmblEntryWriter
protected void writeSequence(Writer writer) throws IOException
```

Default implementation calls the existing byte-path constructor. Override to inject the streaming path.

## Out of Scope

- `EmblReducedFlatFileWriter` — not modified; holds the full sequence by design.
- `Sequence.java` — unchanged; the streaming path bypasses `getSequenceByte()` entirely.
- gff3tools implementation — separate repo, consumes this API.
- gff3tools CRC32 source — callers are responsible for supplying a precomputed value (e.g. from a single-pass scan in fastareader).

## Design Constraints

- No new dependency on `fastareader`. Interface uses only `java.io.Reader`, `Map<Character, Long>`, and `long`.
- The streaming path produces byte-identical output to the `byte[]` path: same `SQ` header, same 5-space indent, same 10-base groups / 6 blocks per line, same position-counter padding rules for full and partial lines.

## Tests

- `EmblSequenceWriterStreamingTest` — equivalence tests: multi-line (130 bases), partial-line (75 bases), zero-length, and CRC32 field. Both paths run against the same sequence and crc; output asserted identical.
- Existing `SequenceWriterTest`, `EmblEntryWriterTest`, `EmblEntryRoundTripTest` — unmodified, all pass.
