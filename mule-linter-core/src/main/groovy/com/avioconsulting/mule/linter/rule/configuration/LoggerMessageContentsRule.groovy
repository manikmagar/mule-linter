package com.avioconsulting.mule.linter.rule.configuration

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.configuration.LoggerComponent
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

import java.util.regex.Pattern

class LoggerMessageContentsRule extends Rule {

    static final String RULE_ID = 'LOGGER_MESSAGE_CONTENTS'
    static final String RULE_NAME = 'The Logger message does not contain undesired info. '
    static final String RULE_VIOLATION_MESSAGE = 'Logger is not allowed to have this message '
    static final String LOGGER_LEVEL = "INFO"
    static final Pattern DEFAULT_REGEX = ~/payload]/

    Pattern rulePattern
    Map<String, Pattern> rulesMap

    @Param("pattern") String pattern
    @Param("rules") Map<String, String> rules

    LoggerMessageContentsRule() {
        this(DEFAULT_REGEX)
    }

    LoggerMessageContentsRule(Pattern pattern) {
        super(RULE_ID, RULE_NAME)
        this.rulePattern = pattern
    }

    LoggerMessageContentsRule(Map<String, Pattern> rules) {
        super(RULE_ID, RULE_NAME)
        this.rulesMap = rules
    }

    @Override
    void init(){
        if(pattern != null) {
            this.rulePattern = Pattern.compile(pattern)
        }
        if(rules != null) {
            Map<String, Pattern> rulesParam = new HashMap<>()

            rules.forEach((key, value)->{
                rulesParam.put(key as String, Pattern.compile(value as String))
            })
            this.rulesMap = rulesParam
        }
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.configurationFiles.collect({it.findLoggerComponents()}).flatten().each {
            LoggerComponent loggerComponent ->
                if (rulesMap != null) {
                    if (rulesMap.any {loggerComponent.level == it.key && loggerComponent.message =~ it.value}) {
                        violations.add(new RuleViolation(this, loggerComponent.file.path, loggerComponent.lineNumber,
                                RULE_VIOLATION_MESSAGE + loggerComponent.message))
                    }
                } else if (loggerComponent.level == LOGGER_LEVEL && loggerComponent.message =~ rulePattern) {
                    violations.add(new RuleViolation(this, loggerComponent.file.path, loggerComponent.lineNumber,
                            RULE_VIOLATION_MESSAGE + loggerComponent.message))
                }}
        return violations
    }
}
