from pathlib import Path


ROOT = Path(__file__).resolve().parent
SOURCE = ROOT / "FRONTEND_UI_REQUIREMENTS_NEW.md"
LEGACY = ROOT / "FRONTEND_UI_REQUIREMENTS.md"


def main() -> None:
    content = SOURCE.read_text(encoding="utf-8")
    LEGACY.write_text(content, encoding="utf-8")
    print(f"Synced {LEGACY.name} from {SOURCE.name}")


if __name__ == "__main__":
    main()
