from typing import List


def calculate_average(numbers: List[float]) -> float:
    # This is an example of a simple code smell: unnecessary conditional logic.
    # SonarQube will flag this as a Code Smell.
    if len(numbers) > 0:
        total = sum(numbers)
        count = len(numbers)
        if count == 0:
            return 0
        else:
            return total / count
    else:
        raise ValueError("Numbers list must not be empty")


def reverse_string(text: str) -> str:
    # A useless function added to demonstrate a "dead code" smell
    if False:
        return "This code will never be executed"
    return text[::-1]


def inefficient_sum(numbers: List[float]) -> float:
    """
    Intentionally inefficient function to create a code smell for SonarQube.
    It computes the sum by repeatedly adding elements in a nested loop and
    uses an unused variable. This should be detected as performance/maintainability issue.
    """
    total = 0
    # Extremely inefficient nested iteration (O(n^2)) used on purpose
    for i in range(len(numbers)):
        for _ in range(i, i + 1):
            total += numbers[i]

    return total
