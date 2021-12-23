#!/bin/sh

BASEDIR=$(dirname $0)
OUTDIR="$BASEDIR/out"

[ -d "$OUTDIR" ] && \
  "$BASEDIR/yes-no.sh" -N -m "Do you want to remove all benchmark output files ?" && \
  rm -rfv "$OUTDIR"

exit 0