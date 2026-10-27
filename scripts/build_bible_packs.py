#!/usr/bin/env python3
"""Build a chapter-queryable offline KJV and ASV SQLite pack from eBible.org USFM."""
from __future__ import annotations

import argparse
import re
import sqlite3
import tempfile
import urllib.request
import zipfile
from pathlib import Path

SOURCES = {
    "KJV": "https://ebible.org/Scriptures/eng-kjv2006_usfm.zip",
    "ASV": "https://ebible.org/Scriptures/eng-asv_usfm.zip",
}
BOOKS = [
    "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth",
    "1 Samuel", "2 Samuel", "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
    "Nehemiah", "Esther", "Job", "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon",
    "Isaiah", "Jeremiah", "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
    "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah",
    "Malachi", "Matthew", "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians",
    "2 Corinthians", "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians",
    "2 Thessalonians", "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James",
    "1 Peter", "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation",
]

FOOTNOTES = re.compile(r"\\(?:f|x)\s.*?\\(?:f|x)\*", re.DOTALL)
WORD_WITH_ATTRIBUTES = re.compile(r"\\\+?w\s+(.*?)\|.*?\\\+?w\*", re.DOTALL)
MARKUP = re.compile(r"\\[+A-Za-z0-9-]+\*?")
CHAPTER = re.compile(r"\\c\s+(\d+)")
VERSE = re.compile(r"\\v\s+(\d+[a-z]?(?:-\d+[a-z]?)?)\s*(.*?)(?=\\v\s+\d|\Z)", re.DOTALL)


def fetch_archive(version: str, supplied: str | None, temp_dir: Path) -> Path:
    if supplied:
        return Path(supplied)
    target = temp_dir / f"{version.lower()}_usfm.zip"
    request = urllib.request.Request(SOURCES[version], headers={"User-Agent": "Makarios Bible pack builder"})
    with urllib.request.urlopen(request, timeout=60) as response, target.open("wb") as output:
        output.write(response.read())
    return target


def clean_text(text: str) -> str:
    text = WORD_WITH_ATTRIBUTES.sub(lambda m: m.group(1), text)
    text = text.replace("\ufffd", "")
    text = FOOTNOTES.sub(" ", text)
    text = MARKUP.sub(" ", text)
    text = re.sub(r"\\[^\s]+", " ", text)
    text = text.replace("~", " ").replace('\\"', '"').replace("\\'", "'")
    return re.sub(r"\s+", " ", text).strip()


def parse_book(data: bytes, version: str, book: str) -> list[tuple[str, str, int, int, str]]:
    text = data.decode("utf-8-sig")
    text = FOOTNOTES.sub(" ", text)
    chunks = list(CHAPTER.finditer(text))
    rows = []
    for index, chapter_match in enumerate(chunks):
        chapter = int(chapter_match.group(1))
        end = chunks[index + 1].start() if index + 1 < len(chunks) else len(text)
        section = text[chapter_match.end():end]
        for verse_match in VERSE.finditer(section):
            verse_token = verse_match.group(1)
            verse = int(re.match(r"\d+", verse_token).group(0))
            verse_text = clean_text(verse_match.group(2))
            if verse_text:
                rows.append((version, book, chapter, verse, verse_text))
    return rows


def read_archive(path: Path, version: str) -> list[tuple[str, str, int, int, str]]:
    with zipfile.ZipFile(path) as archive:
        names = [name for name in archive.namelist() if name.lower().endswith(".usfm")]
        names.sort()
        scripture_files = []
        for name in names:
            data = archive.read(name)
            book_match = re.search(rb"(?m)^\\id\s+([A-Z0-9]{3})", data)
            if book_match and book_match.group(1).decode("ascii") not in {"FRT", "INT", "BAK", "OTH"}:
                scripture_files.append((name, data))
        if len(scripture_files) != 66:
            raise ValueError(f"{path} has {len(scripture_files)} scripture book files, expected 66")
        rows = []
        for book, (_, data) in zip(BOOKS, scripture_files):
            rows.extend(parse_book(data, version, book))
        return rows


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--kjv-zip")
    parser.add_argument("--asv-zip")
    parser.add_argument("--output", default="android/app/src/main/assets/bible_packs.db")
    args = parser.parse_args()
    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="makarios-bible-") as temporary:
        temp_dir = Path(temporary)
        kjv = read_archive(fetch_archive("KJV", args.kjv_zip, temp_dir), "KJV")
        asv = read_archive(fetch_archive("ASV", args.asv_zip, temp_dir), "ASV")
        if output.exists():
            output.unlink()
        db = sqlite3.connect(output)
        try:
            db.execute("CREATE TABLE verses(version TEXT NOT NULL, book TEXT NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, text TEXT NOT NULL, PRIMARY KEY(version, book, chapter, verse))")
            db.execute("CREATE INDEX idx_translation_chapter ON verses(version, book, chapter, verse)")
            db.executemany("INSERT INTO verses VALUES(?, ?, ?, ?, ?)", kjv + asv)
            db.commit()
            for version in ("KJV", "ASV"):
                count = db.execute("SELECT COUNT(*) FROM verses WHERE version = ?", (version,)).fetchone()[0]
                print(f"{version}: {count} verses")
        finally:
            db.close()
    print(f"Wrote {output}")


if __name__ == "__main__":
    main()

