#!/bin/sh

echo "Generating plots..."

BASEDIR=$(dirname $0)
OUTDIR="$BASEDIR/out"
GRAPHICS="--no-graphics"

if "$BASEDIR/yes-no.sh" -m "Do you want to see charts in a graphical interface ?"; then
  GRAPHICS=""
fi

mkdir "$OUTDIR"

# Tache 1.1
"$BASEDIR/make_plot.py" \
  --type bar \
  -t "Request/Response elapsed time with no tasks in the server queue" \
  -x "Passwords" \
  -y "Request/Response elapsed time (in ms)" \
  -i "$BASEDIR/test_password_length_basic.csv" -m "Basic Implementation" \
  -i "$BASEDIR/test_password_length_optimized.csv" -m "Optimized Implementation" \
  -o "$OUTDIR/test_password_length" \
  "$GRAPHICS" &

wait $(jobs -p)
