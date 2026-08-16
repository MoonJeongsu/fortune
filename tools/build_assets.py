"""Convert the legacy MSSQL inserts into the app's offline JSON asset."""

from __future__ import annotations

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "SKT 오늘의 운세_20150130" / "TodayFortune.sql"
OUTPUT = ROOT / "DakbitFortune" / "app" / "src" / "main" / "assets" / "fortune_data.json"

INSERT_RE = re.compile(
    r"^INSERT \[dbo\]\.\[(?P<table>[^\]]+)\] "
    r"\((?P<columns>.+)\) VALUES \((?P<values>.*)\)$"
)

# 원본의 문체는 유지하고 명백한 오타만 교정한다.
TEXT_CORRECTIONS = {
    "그 러나": "그러나",
    "쌓여 있던 것을이": "쌓여 있던 것들이",
    "조심스렇게": "조심스럽게",
    "쓸데없지 잡지": "쓸데없이 잡지",
    "길운은 주게": "길운을 주게",
    "어는 한 사람": "어느 한 사람",
    "금새": "금세",
    "왠만하면": "웬만하면",
    "깊숙히": "깊숙이",
    "가릴려고": "가리려고",
    "깍이기": "깎이기",
    "있더라고": "있더라도",
    "흥겨웁게": "흥겹게",
    "생기기 쉽도": "생기기 쉽고",
    "뒷풀이": "뒤풀이",
    "있음으로": "있으므로",
    "날개 돋힌": "날개 돋친",
    "건강에 무리하지만 않는다면": "건강에 무리만 하지 않는다면",
    "가시가 돋혀있는": "가시가 돋아 있는",
    "좋아지는데 불구하고": "좋아지는데도 불구하고",
    "마이나스로": "마이너스로",
    "기분아 나빠지는": "기분이 나빠지는",
    "오늘 해야 할 일이 빨리 결정하고": "오늘 해야 할 일을 빨리 결정하고",
    "저조되기": "저조해지기",
    "차이가 하는 인연": "차이가 나는 인연",
    "불만을 들어내는": "불만을 드러내는",
    "휴혹": "유혹",
    "잇는": "있는",
    "잇습니다": "있습니다",
    "애기 다루듯": "아기 다루듯",
    "댓가": "대가",
    "수리 력": "수리력",
    "스타디 모임": "스터디 모임",
    "피할려면": "피하려면",
    "엔틱한": "앤티크한",
    "콘텍트": "콘택트",
    "스타일리쉬한": "스타일리시한",
    "여러 보이고": "여려 보이고",
    "차분한 컨셉트 어울리는": "차분한 컨셉트가 어울리는",
    "날카로운 이상": "날카로운 인상",
    "그러한 컨셉트 나쁘지 않은": "그러한 컨셉트가 나쁘지 않은",
    "캐쥬얼한": "캐주얼한",
    "지낸다,라는": "지낸다, 라는",
    "좋겠습니다. .": "좋겠습니다.",
    "'스포츠맨 스타일": "스포츠맨 스타일",
    "기운을 띄고 있는": "기운을 띠고 있는",
    "저녁 10시 이후나 운이": "저녁 10시 이후에 운이",
    "대리를 부리는 것이": "대리를 부르는 것이",
    "좋지 않 일로": "좋지 않은 일로",
    "담박에": "단박에",
    "믿음 감을 있지만": "믿음감은 있지만",
    "오랜 된 연인": "오래된 연인",
    "대면 대면하게": "데면데면하게",
    "많이 틀린 사람": "많이 다른 사람",
    "처음과 끝이 틀린 때": "처음과 끝이 다른 때",
    "바람을 피는 것이": "바람을 피우는 것이",
    "밖으로 나가기 싶어집니다": "밖으로 나가기 싫어집니다",
    "매력을 품어내지만": "매력을 뿜어내지만",
    "믿음 감이 깨지는": "믿음이 깨지는",
    "기존이 애인과": "기존의 애인과",
    "기다리고 있는다고": "기다리고 있다고",
    "계돈": "곗돈",
    "들여 매워야": "들여 메워야",
    "거래 운행의 통장": "거래 은행의 통장",
    "재물이 잘 세어 나가지": "재물이 잘 새어 나가지",
    "단기성 일거리의 제안을 받을 제안이 들어오니": "단기성 일거리의 제안이 들어오니",
    "당신의 일을 도와 주력 하는 날": "당신의 일을 도와주려 하는 날",
    "소득을 바래서는": "소득을 바라서는",
    "보상 없는 베품": "보상 없는 베풂",
    "독촉장이 날라옵니다": "독촉장이 날아옵니다",
    "얼은 몸을": "언 몸을",
    "한 문제식": "한 문제씩",
    "용기가 쏟아나고": "용기가 솟아나고",
    "기운을 돋아 주어야": "기운을 돋워 주어야",
    "평시 실력보다 잘 점수가 잘 나오는": "평시 실력보다 점수가 잘 나오는",
    "성적인 떨어지는데": "성적이 떨어지는데",
    "푸짐안 식사": "푸짐한 식사",
    "뒤쳐지기 쉬운": "뒤처지기 쉬운",
    "습관을 길어야": "습관을 길러야",
    "건강 관리만 잘 두었다면": "건강 관리만 잘해 두었다면",
    "확실하게 집고 넘어가는": "확실하게 짚고 넘어가는",
    "번갈아 가면 공부하는": "번갈아 가며 공부하는",
    "매우 좋지 않는 날": "매우 좋지 않은 날",
    "열정이 쏟아나고": "열정이 솟아나고",
    "재미가 새록새록 쏟아납니다": "재미가 새록새록 솟아납니다",
    "답을 태워 나가는": "답을 채워 나가는",
    "그 대가가 돌아오면 학업에": "그 대가가 돌아오며 학업에",
    "틀리 답을": "틀린 답을",
    "결과가 분명하게 들어나게": "결과가 분명하게 드러나게",
    "만날려면": "만나려면",
    "만나볼려면": "만나보려면",
    "이성운이 보이는 시기이는": "이성운이 보이는 시기는",
    "행보나 여행을 하실려면": "행보나 여행을 하시려면",
    "여행을 떠날려면": "여행을 떠나려면",
    "액센트가": "악센트가",
    "갈색의 아이템이나 이나 액세서리": "갈색의 아이템이나 액세서리",
    "좋은 날입이다": "좋은 날입니다",
    "부드러움을 같이 보여준다며": "부드러움을 같이 보여준다면",
    "주름이 많이 들어가 플레어 스커트나 체크무늬가 들어가 스커트": "주름이 많이 들어간 플레어 스커트나 체크무늬가 들어간 스커트",
    "콘택트 렌즈": "콘택트렌즈",
    "래깅스로": "레깅스로",
    "잘 못 하다간": "잘못하다간",
    "잘 못된": "잘못된",
    "잘 못 알아": "잘못 알아",
    "잘 못 타": "잘못 타",
    "고민할 수록": "고민할수록",
    "될 것 입니다": "될 것입니다",
    "줄 것 입니다": "줄 것입니다",
    "할 것 입니다": "할 것입니다",
}


def correct_text(text: str) -> str:
    corrected = text
    for before, after in TEXT_CORRECTIONS.items():
        corrected = corrected.replace(before, after)
    # 문장부호 뒤의 누락된 공백만 보정한다.
    return re.sub(r"([.!?])(?=[가-힣])", r"\1 ", corrected)


def split_sql_values(source: str) -> list[str]:
    values: list[str] = []
    current: list[str] = []
    quoted = False
    index = 0

    while index < len(source):
        char = source[index]
        if char == "'":
            current.append(char)
            if quoted and index + 1 < len(source) and source[index + 1] == "'":
                current.append("'")
                index += 1
            else:
                quoted = not quoted
        elif char == "," and not quoted:
            values.append("".join(current).strip())
            current = []
        else:
            current.append(char)
        index += 1

    values.append("".join(current).strip())
    return values


def sql_value(value: str):
    if value.upper() == "NULL":
        return None
    if value.startswith("N'") or value.startswith("'"):
        start = 2 if value.startswith("N'") else 1
        return correct_text(value[start:-1].replace("''", "'").strip())
    try:
        number = float(value)
        return int(number) if number.is_integer() else number
    except ValueError:
        return value


def main() -> None:
    tables: dict[str, list[dict]] = {}
    for line in SOURCE.read_text(encoding="utf-16").splitlines():
        match = INSERT_RE.match(line.strip())
        if not match:
            continue

        columns = re.findall(r"\[([^\]]+)\]", match.group("columns"))
        values = [sql_value(value) for value in split_sql_values(match.group("values"))]
        if len(columns) != len(values):
            raise ValueError(f"Column/value mismatch in {match.group('table')}")

        tables.setdefault(match.group("table"), []).append(dict(zip(columns, values)))

    expected = 520
    count = sum(len(rows) for rows in tables.values())
    if count != expected:
        raise ValueError(f"Expected {expected} rows, parsed {count}")

    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(
        json.dumps({"version": 1, "tables": tables}, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )
    print(f"Wrote {count} rows across {len(tables)} tables to {OUTPUT}")


if __name__ == "__main__":
    main()
