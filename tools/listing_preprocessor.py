#!/usr/bin/env python3
"""An mdBook preprocessor for the book's code from code/.

Each chapter folder in code/ has a listing/ folder with one file per class, and a solution/ folder
with the files for one answer to the chapter's exercise.

`{{#listing ch01-14}}` shows the chapter's "Putting it together" code: a code block for each file
that's new or changed since the previous chapter's listing, with the parts that didn't change hidden.
Files that didn't change at all go in a collapsed section, and files that went away are listed so
students know they can delete them.

`{{#solution ch01-14}}` shows the files the exercise asked students to change, compared to that
chapter's listing, the same way.

The eye button on a code block shows its hidden lines, and the copy button always copies the whole
file (see theme/copy-full-code.js). Hidden lines are marked with `~`, the prefix set in book.toml
under [output.html.code.hidelines].

Both tags go on a line by themselves, not inside a ``` code block, because they make the code blocks.
"""

import difflib
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CODE = ROOT / "code"
TAG = re.compile(r"\{\{#(listing|solution) ([\w-]+)\}\}")


def members(lines):
    """Says which member (field, method, or constructor) each line belongs to.

    Gives back a list with one entry per line: the member's number, "top" for lines like
    `class Player {` and its closing `}`, or None for blank lines.
    The code is indented with tabs, so anything starting with one tab at the top of a class body
    starts a new member, except right after an annotation like `@Override`.
    """
    owner = []
    braces = 0
    count = -1
    previous = ""
    for line in lines:
        if not line.strip():
            owner.append(None)
            continue
        if not line.startswith("\t"):
            owner.append("top")
        elif braces == 1 and not previous.startswith("@"):
            count += 1
            owner.append(count)
        else:
            owner.append(count)
        braces += line.count("{") - line.count("}")
        previous = line.strip()
    return owner


def hidden_lines(old, new):
    owner = members(new)
    changed = set()
    added = set()
    for tag, i1, i2, j1, j2 in difflib.SequenceMatcher(None, old, new, autojunk=False).get_opcodes():
        if tag == "insert":
            # An inserted method usually starts or ends with the same lines as its neighbor, like `}`,
            # so the same insert could be drawn in more than one place. Slide it as far down as it goes,
            # so it starts with the new method's own first line instead of the end of the one before.
            while j2 < len(new) and new[j1] == new[j2]:
                j1 += 1
                j2 += 1
            added.update(range(j1, j2))
        if tag in ("replace", "insert"):
            changed.update(range(j1, j2))
        elif tag == "delete" and 0 < j1 < len(new) and owner[j1 - 1] == owner[j1]:
            # Lines were taken out of the middle of a member, so that member changed.
            changed.add(j1)

    changed_members = {owner[i] for i in changed if isinstance(owner[i], int)}
    visible = [isinstance(m, int) and m in changed_members for m in owner]

    # Show a class's first and last lines if anything inside it is showing, or if they changed.
    start = 0
    for i, line in enumerate(new):
        if owner[i] == "top" and line.rstrip().endswith("{"):
            start = i
        elif owner[i] == "top":
            if any(visible[start:i]) or start in changed:
                visible[start] = visible[i] = True
                if start in added:
                    # A whole new class: show all of it.
                    visible[start:i + 1] = [True] * (i + 1 - start)

    # Keep one blank line after anything showing, as long as more showing code comes after it
    # (other than the end of the class).
    for i, line in enumerate(new):
        if owner[i] is not None:
            continue
        before = next((k for k in range(i - 1, -1, -1) if owner[k] is not None), None)
        after = next((k for k in range(i + 1, len(new)) if visible[k]), None)
        visible[i] = (
            before is not None
            and visible[before]
            and after is not None
            and new[after].strip() != "}"
        )

    return [not shown for shown in visible]


def read_lines(path):
    return path.read_text().rstrip("\n").split("\n")


def read_files(folder):
    return {path.name: read_lines(path) for path in sorted(folder.glob("*.java"))}


def split_imports(lines):
    """Separates a file's `import` lines (and the blank line after them) from the rest of it."""
    count = 0
    while count < len(lines) and (lines[count].startswith("import ") or not lines[count].strip()):
        count += 1
    return lines[:count], lines[count:]


def code_block(name, lines, hidden):
    body = "\n".join("~" + line if hide else line for line, hide in zip(lines, hidden))
    return f"`{name}`:\n```java\n{body}\n```"


def changed_file(name, old, new):
    """A code block for a file that's new or changed, with the parts that didn't change hidden."""
    if old is None:
        return code_block(name, new, [False] * len(new))
    old_imports, old_code = split_imports(old)
    new_imports, new_code = split_imports(new)
    # Show the imports only if they changed, so students notice a new one.
    imports_hidden = [new_imports == old_imports or not line.strip() for line in new_imports]
    return code_block(name, new, imports_hidden + hidden_lines(old_code, new_code))


def file_list(names):
    names = [f"`{name}`" for name in names]
    return names[0] if len(names) == 1 else ", ".join(names[:-1]) + " and " + names[-1]


def render(tag, chapter):
    # Students' folders have last chapter's files, plus any they made for last chapter's exercise.
    had = set()
    if tag == "solution":
        old = read_files(CODE / chapter / "listing")
        new = read_files(CODE / chapter / "solution")
    else:
        folders = sorted(path.name for path in CODE.iterdir() if path.is_dir())
        position = folders.index(chapter)
        old = read_files(CODE / folders[position - 1] / "listing") if position > 0 else {}
        new = read_files(CODE / chapter / "listing")
        if position > 0:
            had = set(old) | {path.name for path in (CODE / folders[position - 1] / "solution").glob("*.java")}

    blocks = [changed_file(name, old.get(name), lines) for name, lines in new.items() if old.get(name) != lines]
    unchanged = [name for name, lines in new.items() if old.get(name) == lines]
    removed = sorted(name for name in had if name not in new)

    if removed:
        if len(removed) == 1:
            blocks.append(f"{file_list(removed)} isn't needed anymore, so you can delete it.")
        else:
            blocks.append(f"{file_list(removed)} aren't needed anymore, so you can delete them.")
    if tag == "listing" and unchanged:
        same = "\n\n".join(code_block(name, new[name], [False] * len(new[name])) for name in unchanged)
        blocks.append(f"<details>\n<summary>Files that didn't change</summary>\n\n{same}\n\n</details>")
    # Start with a blank line, so the first file's name doesn't run into the sentence before the tag.
    return "\n" + "\n\n".join(blocks) + "\n"


def process(items):
    for item in items:
        chapter = item.get("Chapter") if isinstance(item, dict) else None
        if not chapter:
            continue
        chapter["content"] = TAG.sub(lambda m: render(m.group(1), m.group(2)), chapter["content"])
        process(chapter.get("sub_items", []))


def main():
    if len(sys.argv) > 1 and sys.argv[1] == "supports":
        sys.exit(0)
    context, book = json.load(sys.stdin)
    # mdBook 0.5 calls the list of chapters "items". Older versions called it "sections".
    process(book.get("items", book.get("sections", [])))
    json.dump(book, sys.stdout)


if __name__ == "__main__":
    main()
