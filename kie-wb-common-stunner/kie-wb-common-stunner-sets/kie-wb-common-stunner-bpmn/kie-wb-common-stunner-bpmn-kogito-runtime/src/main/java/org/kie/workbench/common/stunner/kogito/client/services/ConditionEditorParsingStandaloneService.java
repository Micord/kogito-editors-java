/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.kogito.client.services;

import java.text.ParseException;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import elemental2.promise.Promise;
import org.kie.workbench.common.stunner.bpmn.client.forms.fields.conditionEditor.ConditionEditorParsingService;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.Condition;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.FunctionDef;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ParamDef;
import org.kie.workbench.common.stunner.bpmn.forms.conditions.ParseConditionResult;

@ApplicationScoped
public class ConditionEditorParsingStandaloneService implements ConditionEditorParsingService {

    public static final String KIE_FUNCTIONS = "KieFunctions.";

    private int parseIndex;

    private String expression;

    private String functionName;

    private static final String FUNCTION_NAME_NOT_RECOGNIZED_ERROR = "The function name \"{0}\" is not recognized by system.";

    private static final String FUNCTION_CALL_NOT_FOUND_ERROR = "Function call was not found, a token like \"" + KIE_FUNCTIONS + "functionName(variable, params)\" is expected.";

    private static final String VALID_FUNCTION_CALL_NOT_FOUND_ERROR = "The \"" + KIE_FUNCTIONS + "\" keyword must be followed by one of the following function names: \"{0}\"";

    private static final String CONDITION_OUT_OF_BOUNDS_ERROR = "Out of bounds error, the condition has missing parameters or is not properly configured.";

    private static final String FUNCTION_CALL_NOT_CLOSED_PROPERLY_ERROR = "Function call \"{0}\" is not closed properly, character \")\" is expected.";

    private static final String FUNCTION_CALL_NOT_OPEN_PROPERLY_ERROR = "Function call \"{0}\" is not opened properly, character \"(\" is expected.";

    private static final String SENTENCE_NOT_CLOSED_PROPERLY_ERROR = "Condition not closed properly, character \";\" is expected.";

    private static final String FIELD_NAME_EXPECTED_ERROR = "A valid field name is expected.";

    private static final String PARAMETER_DELIMITER_EXPECTED_ERROR = "Parameter delimiter \",\" is expected.";

    private static final String STRING_PARAMETER_EXPECTED_ERROR = "String parameter value like \"some value\" is expected.";

    private static final String SENTENCE_EXPECTED_AT_POSITION_ERROR = "Sentence \"{0}\" is expected at position {1}.";

    private static final String BLANK_AFTER_RETURN_EXPECTED_ERROR = "Sentence \"{0}\" must be followed by a blank space or a line break.";

    private static final String METHOD_INVOCATION_EXPECTED_ERROR = "A method invocation is expected at position {0}.";

    private static final String METHOD_NOT_PROPERLY_OPENED_ERROR = "Method \"{0}\" invocation is not properly opened, character \"(\" is expected.";

    private static final String METHOD_NOT_PROPERLY_CLOSED_ERROR = "Method \"{0}\" invocation is not properly closed, character \")\" is expected.";

    private static final String RETURN_SENTENCE = "return";

    @Override
    public Promise<ParseConditionResult> call(final String input) {
        this.expression = input;
        this.parseIndex = expression != null ? 0 : -1;
        try {
            return Promise.resolve(new ParseConditionResult(parse()));
        }
        catch (ParseException e) {
            throw new RuntimeException("Failed get ParseConditionResult", e);
        }
    }

    public Condition parse() throws ParseException {
        parseReturnSentence();
        functionName = parseFunctionName();
        functionName = functionName.substring(KIE_FUNCTIONS.length());
        List<FunctionDef> functionDefs = FunctionsRegistry.getInstance().getFunctions(functionName);

        if (functionDefs.isEmpty()) {
            throw new RuntimeException(FUNCTION_NAME_NOT_RECOGNIZED_ERROR);
        }

        ParseException lastTryException = null;
        for (FunctionDef functionDef : functionDefs) {
            try {
                reset();
                return parse(functionDef);
            } catch (ParseException e) {
                lastTryException = e;
            }
        }
        throw lastTryException;
    }

    private Condition parse(FunctionDef functionDef) throws ParseException {
        parseReturnSentence();
        functionName = parseFunctionName();
        functionName = functionName.substring(KIE_FUNCTIONS.length());
        parseFunctionOpen();
        Condition condition = new Condition(functionName);
        String param;
        String[] variableParam;
        boolean first = true;

        for (ParamDef ignored : functionDef.getParams()) {
            if (first) {
                variableParam = parseVariableParam();
                param = variableParam[0] + (variableParam[1] != null ? ("." + variableParam[1]) : "");
                first = false;
            } else {
                parseParamDelimiter();
                param = parseStringParameter();
            }
            condition.addParam(param);
        }

        //all parameters were consumed
        parseFunctionClose();
        parseSentenceClose();

        return condition;
    }

    private void reset() {
        parseIndex = 0;
        functionName = null;
    }

    private void parseSentenceClose(){
        int index = nextNonBlank();
        if (index < 0 || expression.charAt(index) != ';') {
            throw new RuntimeException(SENTENCE_NOT_CLOSED_PROPERLY_ERROR);
        }

        parseIndex = index + 1;
        while (parseIndex < expression.length()) {
            if (isNoneBlank(expression.charAt(parseIndex))) {
                throw new RuntimeException(SENTENCE_NOT_CLOSED_PROPERLY_ERROR);
            }
            parseIndex++;
        }
    }

    private void parseReturnSentence() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || !expression.startsWith(RETURN_SENTENCE, index)) {
            throw new RuntimeException(SENTENCE_EXPECTED_AT_POSITION_ERROR);
        }
        setParseIndex(index + RETURN_SENTENCE.length());
        if (isNoneBlank(expression.charAt(parseIndex))) {
            throw new RuntimeException(BLANK_AFTER_RETURN_EXPECTED_ERROR);
        }
    }

    private String parseFunctionName() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || !expression.startsWith(KIE_FUNCTIONS, index)) {
            throw new RuntimeException(FUNCTION_CALL_NOT_FOUND_ERROR);
        }

        for (FunctionDef functionDef : FunctionsRegistry.getInstance().getFunctions()) {
            if (expression.startsWith(KIE_FUNCTIONS + functionDef.getName(), index)) {
                functionName = KIE_FUNCTIONS + functionDef.getName();
                break;
            }
        }

        if (functionName == null) {
            throw new RuntimeException(VALID_FUNCTION_CALL_NOT_FOUND_ERROR);
        }

        setParseIndex(index + functionName.length());
        return functionName;
    }

    private void setParseIndex(int parseIndex) throws ParseException {
        if (parseIndex >= expression.length()) {
            throw new RuntimeException(CONDITION_OUT_OF_BOUNDS_ERROR);
        }
        this.parseIndex = parseIndex;
    }

    private int nextNonBlank() {
        if (parseIndex < 0) {
            return -1;
        }
        for (int i = parseIndex; i < expression.length(); i++) {
            if (isNoneBlank(expression.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isNoneBlank(Character character) {
        return character != null && !Character.isWhitespace(character);
    }

    private void parseFunctionClose() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || expression.charAt(index) != ')') {
            throw new RuntimeException(FUNCTION_CALL_NOT_CLOSED_PROPERLY_ERROR);
        }
        setParseIndex(index + 1);
    }

    private String parseFunctionOpen() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || expression.charAt(index) != '(') {
            throw new RuntimeException(FUNCTION_CALL_NOT_OPEN_PROPERLY_ERROR);
        }
        setParseIndex(index + 1);
        return "(";
    }

    private String[] parseVariableParam() throws ParseException {
        String[] result = new String[2];
        String variableName = parseVariableName();
        String methodName = null;
        int index = nextNonBlank();
        if (index < 0) {
            throw new RuntimeException(FUNCTION_CALL_NOT_CLOSED_PROPERLY_ERROR);
        }
        if (expression.charAt(index) == '.') {
            setParseIndex(index + 1);
            methodName = parseMethodName();
        }
        result[0] = variableName;
        result[1] = methodName;
        return result;
    }

    private String parseVariableName() throws ParseException {
        int index = nextNonBlank();
        if (index < 0) {
            throw new RuntimeException(FIELD_NAME_EXPECTED_ERROR);
        }
        String result = parseJavaName(expression, index, new char[]{' ', '.', ',', ')', '\r', '\n', '\t'});
        setParseIndex(index + result.length());
        return result;
    }

    public static String parseJavaName(final String token, final int startIndex, final char[] stopCharacters) throws ParseException {
        if (startIndex < 0 || startIndex >= token.length()) {
            throw new IndexOutOfBoundsException("startIndex: " + startIndex + " exceeds token bounds: " + token);
        }
        final StringBuilder javaName = new StringBuilder();
        char currentChar;
        int currentIndex = startIndex;
        while (currentIndex < token.length()) {
            currentChar = token.charAt(currentIndex);
            if (containsChar(stopCharacters, currentChar)) {
                break;
            } else {
                javaName.append(currentChar);
            }
            currentIndex++;
        }

        if (javaName.length() == 0) {
            throw new ParseException("Expected java name was not found at position: " + startIndex, startIndex);
        }
        return javaName.toString();
    }

    private static boolean containsChar(char[] array, char valueToFind) {
        int startIndex = 0;
        if (array == null) {
            return false;
        }
        for (int i = startIndex; i < array.length; i++) {
            if (valueToFind == array[i]) {
                return true;
            }
        }
        return false;
    }

    private String parseMethodName() throws ParseException {
        int index = nextNonBlank();
        if (index < 0) {
            throw new RuntimeException(METHOD_INVOCATION_EXPECTED_ERROR);
        }
        String result = parseJavaName(expression, index, new char[]{' ', '\r', '\n', '\t', '('});
        setParseIndex(index + result.length());
        index = nextNonBlank();
        if (index < 0 || expression.charAt(index) != '(') {
            throw new RuntimeException(METHOD_NOT_PROPERLY_OPENED_ERROR);
        }
        setParseIndex(index + 1);
        index = nextNonBlank();
        if (index < 0 || expression.charAt(index) != ')') {
            throw new RuntimeException(METHOD_NOT_PROPERLY_CLOSED_ERROR);
        }
        setParseIndex(index + 1);
        return result + "()";
    }

    private void parseParamDelimiter() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || expression.charAt(index) != ',') {
            throw new RuntimeException(PARAMETER_DELIMITER_EXPECTED_ERROR);
        }
        setParseIndex(index + 1);
    }

    private String parseStringParameter() throws ParseException {
        int index = nextNonBlank();
        if (index < 0 || expression.charAt(index) != '"') {
            throw new ParseException(STRING_PARAMETER_EXPECTED_ERROR, parseIndex);
        }

        Character scapeChar = '\\';
        Character last = null;
        boolean strReaded = false;
        StringBuilder param = new StringBuilder();
        for (int i = index + 1; i < expression.length(); i++) {
            if (expression.charAt(i) == '"' && !scapeChar.equals(last)) {
                strReaded = true;
                break;
            } else {
                param.append(expression.charAt(i));
                last = expression.charAt(i);
            }
        }

        if (!strReaded) {
            throw new ParseException(STRING_PARAMETER_EXPECTED_ERROR, parseIndex);
        }
        setParseIndex(index + param.length() + 2);
        return param.toString();
    }
}
