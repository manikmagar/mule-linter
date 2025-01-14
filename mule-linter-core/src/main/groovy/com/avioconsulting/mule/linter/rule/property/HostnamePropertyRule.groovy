package com.avioconsulting.mule.linter.rule.property

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.PropertyFile
import com.avioconsulting.mule.linter.model.rule.Param
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

import java.util.regex.Pattern

/**
 * This rule checks that properties containing host or hostname should be fully qualified domain names, and not IP addresses.
 * AVIO recommends that developers avoid using IP addresses, as they can be frequently changed and constrain routing options.
 */
class HostnamePropertyRule extends Rule{

    static final String RULE_ID = 'HOSTNAME_PROPERTY'
    static final String RULE_NAME = 'Hostname is not an IP Address. '
    static final String RULE_VIOLATION_MESSAGE = 'Hostname should not be an IP Address: '
    static final Pattern IPV4_REGEX = ~/^(?:[0-9]{1,3}\.){3}[0-9]{1,3}\u0024/
    static final Pattern IPV6_REGEX = ~/^(?:[A-F0-9]{1,4}:){7}[A-F0-9]{1,4}\u0024/

    /**
     * exemptions: is a list of properties the rule should ignore.
     */
    @Param("exemptions") def exemptions = []

    HostnamePropertyRule() {
        super(RULE_ID, RULE_NAME)
    }

    @Override
    List<RuleViolation> execute(Application application) {
        List<RuleViolation> violations = []
        application.propertyFiles.each { PropertyFile file ->
            file.getProperties().each {
                String propName = it.key.toLowerCase()
                if ((exemptions.length == 0 || !exemptions.any {propName.contains(it.toLowerCase())}) &&
                        (propName.contains("host") || propName.contains("hostname"))) {
                    if (it.value.toString().trim() ==~ IPV4_REGEX || it.value.toString().trim() ==~ IPV6_REGEX) {
                        violations.add(new RuleViolation(this, file.getFile().path, 0,
                                RULE_VIOLATION_MESSAGE + propName))
                    }
                }
            }
        }
        return violations
    }
}
