#!/usr/bin/env bash
#
# Bumps every Camuda version deps and references.
# to the version you pass in. Used during the release process.
#
# Usage:   ./update-camunda-version.sh <released-version>
# Example: ./update-camunda-version.sh 8.7.8
# -------------------------------------------------------------------------

set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <new-version>" >&2
  exit 1
fi

new_version="$1"

###############################################################################
# Helper: cross-platform `sed -i`
###############################################################################
if sed --version >/dev/null 2>&1; then
  # GNU
  sed_i=(sed -E -i)
else
  # BSD (macOS)
  sed_i=(sed -E -i '')
fi

###############################################################################
# 1. README.md  tweak the sample dependency block
###############################################################################
"${sed_i[@]}" \
  '/<artifactId>zeebe-client-java<\/artifactId>/{ 
     n
     s@<version>[0-9.]+</version>@<version>'"$new_version"'</version>@
   }' \
  java/README.md

###############################################################################
# 2. Every pom.xml  update <zeebe.version> properties
###############################################################################
find . -name pom.xml -print0 |
  xargs -0 "${sed_i[@]}" \
    's@(<zeebe\.version>)[0-9.]+@\1'"$new_version"'@g'

echo "Camunda (Zeebe) version updated to ${new_version}"
