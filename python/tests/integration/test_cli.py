import subprocess
import sys


def test_cli_writes_pattern_with_intentional_default_needle_size(tmp_path):
    completed = subprocess.run(
        [sys.executable, "-m", "knitproj"],
        cwd=tmp_path,
        capture_output=True,
        check=True,
        text=True,
    )

    output_file = tmp_path / "output" / "tshirt-pattern.txt"
    output = output_file.read_text(encoding="utf-8")

    assert completed.stdout.strip() == "Pattern written to file."
    assert "rundpinne 4.5" in output
    assert output_file.read_bytes() == output.encode("utf-8")


def test_cli_prints_pattern_when_output_path_is_not_a_directory(tmp_path):
    output_path = tmp_path / "output"
    output_path.write_text("blocks directory creation", encoding="utf-8")

    completed = subprocess.run(
        [sys.executable, "-m", "knitproj"],
        cwd=tmp_path,
        capture_output=True,
        check=True,
        text=True,
    )

    assert completed.stdout.splitlines()[0] == "Couldn't write pattern to file."
    assert "rundpinne 4.5" in completed.stdout