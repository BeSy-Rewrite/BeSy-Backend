#!/bin/bash
COPY_TARGET="/${PWD##*/}-copy"

rm -rf "..${COPY_TARGET}"
mkdir -p "..${COPY_TARGET}"

# Build rsync exclude arguments
EXCLUDE_ARGS=("--exclude=.git")

[ -f .gitignore ] && EXCLUDE_ARGS+=("--filter=:- .gitignore")

rsync -a "${EXCLUDE_ARGS[@]}" ./ "../${COPY_TARGET}/"

cd "..${COPY_TARGET}" || exit 1

tree > tree.txt
mkdir -p ./flat
mv tree.txt ./flat/tree.txt

find . -path ./flat -prune -o -type f -print | while IFS= read -r file; do
  newname="${file#./}"
  newname="${newname//\//_}"
  mv "$file" "./flat/$newname"
done
