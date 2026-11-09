<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ include file="includes/header.jspf" %>
    <h2>Top 10 Leaderboard</h2>

    <c:if test="${not empty error}">
        <p class="msg error"><c:out value="${error}"/></p>
    </c:if>

    <c:if test="${empty error and empty scores}">
        <p class="sub">No scores yet. Be the first to play!</p>
    </c:if>

    <c:if test="${not empty scores}">
        <table>
            <thead>
            <tr><th>#</th><th>Player</th><th>Score</th><th>Difficulty</th><th>Date / Time</th></tr>
            </thead>
            <tbody>
            <c:forEach var="s" items="${scores}" varStatus="st">
                <tr>
                    <td>${st.count}</td>
                    <td><c:out value="${s.playerName}"/></td>
                    <td>${s.score}</td>
                    <td><c:out value="${s.difficulty}"/></td>
                    <td><c:out value="${s.playedAtText}"/></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>

    <p class="links"><a class="btn" href="${pageContext.request.contextPath}/index.jsp">Play Game</a></p>
<%@ include file="includes/footer.jspf" %>
