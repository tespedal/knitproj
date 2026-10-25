import pytest

from knitproj.domain.pattern import PatternParagraph, PatternResult, PatternSection


def test_pattern_section_snapshots_paragraphs_as_immutable_collection():
    paragraph = PatternParagraph("Title", "Body")
    paragraphs = [paragraph]

    section = PatternSection("Section", paragraphs)
    paragraphs.clear()

    assert section.paragraphs == (paragraph,)
    with pytest.raises(TypeError):
        section.paragraphs[0] = PatternParagraph("Changed", "Body")


def test_pattern_result_snapshots_sections_as_immutable_collection():
    section = PatternSection("Section", [])
    sections = [section]

    result = PatternResult(sections)
    sections.clear()

    assert result.sections == (section,)
    with pytest.raises(TypeError):
        result.sections[0] = PatternSection("Changed", [])
