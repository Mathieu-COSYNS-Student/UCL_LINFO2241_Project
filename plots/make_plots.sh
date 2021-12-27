#!/bin/sh

echo "Generating plots..."

BASEDIR=$(dirname $0)
OUTDIR="$BASEDIR/out"
GRAPHICS="--no-graphics"

if "$BASEDIR/yes-no.sh" -m "Do you want to see charts in a graphical interface ?"; then
  GRAPHICS=""
fi

mkdir -p "$OUTDIR"

"$BASEDIR/make_plot.py" \
  --type bar \
  -t "Request/Response elapsed time with no tasks in the server queue" \
  -x "Passwords" \
  -y "Request/Response elapsed time (in seconds)" \
  -i "$BASEDIR/test_password_length_basic.csv" -m "Basic Implementation" \
  -i "$BASEDIR/test_password_length_optimized.csv" -m "Optimized Implementation" \
  --out-png "$OUTDIR/test_password_length.png" \
  "$GRAPHICS" &

"$BASEDIR/make_plot.py" \
  --type bar \
  -t "Request/Response elapsed time with no tasks in the server queue" \
  -x "File sizes" \
  -y "Request/Response elapsed time (in seconds)" \
  -i "$BASEDIR/test_file_sizes_basic.csv" -m "Basic Implementation" \
  -i "$BASEDIR/test_file_sizes_optimized.csv" -m "Optimized Implementation" \
  --out-png "$OUTDIR/test_file_sizes.png" \
  "$GRAPHICS" &

wait $(jobs -p)
