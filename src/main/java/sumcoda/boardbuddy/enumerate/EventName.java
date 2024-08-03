package sumcoda.boardbuddy.enumerate;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventName {

    APPLY_PARTICIPATION("applyParticipationApplication"),
    APPROVE_PARTICIPATION("approveParticipationApplication"),
    REJECT_PARTICIPATION("rejectParticipationApplication"),
    CANCEL_PARTICIPATION("cancelParticipationApplication"),
    REVIEW_REQUEST("reviewRequest"),
    WRITE_COMMENT("writeComment");

    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return this.value;
    }
}
