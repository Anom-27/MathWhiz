<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ include file="includes/header.jspf" %>
    <h2>Result</h2>
    <p class="sub">Player: <strong><c:out value="${result.playerName}"/></strong> &middot; Difficulty: <c:out value="${result.difficulty}"/></p>

    <div class="score"><span>${result.score}</span> / 10</div>

    <ul class="breakdown">
        <c:forEach var="line" items="${breakdown}">
            <li><c:out value="${line}"/></li>
        </c:forEach>
    </ul>

    <c:if test="${not saved}">
        <p class="msg error">Your score could not be saved to the database. Check that MySQL is running and db.properties is correct.</p>
    </c:if>

    <p class="links">
        <a class="btn" href="${pageContext.request.contextPath}/index.jsp">Play Again</a>
        <a class="btn alt" href="${pageContext.request.contextPath}/game?page=leaderboard">Leaderboard</a>
    </p>
<%@ include file="includes/footer.jspf" %>
