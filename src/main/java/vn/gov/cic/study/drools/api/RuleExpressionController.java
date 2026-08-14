package vn.gov.cic.study.drools.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import vn.gov.cic.study.drools.service.RuleExpressionParseService;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleExpressionController {

    private final RuleExpressionParseService parseService;

    @PostMapping("/parse")
    public RuleExpressionParseResponse parse(@Valid @RequestBody RuleExpressionParseRequest request) {
        parseService.parse(request.expression());
        return new RuleExpressionParseResponse("Parse OK", request.expression());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public RuleExpressionParseError handleParseError(IllegalArgumentException ex) {
        return new RuleExpressionParseError(ex.getMessage());
    }

    public record RuleExpressionParseRequest(
            @NotBlank String expression
    ) {
    }

    public record RuleExpressionParseResponse(
            String result,
            String expression
    ) {
    }

    public record RuleExpressionParseError(
            String error
    ) {
    }
}
