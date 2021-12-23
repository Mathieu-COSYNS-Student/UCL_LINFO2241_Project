#!/bin/sh

YES=0
Y_OR_N_DEFAULT="Y"
MESSAGE="Are You Sure?"

while getopts ":m:yN" OPT; do
  case $OPT in
    m)
      MESSAGE="$OPTARG"
      shift            
    ;;
    N)
      Y_OR_N_DEFAULT="N"
    ;;
    y)
      YES=1            
    ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
    ;;
    :*)
      echo "Option -$OPTARG requires an argument." >&2
      exit 1
    ;;
  esac
  shift
done

[ "$YES" -eq 1 ] && exit 0

Y_OR_N=$([ $Y_OR_N_DEFAULT = "Y" ] && echo "[Y/n]" || echo "[y/N]")
YES_OR_NO_DMENU=$([ $Y_OR_N_DEFAULT = "Y" ] && echo "Yes\nNo" || echo "No\nYes")

while true
do
  command -v dmenu > /dev/null && input=$(echo "$YES_OR_NO_DMENU" | dmenu -p "$MESSAGE") || read -r -p "$MESSAGE $Y_OR_N " input
 
  case $input in
    [yY][eE][sS]|[yY])
      exit 0
    break
    ;;
    [nN][oO]|[nN])
      exit 1
    break
    ;;
    "")
      [ "$Y_OR_N_DEFAULT" = "Y" ] && exit 0 || exit 1
    ;;
    *)
      echo "Invalid input..."
    ;;
  esac
done
