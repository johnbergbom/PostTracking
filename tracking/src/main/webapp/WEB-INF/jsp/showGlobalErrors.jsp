<%@include file="include.jsp"
%><spring:hasBindErrors name="infoObj">
  <c:forEach items="${errors.globalErrors}" var="error">
    <div class="form-error-message"><spring:message code="${error.code}" /><br/></div>
  </c:forEach>
</spring:hasBindErrors>

