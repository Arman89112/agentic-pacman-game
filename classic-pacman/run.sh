#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

FX=/usr/lib/jvm/java-27-openjfx/lib
OUT=out

if [ ! -d "$OUT" ]; then
    ./build.sh
fi

exec java --module-path "$FX" --add-modules javafx.controls \
     --enable-native-access=javafx.graphics \
     -Djava.library.path="$FX" \
     -cp "$OUT" com.pacman.PacmanApp "$@"
