<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ include file="includes/header.jspf" %>
    <p class="sub">Good luck, <strong><c:out value="${sessionScope.playerName}"/></strong>! Difficulty: NORMAL</p>
    <p class="hint">For each question: type your numbers, press <strong>Run Seed Generator</strong>
        (it adds or subtracts a random number from 1 to 10), then choose the right answer.
        When all four questions are ready, press <strong>Submit Answers</strong>.</p>

    <c:if test="${not empty error}">
        <p class="msg error"><c:out value="${error}"/></p>
    </c:if>

    <form method="post" id="gameForm" action="${pageContext.request.contextPath}/game">
        <input type="hidden" name="action" value="submit">

        <div class="q" data-q="prime">
            <label>Q1 (2 marks) &ndash; Prime number</label>
            <small>Enter any whole number.</small>
            <div class="row inputs">
                <div><span>Your integer</span>
                    <input type="number" name="primeInput" step="1" required value="<c:out value='${param.primeInput}'/>"></div>
            </div>
            <button type="button" class="btn alt seed-btn">Run Seed Generator</button>
            <p class="msg error card-error" hidden></p>
            <div class="seed-out"></div>
        </div>

        <div class="q" data-q="mod">
            <label>Q2 (2 marks) &ndash; Modulus</label>
            <small>Enter two whole numbers. The divisor must not be zero. The seed generator changes the dividend.</small>
            <div class="row inputs">
                <div><span>Dividend</span>
                    <input type="number" name="dividend" step="1" required value="<c:out value='${param.dividend}'/>"></div>
                <div><span>Divisor</span>
                    <input type="number" name="divisor" step="1" required value="<c:out value='${param.divisor}'/>"></div>
            </div>
            <button type="button" class="btn alt seed-btn">Run Seed Generator</button>
            <p class="msg error card-error" hidden></p>
            <div class="seed-out"></div>
        </div>

        <div class="q" data-q="trig1">
            <label>Q3 (3 marks) &ndash; Trigonometry</label>
            <small>Like <strong>5 sin 30</strong>: an integer, sin / cos / tan, and an angle in degrees. The seed generator changes the integer.</small>
            <div class="row inputs">
                <div><span>Integer</span>
                    <input type="number" name="trig1Coef" step="1" required value="<c:out value='${param.trig1Coef}'/>"></div>
                <div><span>Function</span>
                    <select name="trig1Fn">
                        <option value="sin" ${param.trig1Fn == 'sin' ? 'selected' : ''}>sin</option>
                        <option value="cos" ${param.trig1Fn == 'cos' ? 'selected' : ''}>cos</option>
                        <option value="tan" ${param.trig1Fn == 'tan' ? 'selected' : ''}>tan</option>
                    </select></div>
                <div><span>Degree</span>
                    <input type="text" inputmode="decimal" name="trig1Angle" required value="<c:out value='${param.trig1Angle}'/>"></div>
            </div>
            <button type="button" class="btn alt seed-btn">Run Seed Generator</button>
            <p class="msg error card-error" hidden></p>
            <div class="seed-out"></div>
        </div>

        <div class="q" data-q="trig2">
            <label>Q4 (3 marks) &ndash; Trigonometry</label>
            <small>Like <strong>2 cos 60</strong>: an integer, sin / cos / tan, and an angle in degrees.</small>
            <div class="row inputs">
                <div><span>Integer</span>
                    <input type="number" name="trig2Coef" step="1" required value="<c:out value='${param.trig2Coef}'/>"></div>
                <div><span>Function</span>
                    <select name="trig2Fn">
                        <option value="sin" ${param.trig2Fn == 'sin' ? 'selected' : ''}>sin</option>
                        <option value="cos" ${param.trig2Fn == 'cos' ? 'selected' : ''}>cos</option>
                        <option value="tan" ${param.trig2Fn == 'tan' ? 'selected' : ''}>tan</option>
                    </select></div>
                <div><span>Degree</span>
                    <input type="text" inputmode="decimal" name="trig2Angle" required value="<c:out value='${param.trig2Angle}'/>"></div>
            </div>
            <button type="button" class="btn alt seed-btn">Run Seed Generator</button>
            <p class="msg error card-error" hidden></p>
            <div class="seed-out"></div>
        </div>

        <p class="hint" id="submitHint">Run the seed generator for all four questions to unlock Submit.</p>
        <button type="submit" class="btn" id="submitBtn" disabled>Submit Answers</button>
    </form>

    <script src="${pageContext.request.contextPath}/js/game.js"></script>
<%@ include file="includes/footer.jspf" %>
