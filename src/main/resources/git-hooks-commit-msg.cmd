#!/bin/bash

message=$(head -n1 "$1")

prefix="^(✨|🐛|⬆️|📝|♻️|🧪|🩹|💄|🚧) ?.+"
if ! [[ $message =~ $prefix ]]; then
  echo "    ☔ 커밋 메세지를 제대로 입력해 주세요. (예: ✨ 버튼수정)"
  exit 1
fi

exit 0
