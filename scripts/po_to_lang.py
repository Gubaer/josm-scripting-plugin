#!/usr/bin/env python3
"""Convert .po translation files to JOSM's .lang binary format.

The .lang format is defined by LangFileEncoder.kt in the gradle-josm-plugin:

  BASE LANGUAGE (en.lang):
    Singular section (no plural form, sorted):
      For each msgid:
        - If context: encode "_:context\\nmsgid" with 2-byte big-endian length + UTF-8
        - Else:       encode "msgid" with 2-byte big-endian length + UTF-8
    Separator: 0xFF 0xFF
    Plural section (has plural form, sorted):
      For each msgid:
        - 1-byte count of plural forms
        - For each form: 2-byte big-endian length + UTF-8

  TRANSLATED LANGUAGE (e.g. de.lang):
    Singular section (same order as en.lang):
      - 0x00 0x00  no translation
      - 0xFF 0xFE  translation identical to source
      - otherwise  2-byte big-endian length + UTF-8 of the translated string
    Separator: 0xFF 0xFF
    Plural section (same order as en.lang):
      - 0x00       no translation
      - 0xFE       translation identical to source
      - otherwise  1-byte count + for each form: 2-byte big-endian length + UTF-8

Sort order (matches MsgId.compareTo() in Kotlin):
  1. Number of strings ascending (singular before plural)
  2. Context: None first, then alphabetical
  3. String content alphabetical
"""

import argparse
import struct
import sys
from pathlib import Path
from typing import NamedTuple, Optional

import polib


class MsgId(NamedTuple):
    strings: tuple  # (singular,) for singular entries; (singular, plural_src) for plural
    context: Optional[str]

    def sort_key(self):
        """Matches MsgId.compareTo() in LangFileEncoder.kt."""
        return (
            len(self.strings),
            (1, self.context) if self.context is not None else (0, ""),
            self.strings,
        )


def encode_string(s: str) -> bytes:
    """Encode a string as 2-byte big-endian length + UTF-8 bytes."""
    data = s.encode("utf-8")
    size = len(data)
    if size >= 65534:  # 0xFFFE and 0xFFFF are reserved
        raise ValueError(
            f"String too long ({size} UTF-8 bytes, max 65533): {s[:80]!r}"
        )
    return struct.pack(">H", size) + data


def build_sorted_msgids(po_files: list[polib.POFile]) -> tuple[list[MsgId], list[MsgId]]:
    """Build sorted singular and plural MsgId lists from a collection of .po files.

    Each .po file contains the full set of source msgids (even untranslated ones),
    so the canonical msgid list can be derived from the .po files alone without a .pot.

    Matches the constructor logic in LangFileEncoder.kt:
      baseMsgIds.distinct().sorted().partition { it.id.strings.size <= 1 }
    """
    seen: set[MsgId] = set()
    all_msgids: list[MsgId] = []
    for po in po_files:
        for entry in po:
            if entry.msgid == "":
                continue  # skip gettext header (GETTEXT_HEADER_MSGID)
            strings = (
                (entry.msgid, entry.msgid_plural) if entry.msgid_plural else (entry.msgid,)
            )
            msgid = MsgId(strings=strings, context=entry.msgctxt or None)
            if msgid not in seen:
                seen.add(msgid)
                all_msgids.append(msgid)

    all_msgids.sort(key=MsgId.sort_key)
    singular = [m for m in all_msgids if len(m.strings) <= 1]
    plural = [m for m in all_msgids if len(m.strings) > 1]
    return singular, plural


def build_translations(po: polib.POFile) -> dict[MsgId, tuple]:
    """Build a mapping MsgId -> translated_strings tuple from a .po file.

    Only includes actually translated (non-empty) entries.
    """
    translations: dict[MsgId, tuple] = {}
    for entry in po:
        if entry.msgid == "":
            continue
        if entry.msgid_plural:
            src_strings = (entry.msgid, entry.msgid_plural)
            trans_strings = tuple(
                v for _, v in sorted(entry.msgstr_plural.items())
            )
            if all(t == "" for t in trans_strings):
                continue  # untranslated
        else:
            src_strings = (entry.msgid,)
            if not entry.msgstr:
                continue  # untranslated
            trans_strings = (entry.msgstr,)
        msgid = MsgId(strings=src_strings, context=entry.msgctxt or None)
        translations[msgid] = trans_strings
    return translations


def encode_base_lang(
    singular_msgids: list[MsgId], plural_msgids: list[MsgId]
) -> bytes:
    """Encode en.lang (base language file).

    Matches encodeToBaseLanguageByteArray() in LangFileEncoder.kt.
    """
    result = bytearray()

    # Singular section
    for msgid in singular_msgids:
        # Context is embedded as "_:context\n" prefix in the base language file
        s = (
            f"_:{msgid.context}\n{msgid.strings[0]}"
            if msgid.context is not None
            else msgid.strings[0]
        )
        result += encode_string(s)

    result += b"\xff\xff"  # singular/plural separator

    # Plural section
    for msgid in plural_msgids:
        num_forms = len(msgid.strings)
        if num_forms >= 254:
            raise ValueError(f"Too many plural forms ({num_forms}, max 253)")
        result += bytes([num_forms])
        for s in msgid.strings:
            result += encode_string(s)

    return bytes(result)


def encode_translation(
    singular_msgids: list[MsgId],
    plural_msgids: list[MsgId],
    translations: dict[MsgId, tuple],
) -> bytes:
    """Encode a translated .lang file.

    Matches encodeToByteArray() in LangFileEncoder.kt.
    """
    result = bytearray()

    # Singular section
    for msgid in singular_msgids:
        trans = translations.get(msgid)
        if trans is None:
            result += b"\x00\x00"  # no translation
        elif trans == msgid.strings:
            result += b"\xff\xfe"  # same as source
        else:
            result += encode_string(trans[0])

    result += b"\xff\xff"  # singular/plural separator

    # Plural section
    for msgid in plural_msgids:
        trans = translations.get(msgid)
        if trans is None:
            result += b"\x00"  # no translation
        elif trans == msgid.strings:
            result += b"\xfe"  # same as source
        else:
            num_forms = len(trans)
            if num_forms >= 254:
                raise ValueError(f"Too many plural forms ({num_forms}, max 253)")
            result += bytes([num_forms])
            for form in trans:
                result += encode_string(form)

    return bytes(result)


def main():
    parser = argparse.ArgumentParser(
        description="Convert .po translation files to JOSM .lang binary format"
    )
    parser.add_argument(
        "--po-dir", required=True, help="Directory containing .po translation files"
    )
    parser.add_argument(
        "--output-dir", required=True, help="Output directory for .lang files"
    )
    args = parser.parse_args()

    po_dir = Path(args.po_dir)
    output_dir = Path(args.output_dir)

    po_paths = sorted(po_dir.glob("*.po")) if po_dir.exists() else []
    if not po_paths:
        print(f"No .po files found in {po_dir}, nothing to do")
        return

    output_dir.mkdir(parents=True, exist_ok=True)

    po_files = [polib.pofile(str(p)) for p in po_paths]
    singular_msgids, plural_msgids = build_sorted_msgids(po_files)
    print(
        f"Found {len(singular_msgids)} singular, {len(plural_msgids)} plural msgids"
    )

    # Generate en.lang (base language — defines the string order for all other .lang files)
    en_bytes = encode_base_lang(singular_msgids, plural_msgids)
    en_path = output_dir / "en.lang"
    en_path.write_bytes(en_bytes)
    print(f"Written {en_path} ({len(en_bytes)} bytes)")

    # Generate .lang for each .po translation file
    for po_path, po in zip(po_paths, po_files):
        lang = po_path.stem
        translations = build_translations(po)
        lang_bytes = encode_translation(singular_msgids, plural_msgids, translations)
        out_path = output_dir / f"{lang}.lang"
        out_path.write_bytes(lang_bytes)
        print(
            f"Written {out_path} "
            f"({len(lang_bytes)} bytes, {len(translations)} translated strings)"
        )


if __name__ == "__main__":
    main()
