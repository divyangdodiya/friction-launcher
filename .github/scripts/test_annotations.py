"""Turns failed JUnit results into GitHub annotations so the failing test and message show on the run."""

import glob
import os
import sys
import xml.etree.ElementTree as ET


def escape(text: str) -> str:
    return text.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")


def main(results_dir: str) -> None:
    reports = glob.glob(os.path.join(results_dir, "*.xml"))
    if not reports:
        print(f"No test results in {results_dir}; the tests may not have compiled.")
        return
    for report in reports:
        for case in ET.parse(report).getroot().iter("testcase"):
            for problem in list(case.findall("failure")) + list(case.findall("error")):
                name = f"{case.get('classname')}.{case.get('name')}"
                message = problem.get("message") or ""
                detail = "\n".join((problem.text or "").strip().splitlines()[:8])
                print(f"::error title={escape(name)}::{escape(message + chr(10) + detail)}")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "app/build/test-results/testDebugUnitTest")
