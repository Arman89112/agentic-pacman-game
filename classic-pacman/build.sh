#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

FX=/usr/lib/jvm/java-27-openjfx/lib
OUT=out

rm -rf "$OUT"
mkdir -p "$OUT"

javac --module-path "$FX" --add-modules javafx.controls \
      -d "$OUT" $(find src -name '*.java')

echo "Build OK -> $OUT"
