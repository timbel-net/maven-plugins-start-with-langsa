#!/bin/sh

echo "    ✅ 커밋 규칙을 검사합니다. "

unset JAVA_TOOL_OPTIONS
./mvnw.cmd -Dfile.encoding=utf-8 checkstyle:check | grep -E "(^\[ERROR].+\..+:[0-9]+:[0-9]+.+$)|(^\[INFO] BUILD FAILURE$)"


RESULTS=$?

if [ $RESULTS -ne 1 ]; then
  echo "    🚫 체크스타일 규칙을 준수해주세요! "
  exit 1
fi

printf "    ❤️ Thanks for keep the code style! "
exit 0
