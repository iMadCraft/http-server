#! /bin/sh

set -e

ECHO="${ECHO="echo"}";
BASENAME="${BASENAME="basename"}";
DIRNAME="${DIRNAME="dirname"}";
AUTOCONF="${AUTOCONF="autoconf"}";
CHMOD="${CHMOD="chmod"}";

FILENAME="${FILENAME="$( ${BASENAME} "${0}" )"}";
PROJDIR="${PROJDIR="$( cd "$(${DIRNAME} "${0}")" && pwd )"}";

${ECHO} "${FILENAME} creating configure"
${AUTOCONF} "${PROJDIR}"/src/build/ac/root-configure.ac \
   >"${PROJDIR}"/configure

${CHMOD} +x "${PROJDIR}"/configure

