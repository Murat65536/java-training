#!/usr/bin/env python3
"""Checks the book's code and links.

Every chapter folder in code/ is laid out the way a student's folder is:
  - listing/ has one file per class: the chapter's "Putting it together" code.
  - Game.java is the "Try it yourself" main.
  - solution/ has the files for one answer to the exercise.

For each chapter:
  1. The files in listing/ have to compile.
  2. Game.java is run together with the files in solution/, and every line it prints has to match
     the `// comment` at the end of its println.

Then every link between pages in src/ has to point at a page and heading that exist, and every\n{{#include}}, {{#listing}}, and {{#solution}} has to point at code that exists.

Run it from anywhere: python3 tools/check_code.py
"""

import re
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CODE = ROOT / "code"
SRC = ROOT / "src"

failures = []


def fail(message):
    failures.append(message)
    print(f"  FAIL: {message}")


def javac(files, out):
    return subprocess.run(["javac", "-d", str(out), *map(str, files)], capture_output=True, text=True)


def compiles(chapter):
    with tempfile.TemporaryDirectory() as out:
        result = javac(sorted((chapter / "listing").glob("*.java")), out)
    if result.returncode != 0:
        fail(f"{chapter.name}/listing doesn't compile:\n{result.stderr}")


def expected_output(check_source):
    # Every println in a checker ends with a comment saying what it should print.
    return re.findall(r"System\.out\.println\(.*\);\s*//\s*(.*)$", check_source, re.MULTILINE)


def check_matches(chapter):
    files = [chapter / "Game.java", *sorted((chapter / "solution").glob("*.java"))]
    with tempfile.TemporaryDirectory() as out:
        result = javac(files, out)
        if result.returncode != 0:
            fail(f"{chapter.name}: Game.java with the solution doesn't compile:\n{result.stderr}")
            return
        result = subprocess.run(["java", "-cp", out, "Game"], capture_output=True, text=True)
    if result.returncode != 0:
        fail(f"{chapter.name}: Game.java with the solution didn't run:\n{result.stderr}")
        return
    printed = result.stdout.splitlines()
    expected = expected_output((chapter / "Game.java").read_text())
    if printed != expected:
        fail(f"{chapter.name}: expected {expected}, but it printed {printed}")


def heading_ids(markdown):
    # The same ids mdBook gives headings: lowercase, punctuation removed, spaces turned into dashes.
    ids = set()
    for heading in re.findall(r"^#+ (.*)$", markdown, re.MULTILINE):
        text = re.sub(r"[^\w\- ]", "", heading.strip().lower())
        ids.add(text.replace(" ", "-"))
    return ids


def check_links():
    pages = {page.name: page.read_text() for page in SRC.glob("*.md")}
    ids = {name: heading_ids(text) for name, text in pages.items()}
    for name, text in sorted(pages.items()):
        for link in re.findall(r"\]\(([^)\s]+)\)", text):
            if re.match(r"[a-z]+:", link):
                continue
            page, _, anchor = link.partition("#")
            page = page or name
            if not page.endswith(".md"):
                # Images and other files live in src/ next to the pages.
                if not (SRC / page).exists():
                    fail(f"{name} uses {page}, which doesn't exist yet")
            elif page not in pages:
                fail(f"{name} links to {link}, but there's no page named {page}")
            elif anchor and anchor not in ids[page]:
                fail(f"{name} links to {link}, but {page} has no heading with id #{anchor}")


def check_includes():
    # Every {{#include}}, {{#listing}}, and {{#solution}} has to point at code that exists.
    for page in sorted(SRC.glob("*.md")):
        text = page.read_text()
        for path in re.findall(r"\{\{#include ([^}:]+)", text):
            if not (page.parent / path).exists():
                fail(f"{page.name} includes {path}, which doesn't exist")
        for tag, chapter in re.findall(r"\{\{#(listing|solution) ([\w-]+)\}\}", text):
            folder = CODE / chapter / ("listing" if tag == "listing" else "solution")
            if not folder.is_dir():
                fail(f"{page.name} uses {{{{#{tag} {chapter}}}}}, but there's no {folder.relative_to(ROOT)}")


def main():
    for chapter in sorted(path for path in CODE.iterdir() if path.is_dir()):
        print(chapter.name)
        compiles(chapter)
        check_matches(chapter)
    print("links")
    check_links()
    check_includes()

    if failures:
        print(f"\n{len(failures)} problem(s) found.")
        sys.exit(1)
    print("\nEverything works!")


if __name__ == "__main__":
    main()
