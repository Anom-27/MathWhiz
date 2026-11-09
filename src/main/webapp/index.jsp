<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ include file="includes/header.jspf" %>
    <p class="sub">Four maths challenges: prime numbers, modulus and trigonometry. Each question has its own seed generator that changes your numbers, then you pick the right answer. Score up to 10 marks!</p>

    <c:if test="${not empty error}">
        <p class="msg error"><c:out value="${error}"/></p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/game">
        <input type="hidden" name="action" value="start">
        <label for="playerName">Player name</label>
        <input type="text" id="playerName" name="playerName" maxlength="100" required autofocus
               value="<c:out value='${param.playerName}'/>" placeholder="Enter your name">
        <button type="submit" class="btn">Start Game</button>
    </form>

    <p class="links"><a href="${pageContext.request.contextPath}/game?page=leaderboard">View Top 10 Leaderboard</a></p>
<%@ include file="includes/footer.jspf" %>
