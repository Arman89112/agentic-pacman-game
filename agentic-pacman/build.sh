#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

FX=/usr/lib/jvm/java-27-openjfx/lib
OUT=out

rm -rf "$OUT"
mkdir -p "$OUT"

javac --module-path "$FX" --add-modules javafx.controls,javafx.fxml \
      -d "$OUT" $(find src -name '*.java')

# Copy FXML (and any other) resources so they are found on the classpath.
find src -name '*.fxml' -print0 | while IFS= read -r -d '' f; do
    rel="${f#src/}"
    mkdir -p "$OUT/$(dirname "$rel")"
    cp "$f" "$OUT/$rel"
done

echo "Build OK -> $OUT"
