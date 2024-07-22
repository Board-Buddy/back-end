package sumcoda.boardbuddy.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewType {

    EXCELLENT("excellent"),
    GOOD("good"),
    BAD("bad"),
    NOSHOW("noshow");

    private final String value;
}
