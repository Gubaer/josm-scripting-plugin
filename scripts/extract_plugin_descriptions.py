#!/usr/bin/env python3
"""Extract Plugin-Description translations from .po files.

Looks up the English plugin description in every .po file and emits a JSON
map of {language_code: translated_description} for all languages that have a
translation. The output is consumed by the Gradle build to populate the
xx_Plugin-Description manifest attributes in the plugin JAR.
"""

import argparse
import json
from pathlib import Path

import polib


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--po-dir", required=True, help="Directory containing .po translation files"
    )
    parser.add_argument(
        "--description",
        required=True,
        help="English source string to look up (value of plugin.description)",
    )
    parser.add_argument(
        "--output", required=True, help="Output JSON file path"
    )
    args = parser.parse_args()

    po_dir = Path(args.po_dir)
    result = {}
    for po_path in sorted(po_dir.glob("*.po")):
        po = polib.pofile(str(po_path))
        for entry in po:
            if entry.msgid == args.description and entry.msgstr:
                result[po_path.stem] = entry.msgstr
                break

    Path(args.output).parent.mkdir(parents=True, exist_ok=True)
    Path(args.output).write_text(
        json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(f"Written {args.output} ({len(result)} translations)")


if __name__ == "__main__":
    main()
