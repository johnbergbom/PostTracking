<%@include file="include.jsp"
%><c:import url="header.jsp"/><body>

<div id="main">
	<div id="content">
		<div class="content">
			<div class="columns">
				<div class="column1">
					<h1><spring:message code="menu.title"/>!</h1>
					<div class="blocks">
						<div class="block top">
							<div class="desc">
								<h2><spring:message code="fillOutEmailAndTrackingCode"/>:</h2>
									<%-- Give command object a meaningful name instead of using default name, 'command' --%>
									<form:form commandName="infoObj">
										<div class="form-item">
											<div class="form-label"><spring:message code="productName"/>:</div>
        									<form:input path="productName" size="40" cssErrorClass="form-error-field"/>
        									<div class="form-error-message"><form:errors path="productName"/></div>
    									</div>
										<div class="form-item">
											<div class="form-label"><spring:message code="trackingCode"/>:</div>
 											<spring:message var="trackingTooltip" code="trackingCodeTooltip" scope="request"/>
        									<form:input path="trackingCode" size="40" cssErrorClass="form-error-field" title="${trackingTooltip}"/>
        									<div class="form-error-message"><form:errors path="trackingCode"/></div>
    									</div>
     									<div class="form-item">
											<div class="form-label"><spring:message code="emailAddress"/>:</div>
											<spring:message var="emailTooltip" code="emailAddressTooltip" scope="request"/>
											<form:input path="emailAddress" size="40" cssErrorClass="form-error-field" title="${emailTooltip}"/>
        									<div class="form-error-message"><form:errors path="emailAddress"/></div>
    									</div>
    									<div class="form-item">
    										<input type="submit" value="<spring:message code="submit"/>" />
    									</div>
    									<h3><spring:message code="${information}"/></h3>
									</form:form>

							</div>
						</div>
						<div class="block middle">
							<div class="desc">
								<h2><spring:message code="youGetInfoAboutLocation"/></h2>
								<p><spring:message code="pointTwoInfo"/></p>
							</div>
						</div>
						<div class="block bottom">
							<div class="desc">
								<h2><spring:message code="pointThreeHeadline"/></h2>
								<p><spring:message code="pointThreeInfo"/></p>
							</div>
						</div>
					</div>
					<dev class="footer"><spring:message code="footer"/></dev>
				</div>
			</div>
		</div>
	</div>
</div>

</body>

</html>
